/**
 * @author walker
 *
 */

package com.example.android.BluetoothRemote;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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
    private CheckBox mCbxSupplementLib;
    
    private boolean mIsSupplementLib;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.config_remote);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);
		
        // Initialize the send button with a listener that for click events
        mOkButton = (Button) findViewById(R.id.ok);
        mOkButton.setOnClickListener(mOnOkClickListner);
        
        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(mOnCancelClickListner);
        
        mCbxSupplementLib = (CheckBox) findViewById(R.id.cbxSupplementLib);
        mCbxSupplementLib.setOnClickListener(mOnCbxSupplementLibClickListener);
        
        mEditText=(EditText) findViewById(R.id.codenumedit);
        Bundle bdl = getIntent().getExtras();   //获取传过来的参数
        mEditText.setText(""+bdl.getInt(REMOTE_CODENUMBER));
        
        mIsSupplementLib=bdl.getBoolean(REMOTE_ISSUPPLEMENTLIB);
        

    	mEditText.setEnabled(
    			!mIsSupplementLib);
    	mCbxSupplementLib.setChecked(mIsSupplementLib);

    
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
        	Intent intent = new Intent();     //申请Bundle变量
        	intent.putExtra(REMOTE_CODENUMBER, Integer.parseInt(mEditText.getText().toString()));     //加到传入变量中
        	intent.putExtra(REMOTE_ISSUPPLEMENTLIB, mIsSupplementLib);
        	setResult(Activity.RESULT_OK,intent);
        	finish();
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
