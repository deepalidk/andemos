/**
 * @author walker
 *
 */

package remotec.BluetoothRemote.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import remotec.BluetoothRemote.BTIO.IrApi;
import remotec.BluetoothRemote.activities.R;
import remotec.BluetoothRemote.activities.R.id;
import remotec.BluetoothRemote.activities.R.layout;
import remotec.BluetoothRemote.activities.R.string;
import remotec.BluetoothRemote.data.DbManager;
import remotec.BluetoothRemote.ui.components.RtArrayAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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
	public static String REMOTE_TRANSMIT_TYPE = "remote_transmit_type";
	public static String REMOTE_TYPE="remote_device_type";

	// Member fields
	private EditText mEditText;
	private EditText mEttFilter;
	private Button mOkButton;
	private Button mCancelButton;
	private RadioButton mRDBBuildIn;
	private RadioButton mRDBExtern;
	private RadioGroup mRadioGroup;
	private CheckBox mCbxSupplementLib;

	private boolean mIsSupplementLib;

	// Member fields
	private RtArrayAdapter<String> mCodelibArrayAdapter;

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
	private TextView mTvwRecordsCount;
	private View mLastSelected = null;

	private int mSelectedIndex;

	private static String TypeName[] = { "", "TV", "VCR", "SATELETE TV",
			"CABLE", "DVD", "AMP/AUDIO", "CD" };

	private static RtArrayAdapter<String> mInternalLib;

	public static void Init(Context context,SQLiteDatabase db) {
		if (mInternalLib == null) {
			// If there are code library files, add each one to the ArrayAdapter
			String id;
			String brandName;
			String irCodeNum;
			int type;
			int i = 1;

			mInternalLib = new RtArrayAdapter<String>(context,
					R.drawable.listview_layout);

			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			// 查询test_listview数据
			cursor = db.query("irCodeList", new String[] {}, null, null, null,
					null, null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {
				id = cursor.getString(cursor.getColumnIndex("id"));
				brandName = cursor
						.getString(cursor.getColumnIndex("brandName"));
				irCodeNum = cursor
						.getString(cursor.getColumnIndex("irCodeNum"));
				type = cursor.getInt(cursor.getColumnIndex("devType"));
				mInternalLib.add(String.format("%d.%s-%s-%s", i++, brandName,
						TypeName[type], irCodeNum));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.config_remote);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		mmIrController = IrApi.getHandle();

		// Initialize the send button with a listener that for click events
		mOkButton = (Button) findViewById(R.id.ok);
		mOkButton.setOnClickListener(mOnOkClickListner);

		mCancelButton = (Button) findViewById(R.id.cancel);
		mCancelButton.setOnClickListener(mOnCancelClickListner);

		mRDBBuildIn = (RadioButton) findViewById(R.id.rbnBuildIn);
		mRDBBuildIn
				.setOnCheckedChangeListener(mRdbBuildInOnCheckedChangeListner);

		mCbxSupplementLib = (CheckBox) findViewById(R.id.cbxSupplementLib);
		mCbxSupplementLib.setOnClickListener(mOnCbxSupplementLibClickListener);

		mEditText = (EditText) findViewById(R.id.codenumedit);
		Bundle bdl = getIntent().getExtras(); // 获取传过来的参数
		mEditText.setText("" + bdl.getInt(REMOTE_CODENUMBER));
		
		mTvwRecordsCount=(TextView)findViewById(R.id.tvwRecordsCount);

		mEttFilter = (EditText) findViewById(R.id.ettFilter);
		mEttFilter.addTextChangedListener(mTextWatcher);

		mIsSupplementLib = bdl.getBoolean(REMOTE_TRANSMIT_TYPE);

		mEditText.setEnabled(!mIsSupplementLib);
		mCbxSupplementLib.setChecked(mIsSupplementLib);

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mCodelibArrayAdapter = new RtArrayAdapter<String>(this,
				R.drawable.listview_layout);
		mInternalLib.setContext(this);

		resetSelection();

		// Find and set up the ListView for paired devices
		codelibListView = (ListView) findViewById(R.id.lvwCodeList);
		codelibListView.setAdapter(mCodelibArrayAdapter);
		codelibListView.setOnItemClickListener(mDeviceClickListener);
		codelibListView.setTextFilterEnabled(true);

		// Register for broadcasts when a device is discovered
		// IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// this.registerReceiver(mReceiver, filter);

		// Get a set of currently paired devices
		mFiles = new ArrayList<File>();

		mFile = new File(mPath);

		if (!mFile.exists()) {
			mFile.mkdir();
		}

		searchFile(mFile, ".rtdb", mFiles);

		if (mRDBBuildIn.isChecked()) {
			updateBuildInLibrary();
		} else {
			updateExternalCodeLibrary();
		}

		codelibListView.scrollTo(0, 0);

	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			UpdateRecordCount();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			UpdateRecordCount();
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			if (mRDBBuildIn.isChecked()) {
				mInternalLib.getFilter().filter(mEttFilter.getText());
			} else {
				mCodelibArrayAdapter.getFilter().filter(mEttFilter.getText());
			}
			
			UpdateRecordCount();

		}
	};

	private void resetSelection() {
		mSelectedIndex = -1;

		if (mSelectedIndex == -1) {
			mOkButton.setEnabled(false);
		}
	}

	protected void updateExternalCodeLibrary() {
		mCodelibArrayAdapter.clear();
		mInternalLib.getFilter().filter("");
		// If there are code library files, add each one to the ArrayAdapter
		if (mFiles.size() > 0) {
			// findViewById(R.id.title_codelib_files).setVisibility(View.VISIBLE);
			int i = 1;
			for (File f : mFiles) {

				mCodelibArrayAdapter.add(String.format("%d.%s", i, f.getName()
						.substring(0, f.getName().length() - 5)));
				i++;
			}
		} else {
			// findViewById(R.id.title_codelib_files)
			// .setVisibility(View.INVISIBLE);
			String noDevices = getResources().getText(
					R.string.none_codelib_file).toString();
			mCodelibArrayAdapter.add(noDevices);
		}

		codelibListView.setAdapter(mCodelibArrayAdapter);
		UpdateRecordCount();
		mLastSelected = null;
	}

	protected void updateBuildInLibrary() {
		mInternalLib.getFilter().filter("");
		codelibListView.setAdapter(mInternalLib);
		UpdateRecordCount();
		mLastSelected = null;
	}
	
	private void UpdateRecordCount()
	{
		mTvwRecordsCount.setText(String.format(" Total: %d ",codelibListView.getAdapter().getCount() ));
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
		
			if (!mRDBBuildIn.isChecked()) {
				if (mFiles.size() > 0) {

					v.setSelected(true);
					mSelectedIndex = arg2;
					mOkButton.setEnabled(mSelectedIndex != -1);
					mLastSelected = v;
				}
			} else {
				v.setSelected(true);
				mSelectedIndex = arg2;
				mOkButton.setEnabled(mSelectedIndex != -1);
				mLastSelected = v;
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

		boolean result = false;
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.updating);

		if (mSelectedIndex < mFiles.size()) {

			TextView view = (TextView) mLastSelected;
			String text = view.getText().toString();

			int index = 0;

			try {
				index = Integer.parseInt(text.split("\\.")[0]);
				index--;
			} catch (Exception e) {
              return false;
			}

			File f = mFiles.get(index);

			if (f != null && f.exists()) {
				try {
					byte[] byteArray = readFileAll(f);

					if (D)
						Log.d(TAG, "Byte Array Len:" + byteArray.length);

					result = mmIrController.StoreLibrary2E2prom((byte) 0,
							byteArray);

					if (D)
						Log.d(TAG, "result:" + result);

					if (result) {
						Toast.makeText(this,
								"Code Library Updated Successfully!",
								Toast.LENGTH_LONG).show();
					} else {
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

	private OnClickListener mOnOkClickListner = new OnClickListener() {
		public void onClick(View v) {
			// Send a message using content of the edit text widget
			String msg = String.format("ButtonId=%d", View.NO_ID);
			if (D)
				Log.d(TAG, msg);
			if (mRDBBuildIn.isChecked()) {

				Intent intent = new Intent(); // 申请Bundle变量

				try {
					String text = ((TextView) mLastSelected).getText()
							.toString().split("-")[2];
					int codeNum = Integer.parseInt(text);

					intent.putExtra(REMOTE_CODENUMBER, codeNum); // 加到传入变量中
					intent.putExtra(REMOTE_TYPE,codeNum%10);
					intent.putExtra(REMOTE_TRANSMIT_TYPE, 0x81);
					setResult(Activity.RESULT_OK, intent);
					finish();

				} catch (Exception ex) {

				}

			} else {
				v.setClickable(false);
				boolean result = doUpdate();
				v.setClickable(true);

				if (result) {
			
					Intent intent = new Intent(); // 申请Bundle变量			
					intent.putExtra(REMOTE_CODENUMBER, 1); // 加到传入变量中
					intent.putExtra(REMOTE_TYPE,getExternalLibType());
					intent.putExtra(REMOTE_TRANSMIT_TYPE, 0x82);
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			}

		}
	};
	
	private int getExternalLibType()
	{
		int result=1;
		
		TextView view = (TextView) mLastSelected;
		String text = view.getText().toString();

		int index = 0;

		try {
			String typeName = text.split("-")[1];
			
		    for(int i=1;i<this.TypeName.length;i++)
		    {
		    	if(typeName.toLowerCase().endsWith(this.TypeName[i].toLowerCase()))
		    	{
		    		result=i;
		    		break;
		    	}
		    }
			
		} catch (Exception e) {

		}
		
		return result;
	}

	private OnClickListener mOnCancelClickListner = new OnClickListener() {
		public void onClick(View v) {
			// Send a message using content of the edit text widget
			String msg = String.format("ButtonId=%d", View.NO_ID);
			if (D)
				Log.d(TAG, msg);
			finish();
		}
	};

	private OnCheckedChangeListener mRdbBuildInOnCheckedChangeListner = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			String msg = String.format("ButtonId=%d", buttonView.NO_ID);
			if (D)
				Log.d(TAG, msg);

			if (isChecked) {
				updateBuildInLibrary();
				mIsSupplementLib = false;
			} else {
				updateExternalCodeLibrary();
				mIsSupplementLib = true;
			}

			resetSelection();
		}
	};

	private OnClickListener mOnCbxSupplementLibClickListener = new OnClickListener() {
		public void onClick(View v) {
			// Send a message using content of the edit text widget
			String msg = String.format("ButtonId=%d", View.NO_ID);
			if (D)
				Log.d(TAG, msg);
			mIsSupplementLib = mCbxSupplementLib.isChecked();
			mEditText.setEnabled(!mIsSupplementLib);
		}
	};

}
