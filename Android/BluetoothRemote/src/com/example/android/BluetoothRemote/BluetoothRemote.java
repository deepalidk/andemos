/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothRemote;

import com.example.android.BluetoothRemote.R;
import com.example.android.BluetoothRemote.R.id;
import com.example.android.BluetoothRemote.R.layout;
import com.example.android.BluetoothRemote.R.menu;
import com.example.android.BluetoothRemote.R.string;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */ 
/** 
 * @author walker
 * 
 */
public class BluetoothRemote extends Activity implements View.OnClickListener  {
	// Debugging 
	private static final String TAG = "BluetoothRemote";
	private static final boolean D = false;
	private static final boolean emulatorTag=true;

	// Message types sent from the BluetoothRemoteService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothRemoteService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_CONFIG_REMOTE = 3;
	private static final int REQUEST_UPDATE_REMOTE = 4;

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private MenuItem mMenuConfig;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
//	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothRemoteService mChatService = null;

	// button press sound
	private SoundPool soundPool;
	private int soundId;

	// private Code number;
	private int mCodeNum;

	private boolean mIsSupplementLib;
	
	/**
	 * Ir API object
	 */
	private IrApi mmIrController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);

		mTitle = (TextView) findViewById(R.id.title_right_text);

		mmIrController = IrApi.getHandle();

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// SoundPool的初始化
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundId = soundPool.load(this, R.raw.water, 1);

		mCodeNum = 125;
		mIsSupplementLib=false;
		// If the adapter is null, then Bluetooth is not supported
		if(!emulatorTag)
		{
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, "Bluetooth is not available",
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
       
		if(!emulatorTag)
		{
			// If BT is not on, request that it be enabled.
			// setupChat() will then be called during onActivityResult
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				// Otherwise, setup the chat session
			} else {
				if (mChatService == null)
					setupChat();
			}
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothRemoteService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// // Initialize the array adapter for the conversation thread
		// mConversationArrayAdapter = new ArrayAdapter<String>(this,
		// R.layout.message);
		// mConversationView = (ListView) findViewById(R.id.in);
		// mConversationView.setAdapter(mConversationArrayAdapter);
		//
		// // Initialize the compose field with a listener for the return key
		// mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		// mOutEditText.setOnEditorActionListener(mWriteListener);
		//
		// // Initialize the send button with a listener that for click events
		// mSendButton = (Button) findViewById(R.id.button_send);
		// mSendButton.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// // Send a message using content of the edit text widget
		// TextView view = (TextView) findViewById(R.id.edit_text_out);
		// String message = view.getText().toString();
		// sendMessage(message);
		// }
		// });

		// Initialize the BluetoothRemoteService to perform bluetooth
		// connections
		mChatService = new BluetoothRemoteService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothRemoteService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothRemoteService to
			// write

			byte[] version = new byte[2];
			version[0] = (byte) 0xff;
			version[1] = (byte) 0xff;
			boolean result = mmIrController.IrGetVersion(version);

			if (result) {
				if (D)
					Log.i(TAG, "ir.IrGetVersion: True");
				String strLog = String.format("%d ", version.length,
						version[0], version[1]);
				if (D)
					Log.i(TAG, strLog);
			} else {
				if (D)
					Log.i(TAG, "ir.IrGetVersion: False");
			}

			// byte[] send = message.getBytes();
			// mChatService.write(send);
			//
			// // Reset out string buffer to zero and clear the edit text field
			// mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);

			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	// The Handler that gets information back from the BluetoothRemoteService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothRemoteService.STATE_CONNECTED: {
					boolean result = mmIrController.init(mChatService);
					// if(result)
					// {
					setConnectedTitle();
					mMenuConfig.setEnabled(true);
//					mConversationArrayAdapter.clear();
					// }
					// else
					// {
					// mChatService.stop();
					// mMenuConfig.setEnabled(false);
					// mTitle.setText(R.string.title_not_connected);
					// }
					break;
				}
				case BluetoothRemoteService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					if (mMenuConfig != null) {
						mMenuConfig.setEnabled(false);
					}
					break;
				case BluetoothRemoteService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					if (mMenuConfig != null) {
						mMenuConfig.setEnabled(false);
					}
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
//				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
//				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
//						+ readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case REQUEST_CONFIG_REMOTE:
			if (resultCode == Activity.RESULT_OK) {
				mCodeNum = data
						.getIntExtra(ConfigRemote.REMOTE_CODENUMBER, 125);
				mIsSupplementLib=data.getBooleanExtra(ConfigRemote.REMOTE_ISSUPPLEMENTLIB, false);
				setConnectedTitle();
			}
			break;
			
		case REQUEST_UPDATE_REMOTE:
			if(resultCode==Activity.RESULT_OK)
			{
				
			}
			break;
		}
	}

	/**
	 * set the title when succeeded connected to remote;
	 */
	private void setConnectedTitle() {
		mTitle.setText(R.string.title_connected_to);
		mTitle.append(mConnectedDeviceName);
		if(this.mIsSupplementLib)
		{
			mTitle.append(" SupplementLib");
		}
		else
		{
			mTitle.append(" IRCode:" + mCodeNum);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		mMenuConfig = (MenuItem) menu.findItem(R.id.Config);
		if (mMenuConfig != null) {
//			mMenuConfig.setEnabled(false);
			mMenuConfig.setEnabled(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.Config:
			Intent remoteConfig = new Intent(this, ConfigRemote.class);
			Bundle bdl = new Bundle(); // 申请Bundle变量
			bdl.putInt(ConfigRemote.REMOTE_CODENUMBER, mCodeNum); // 加到传入变量中
			bdl.putBoolean(ConfigRemote.REMOTE_ISSUPPLEMENTLIB, this.mIsSupplementLib);
			remoteConfig.putExtras(bdl); // 传参
			startActivityForResult(remoteConfig, REQUEST_CONFIG_REMOTE);

			return true;
		case R.id.Update:
			
			Intent updateIntent = new Intent(this, CodelibListActivity.class);
			startActivityForResult(updateIntent, REQUEST_UPDATE_REMOTE);
			
			return true;
		} 
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// Kabloey
//		soundPool.play(soundId, 3, 3, 1, 0, 1f);// 备注3
		byte keyId = Byte.parseByte(v.getTag().toString(), 10);

		if (mmIrController != null) {
			if (mChatService.getState() ==BluetoothRemoteService.STATE_CONNECTED) {
//				boolean result = mmIrController.transmitPreprogramedCode(
//						(byte) 0x81, (byte) (mCodeNum % 10), mCodeNum / 10,
//						keyId);
				
				for(int i=1;i<100;i++)
				{
				
				boolean result = mmIrController.transmitPreprogramedCode(
						(byte) 0x82, (byte) 1, 1,
						(byte)i);
				}
			}
		}
	}
}