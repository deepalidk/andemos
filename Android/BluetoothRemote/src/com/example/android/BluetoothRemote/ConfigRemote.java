/**
 * @author walker
 *
 */

package com.example.android.BluetoothRemote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.android.BluetoothRemote.R;
import com.example.android.BluetoothRemote.R.id;
import com.example.android.BluetoothRemote.R.layout;
import com.example.android.BluetoothRemote.R.string;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This Activity appears as a dialog. It Set the Current Remote Code num.
 */
public class ConfigRemote extends Activity {
	// Debugging
	private static final String TAG = "ConfigRemote";
	private static final boolean D = false;
 
	// Return Intent extra
	public static String REMOTE_CODENUMBER = "remote_code_number";
	public static String REMOTE_ISSUPPLEMENTLIB = "remote_is_supplement_lib";
	

	// Member fields
    private EditText mEditText;
    private Button mOkButton;
    private Button mCancelButton;
    private RadioButton mRDBBuildIn;
    private RadioButton mRDBExtern;
    private RadioGroup mRadioGroup;
    private CheckBox mCbxSupplementLib;
    
    private boolean mIsSupplementLib;
    
	// Member fields
	private ArrayAdapter<String> mCodelibArrayAdapter;

	List<File> mFiles;

	// File
	private File mFile;
	private String mPath = "/sdcard/remotec";
	private String key_search;

	/**
	 * Ir API object
	 */
	private IrApi mmIrController;
	private ListView codelibListView;

	private int mSelectedIndex;
    
	private int buildInIrCode[]={51,201,951,1091
			,1491,1571,1651,1831
			,1881,1961,2151,2711
			,2801,1,101,251
			,261,331,351,531
			,571,771,781,1191
			,2071,2141,2721,1
			,341,0461,681,691
			,1221,1291,1441,2031
			,2201,2261,2731,1071
			,1641,1731,1901,2161
			,2741,1741,2901
			};
	private String buildLibNames[]={"Panasonic-0051","Panasonic-0201","Panasonic-0951","Panasonic-1091"
			,"Panasonic-1491","Panasonic-1571","Panasonic-1651","Panasonic-1831"
			,"Panasonic-1881","Panasonic-1961","Panasonic-2151","Panasonic-2711"
			,"Panasonic-2801","Samsung-0001","Samsung-0101","Samsung-0251"
			,"Samsung-0261","Samsung-0331","Samsung-0351","Samsung-0531"
			,"Samsung-0571","Samsung-0771","Samsung-0781","Samsung-1191"
			,"Samsung-2071","Samsung-2141","Samsung-2721","Sharp-0001"
			,"Sharp-0341","Sharp-0461","Sharp-0681","Sharp-0691"
			,"Sharp-1221","Sharp-1291","Sharp-1441","Sharp-2031"
			,"Sharp-2201","Sharp-2261","Sharp-2731","Sony-1071"
			,"Sony-1641","Sony-1731","Sony-1901","Sony-2161"
			,"Sony-2741","Vizio-1741","Vizio-2901"		
			};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.config_remote);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);
		
		mmIrController = IrApi.getHandle();
		
        // Initialize the send button with a listener that for click events
        mOkButton = (Button) findViewById(R.id.ok);
        mOkButton.setOnClickListener(mOnOkClickListner);
        
        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(mOnCancelClickListner);
        
        mRDBBuildIn=(RadioButton) findViewById(R.id.rbnBuildIn);
        mRDBBuildIn.setOnCheckedChangeListener(mRdbBuildInOnCheckedChangeListner);
         
        mCbxSupplementLib = (CheckBox) findViewById(R.id.cbxSupplementLib);
        mCbxSupplementLib.setOnClickListener(mOnCbxSupplementLibClickListener);
        
        mEditText=(EditText) findViewById(R.id.codenumedit);
        Bundle bdl = getIntent().getExtras();   //获取传过来的参数
        mEditText.setText(""+bdl.getInt(REMOTE_CODENUMBER));
        
        mIsSupplementLib=bdl.getBoolean(REMOTE_ISSUPPLEMENTLIB);
        

    	mEditText.setEnabled(
    			!mIsSupplementLib);
    	mCbxSupplementLib.setChecked(mIsSupplementLib);
    	
    	
    	
    	// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mCodelibArrayAdapter = new ArrayAdapter<String>(this,
				R.drawable.listview_layout); 
 
		resetSelection();

		// Find and set up the ListView for paired devices
		codelibListView = (ListView) findViewById(R.id.lvwCodeList);
		codelibListView.setAdapter(mCodelibArrayAdapter);
		codelibListView.setOnItemClickListener(mDeviceClickListener);
		codelibListView.setFocusableInTouchMode(true);

		// Register for broadcasts when a device is discovered
		// IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// this.registerReceiver(mReceiver, filter);

		// Get a set of currently paired devices
		mFiles = new ArrayList<File>();

		mFile = new File(mPath);
		
		if(!mFile.exists())
		{
			mFile.mkdir();
		}
		
		searchFile(mFile, ".rtdb", mFiles);
	
		if(mRDBBuildIn.isChecked())
    	{
    		updateBuildInLibrary();
    	}
    	else
    	{
    		updateExternalCodeLibrary();
    	}
		
		codelibListView.scrollTo(0, 0);
    
	}

	private void resetSelection() {
		mSelectedIndex = -1;

		if (mSelectedIndex == -1) {
			mOkButton.setEnabled(false);
		}
	}
	
	protected void updateExternalCodeLibrary()
	{
		mCodelibArrayAdapter.clear();
		// If there are code library files, add each one to the ArrayAdapter
		if (mFiles.size() > 0) {
//			findViewById(R.id.title_codelib_files).setVisibility(View.VISIBLE);
			int i=1;
			for (File f : mFiles) {
				
				mCodelibArrayAdapter.add(String.format("%d.%s", i,f.getName().substring(0,
						f.getName().length() - 5)));
				i++;
			}
		} else {
//			findViewById(R.id.title_codelib_files)
//					.setVisibility(View.INVISIBLE);
			String noDevices = getResources().getText(
					R.string.none_codelib_file).toString();
			mCodelibArrayAdapter.add(noDevices);
		}
	}
	
	protected void updateBuildInLibrary()
	{
		
		mCodelibArrayAdapter.clear();
		// If there are code library files, add each one to the ArrayAdapter

		int i=1;
		for (String name : buildLibNames) {
				mCodelibArrayAdapter.add(String.format("%d.%s", i,name));
				i++;
		}

	}
	
	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

			if(!mRDBBuildIn.isChecked())
			{
			if(mFiles.size()>0)
			{
//			 codelibListView.setSelection(arg2);
			 v.setSelected(true);
			 mSelectedIndex = arg2;
			 mOkButton.setEnabled(mSelectedIndex != -1);
			}
			}
			else
			{
//				 codelibListView.setSelection(arg2);
				 v.setSelected(true);
				 mSelectedIndex = arg2;
				 mOkButton.setEnabled(mSelectedIndex != -1);
			}

		}
	};
	
	/**
	 * 
	 * ReadFile
	 * 
	 * 
	 * 
	 * @param fileName
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public byte[] readFileAll(File file) throws Exception {

		FileInputStream fileInputStream = new FileInputStream(file);

		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];

		int len = 0;

		while ((len = fileInputStream.read(buffer)) > 0) {

			byteArray.write(buffer, 0, len);

		}
		;

		return byteArray.toByteArray();
	}

	/**
	 * Start remote update
	 */
	private boolean doUpdate() {
		if (D)
			Log.d(TAG, "doUpdate()");

		boolean result=false;
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.updating);

		if (mSelectedIndex < mFiles.size()) {

			File f = mFiles.get(mSelectedIndex);

			if (f != null && f.exists()) {
				try {
					byte[] byteArray = readFileAll(f);

					if (D)
						Log.d(TAG, "Byte Array Len:" + byteArray.length);

					result = mmIrController.StoreLibrary2E2prom(
							(byte) 0, byteArray);

					if (D)
						Log.d(TAG, "result:" + result);
				
					if(result)
					{
						Toast.makeText(this, "Code Library Updated Successfully!",
								Toast.LENGTH_LONG).show();
					}
					else
					{
						Toast.makeText(this, "Failed, Please Try Again!",
								Toast.LENGTH_LONG).show();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		setProgressBarIndeterminateVisibility(false);
		setTitle(R.string.select_codelib);
		
		return result;

	}
	
	protected void searchFile(File dir, String suffix, List<File> result) {
		// TODO Auto-generated method stub
		try {
			File[] all_file = dir.listFiles();

			for (File tempf : all_file) {

				if (tempf.isDirectory()) {
					searchFile(tempf, suffix, result);
				} else if (tempf.getName().endsWith(suffix)) {

					result.add(tempf);

				}
			}

		} catch (Exception e) {
			// 如果路径找不到，提示错误
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private OnClickListener  mOnOkClickListner=new OnClickListener() {
        public void onClick(View v) {
            // Send a message using content of the edit text widget
        	String msg=String.format("ButtonId=%d",View.NO_ID );
        	if(D)Log.d(TAG, msg);
        	if(mRDBBuildIn.isChecked())
        	{
        		 if (mSelectedIndex < buildInIrCode.length) {
		        	 Intent intent = new Intent();     //申请Bundle变量
		        	 intent.putExtra(REMOTE_CODENUMBER,buildInIrCode[mSelectedIndex]);     //加到传入变量中
		        	 intent.putExtra(REMOTE_ISSUPPLEMENTLIB, false);
		        	 setResult(Activity.RESULT_OK,intent);
		         	 finish();
        		 }
        	}
        	else
        	{
        		v.setClickable(false);
        		boolean result=doUpdate();
        		v.setClickable(true);
        		
        		if(result)
        		{
               	 Intent intent = new Intent();     //申请Bundle变量
            	 intent.putExtra(REMOTE_CODENUMBER, 1);     //加到传入变量中
            	 intent.putExtra(REMOTE_ISSUPPLEMENTLIB, true);
        		 setResult(Activity.RESULT_OK,intent);
                 finish();
        		}
        	}

        }
	};
	
	private OnClickListener  mOnCancelClickListner=new OnClickListener() {
        public void onClick(View v) {
            // Send a message using content of the edit text widget
        	String msg=String.format("ButtonId=%d",View.NO_ID );
        	if(D)Log.d(TAG, msg);
        	finish();
        }
	};
	
	private OnCheckedChangeListener  mRdbBuildInOnCheckedChangeListner=new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
        	String msg=String.format("ButtonId=%d",buttonView.NO_ID );
        	if(D)Log.d(TAG, msg);
        	
        	if(isChecked)
        	{
        		updateBuildInLibrary();
        		mIsSupplementLib=false;
        	}
        	else
        	{
        		updateExternalCodeLibrary();
        		mIsSupplementLib=true;
        	}
        	
        	resetSelection();  	
		}
	};
	
//	private OnClickListener  mRdbExternOnClickListner=new OnClickListener() {
//        public void onClick(View v) {
//            // Send a message using content of the edit text widget
//        	String msg=String.format("ButtonId=%d",View.NO_ID );
//        	if(D)Log.d(TAG, msg);
//        
//        }
//	};
	
	private OnClickListener  mOnCbxSupplementLibClickListener=new OnClickListener() {
        public void onClick(View v) {
            // Send a message using content of the edit text widget
        	String msg=String.format("ButtonId=%d",View.NO_ID );
        	if(D)Log.d(TAG, msg);
        	mIsSupplementLib=mCbxSupplementLib.isChecked();
        	mEditText.setEnabled(!mIsSupplementLib);
//        	finish();
        }
	};
	
	
	
}
