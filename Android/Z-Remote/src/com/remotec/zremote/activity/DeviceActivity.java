/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.zremote.activity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.common.FileManager;
import com.remotec.zremote.activity.R;
import com.remotec.zremote.activity.component.DeviceButton;
import com.remotec.zremote.activity.component.KeyButton;
import com.remotec.zremote.data.Device;
import com.remotec.zremote.data.Extender;
import com.remotec.zremote.data.Key;
import com.remotec.zremote.data.RemoteUi;
import com.remotec.zremote.data.RemoteUi.BrandListType;
import com.remotec.zremote.irapi.BtConnectionManager;
import com.remotec.zremote.irapi.IrApi;
import com.remotec.zremote.irapi.Zapi;
import com.remotec.zremote.persistence.XmlManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/*
 *Displays device for UI. 
 */
public class DeviceActivity extends Activity {

	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ADD_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private static final int REQUEST_EDIT_DEVICE = 4;

	// Debugging Tags
	private static final String TAG = "zremoteActivity";
	private static final boolean D = false;

	// dialog　ids
	private static final int PROGRESS_DIALOG = 0;

	// Message types sent from the Bluetooth connect manager Handler
	public static final int CONNECTTION_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_DEVICE_ADDRESS = 6;

	// Key names received from the BluetoothRemoteService Handler
	public static final String DEVICE_NAME = "device_name";
	// Key names received from the BluetoothRemoteService Handler
	public static final String DEVICE_ADDRESS = "device_address";
	public static final String TOAST = "toast";

	private ProgressDialog mProgressDialog;

	private List<DeviceButton> mDevButtonList = null;
	private TextView mTitleRight = null;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BtConnectionManager mBtConnectMgr = null;

	// /**
	// * Ir API object
	// */
	// private IrApi mIrController;

	/**
	 * z-wave controller object
	 */
	private Zapi mZController;

	private float mScale = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.device);

		// for null pointer bug, move the find right title text before bt init.
		mTitleRight = (TextView) findViewById(R.id.title_right_text);
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (!RemoteUi.getEmulatorTag()) {
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, "Bluetooth is not available",
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		// Initializing data.
		InitAppTask initor = new InitAppTask();
		initor.execute(0);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle("");
			mProgressDialog.setMessage(getResources().getText(
					R.string.initial_waiting));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(false);
			return mProgressDialog;
		default:
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.device_menu, menu);

		return true;
	}

	/*
	 * inits the data for device activity
	 */
	private void initData() {

		// /*
		// * irApi init.
		// */
		// mIrController = IrApi.getHandle();

		mZController = Zapi.getHandle();

		mScale = getResources().getDimension(R.dimen.scale);

		// we already load the device object infos now,
		// but the resId of icon can only access during run time.
		// get the resId of icon with the icon name, and set to device object.
		initDeviceIconId();

		mDevButtonList = new ArrayList<DeviceButton>();

		ViewGroup tbLayout = (ViewGroup) findViewById(R.id.device_table);

		findButtons(tbLayout, mDevButtonList);

		for (DeviceButton devBtn : mDevButtonList) {
			devBtn.setOnClickListener(this.mDevButtonOnClickListener);
			devBtn.setOnLongClickListener(this.mDevButtonOnLongClickListener);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_connect:
			startConnectDialog();
			return true;
		case R.id.menu_del:
			// check if an extender is connected, if not, then start an connect
			// activity.
			if (checkConnectionState()) {
			 (new DelDevTask()).execute(0);
			}
			
			return true;
		case R.id.menu_about:
			displayAboutDialog();
			return true;

		default:
			break;
		}
		return false;
	}

	/*
	 * check current bt connection state. if none, then ask if user wanna a
	 * connection now. if connecting, then told user to try again. if connected,
	 * do nothing.
	 */
	private boolean checkConnectionState() {

		if (RemoteUi.getEmulatorTag()) {
			return true;
		}

		if (this.mBtConnectMgr.getState() == BtConnectionManager.STATE_NONE) {
			/* build a dialog, ask if want to connect an extender */
			AlertDialog.Builder builder = new Builder(this);

			builder.setMessage(R.string.connect_now);

			builder.setTitle(R.string.connect_extender);

			builder.setIcon(android.R.drawable.ic_dialog_info);

			builder.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							// start connect extender activity.
							startConnectDialog();
						}
					});

			builder.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					});

			builder.create().show();

			return false;
		}

		return true;
	}

	/*
	 * starts an activity to connect an bt extender.
	 */
	private void startConnectDialog() {
		// Launch the DeviceListActivity to see devices and do scan
		Intent serverIntent = new Intent(DeviceActivity.this,
				BtDeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	/*
	 * get Current apk version
	 */
	private String getVersionName() {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		String version = "";
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return version;
	}

	/*
	 * Display system info.
	 */
	private void displayAboutDialog() {

		// String version = getVersionName(); // 得到版本信息
		// Toast.makeText(this,
		// "packageName:" + packageName + ";version:" + version,
		// Toast.LENGTH_LONG).show();

		/*
		 * displays the learn failed dialog.
		 */
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.about_title);

		String msg = String.format("Version:  %s\n", getVersionName());

		if (RemoteUi.getHandle().getActiveExtender() != null) {
			msg += String.format("Extender: %s\n", RemoteUi.getHandle()
					.getActiveExtender().getVersion().toUpperCase());
		} else {
			msg += String.format("Extender: not connected\n");
		}

		builder.setMessage(msg);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.create().show();

	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		if (!RemoteUi.getEmulatorTag()) {
			// If BT is not on, request that it be enabled.
			// setupChat() will then be called during onActivityResult
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				// Otherwise, setup the chat session
			} else {
				if (mBtConnectMgr == null)
					setupBluetooth();
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
		if (mBtConnectMgr != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mBtConnectMgr.getState() == mBtConnectMgr.STATE_NONE) {
				// Start the Bluetooth chat services
				mBtConnectMgr.start();
			}
		}
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
		if (mBtConnectMgr != null)
			mBtConnectMgr.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	/*
	 * Inits the Device object Icon res id.
	 */
	private void initDeviceIconId() {
		List<Device> devList = RemoteUi.getHandle().getChildren();

		for (Device dev : devList) {
			int resId = getResources().getIdentifier(dev.getIconName(),
					"drawable", getApplicationInfo().packageName);
			dev.setIconResId(resId);
		}
	}

	private void findButtons(ViewGroup vg, List<DeviceButton> bList) {

		for (int i = 0; i < vg.getChildCount(); i++) {
			View v = vg.getChildAt(i);

			if (v instanceof DeviceButton) {
				bList.add((DeviceButton) v);
			} else if (v instanceof ViewGroup) {
				findButtons((ViewGroup) v, bList);
			}
		}
	}

	/*
	 * displays the devices on the screen.
	 */
	private void displayDevices() {

		if (RemoteUi.getHandle().getChildren().size() > 0) {
			List<Device> devList = RemoteUi.getHandle().getChildren();

			for (int i = 0; i < devList.size(); i++) {
				displayDevice(devList.get(i), mDevButtonList.get(i));
			}

			if (devList.size() < mDevButtonList.size()) {
				// add device button
				displayAddDevice(mDevButtonList.get(devList.size()));

				for (int i = devList.size() + 1; i < mDevButtonList.size(); i++) {
					mDevButtonList.get(i).setVisibility(View.INVISIBLE);
				}
			}
		} else {
			for (int i = 1; i < mDevButtonList.size(); i++) {
				mDevButtonList.get(i).setVisibility(View.INVISIBLE);
			}
			// add device button
			displayAddDevice(mDevButtonList.get(0));
		}
	}

	/*
	 * displays a device button with device button object information.
	 */
	private void displayDevice(Device dev, DeviceButton devButton) {
		devButton.setDevice(dev);
		devButton.setVisibility(View.VISIBLE);
		devButton.setText(dev.getName());
		setDevButtonIcon(devButton, dev.getIconResId());
	}

	/*
	 * displays add device button.
	 */
	private void displayAddDevice(DeviceButton devButton) {
		devButton.setDevice(null);
		devButton.setVisibility(View.VISIBLE);
		devButton.setText(R.string.add_device);
		setDevButtonIcon(devButton, R.drawable.img_add);
	}

	/*
	 * Sets the device button Icon
	 */
	private void setDevButtonIcon(DeviceButton devButton, int resId) {
		Drawable topD = this.getResources().getDrawable(resId);
		if (topD != null) {
			topD.setBounds(0, 0, (int) (topD.getMinimumWidth() * mScale),
					(int) (topD.getMinimumHeight() * mScale));
			devButton.setCompoundDrawables(null, topD, null, null);
		}
	}

	private OnClickListener mDevButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DeviceButton devButton = (DeviceButton) v;

			// check if an extender is connected, if not, then start an connect
			// activity.
			if (checkConnectionState()) {
				// identifies add device button or device button
				if (devButton.getDevice() != null) {

					/*
					 * crate a intent object, then call the device activity
					 * class
					 */
//					Intent devKeyIntent = new Intent(DeviceActivity.this,
//							DeviceKeyActivity.class);
//					devKeyIntent.putExtra(DeviceKeyActivity.ACTIVITY_MODE,
//							DeviceKeyActivity.ACTIVITY_CONTROL);
//					RemoteUi.getHandle().setActiveDevice(devButton.getDevice());
//
//					startActivity(devKeyIntent);
			
					byte nodeId=(byte)devButton.getDevice().getIrCode();
					byte value=(byte) ((devButton.getDevice().getValue()==0)?0x63:0x00);
					mZController.ControlDevice(nodeId, (byte) 0, value);
					
					devButton.getDevice().setValue(value);
					
				} else {

					// Intent addDeviceIntent = new Intent(DeviceActivity.this,
					// AddDeviceActivity.class);
					// startActivityForResult(addDeviceIntent,
					// REQUEST_ADD_DEVICE);

					// Initializing data.
					AddDevTask task = new AddDevTask();
					task.execute(0);
				}
			}

		}

	};

	private OnLongClickListener mDevButtonOnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {

			DeviceButton devBtn = (DeviceButton) v;

			// identifies add device button or device button
			if (devBtn.getDevice() != null) {

				// check if an extender is connected, if not, then start an
				// connect
				// activity.
				if (checkConnectionState()) {

					Device dev = devBtn.getDevice();

					RemoteUi.getHandle().setActiveDevice(dev);

					displayEditChoiceMenu(dev);

					return true;
				}
			}

			return false;
		}

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_ADD_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				displayDevices();
			}
			break;

		case REQUEST_EDIT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				displayDevices();
			}
			break;

		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						BtDeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mBtConnectMgr.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupBluetooth();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		}
	}

	private void setupBluetooth() {
		Log.d(TAG, "setupBluetooth()");

		// Initialize the BluetoothRemoteService to perform bluetooth
		// connections
		mBtConnectMgr = new BtConnectionManager(mHandler);

	}

	/*
	 * displays the remove confirm dialog.
	 */
	private void displayRemoveConfirmDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(DeviceActivity.this);

		builder.setMessage(R.string.remove_device_message);

		builder.setTitle(R.string.remove_device_title);

		builder.setIcon(android.R.drawable.ic_dialog_alert);

		builder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Device activeDev = RemoteUi.getHandle()
								.getActiveDevice();

						RemoteUi.getHandle().getChildren().remove(activeDev);

						/*
						 * save the data.
						 */
						XmlManager xmlManager = new XmlManager();
						xmlManager.saveData(RemoteUi.getHandle(),
								RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
										+ RemoteUi.UI_XML_FILE);

						dialog.dismiss();
						DeviceActivity.this.displayDevices();
					}
				});

		builder.setNegativeButton(android.R.string.no,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});

		builder.create().show();
	}

	/*
	 * display a choice menu
	 */
	private void displayEditChoiceMenu(Device dev) {
		AlertDialog dlg;
		Builder builder = new AlertDialog.Builder(DeviceActivity.this);
		builder.setTitle(dev.getName());

		// 设置可供选择的ListView
		builder.setItems(R.array.dev_long_click_menu,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// which是选中的位置(基于0的)
						if (which == 0) { // edit device
							Intent addDeviceIntent = new Intent(
									DeviceActivity.this,
									EditDeviceActivity.class);
							startActivityForResult(addDeviceIntent,
									REQUEST_ADD_DEVICE);
						} else { // remove device
							displayRemoveConfirmDlg();
						}

						dialog.dismiss();
					}
				});

		dlg = builder.create();

		dlg.show();
	}

	/*
	 * the brand list used to add device.
	 */
	void loadCategoryAndBrand(BrandListType type) {

		// we already load brand list when startup.
		// Now we check the needed type and current type to determine whether
		// reload
		// brand list.
		// if the the require type is the same as the current type we do
		// nothing.
		BrandListType curBrandType = RemoteUi.getHandle().getBrandListType();

		if (curBrandType == type)
			return;

		/*
		 * if a category has no device under it, we will not display it to user.
		 */
		RemoteUi.getHandle().clearEmptyCategory();
	}

	// The Handler that gets information back from the BluetoothRemoteService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONNECTTION_STATE_CHANGE:
				switch (msg.arg1) {
				case BtConnectionManager.STATE_CONNECTED: {

					String version = mZController.init(mBtConnectMgr);

					if (version != null) {
						// set the version info to the extender.
						Extender actExtender = RemoteUi.getHandle()
								.getActiveExtender();
						actExtender.setVersion(version);

						mTitleRight.setText(R.string.title_connected_to);
						mTitleRight.append(actExtender.getName());

						XmlManager xm = new XmlManager();
						xm.saveData(RemoteUi.getHandle(),
								RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
										+ RemoteUi.UI_XML_FILE);

						// if the extender support Uird lib we load uird brand
						// list.
						// only when the extender not support uird and support
						// buildIn. we load build in.
						if (actExtender.getSupportUirdLib()) {
							loadCategoryAndBrand(BrandListType.UIRD);
						} else if ((!actExtender.getSupportUirdLib())
								&& actExtender.getSupportInternalLib()) {
							loadCategoryAndBrand(BrandListType.BuildIn);
						}

					} else {
						mTitleRight.setText(R.string.title_not_connected);
					}

					break;
				}
				case BtConnectionManager.STATE_CONNECTING:
					mTitleRight.setText(R.string.title_connecting);
					break;
				case BtConnectionManager.STATE_NONE:
					mTitleRight.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_DEVICE_ADDRESS:
				// save the connected device's name
				String devAddr = msg.getData().getString(DEVICE_ADDRESS);

				Extender activeExtender;

				// already has the extender.
				if (RemoteUi.getHandle().getExtenderMap().containsKey(devAddr)) {
					activeExtender = RemoteUi.getHandle().getExtenderMap()
							.get(devAddr);
					RemoteUi.getHandle().setActiveExtender(activeExtender);
				} else {
					activeExtender = new Extender();
					activeExtender.setAddress(devAddr);
					RemoteUi.getHandle().getExtenderMap()
							.put(devAddr, activeExtender);
					RemoteUi.getHandle().setActiveExtender(activeExtender);
				}

				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				String devName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + devName, Toast.LENGTH_SHORT).show();
				RemoteUi.getHandle().getActiveExtender().setName(devName);

				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	/*
	 * finds all key Buttons
	 */
	private void findKeyButtons(ViewGroup vg, Map<Integer, KeyButton> bMap) {

		for (int i = 0; i < vg.getChildCount(); i++) {
			View v = vg.getChildAt(i);

			if (v instanceof KeyButton) {
				KeyButton btn = (KeyButton) v;
				if (btn.getKeyId() != -1) {
					bMap.put(btn.getKeyId(), btn);
				}
			} else if (v instanceof ViewGroup) {
				findKeyButtons((ViewGroup) v, bMap);
			}
		}
	}

	/*
	 * AsyncTask for App Initializing.
	 */
	private class InitAppTask extends
			android.os.AsyncTask<Integer, Integer, Integer> {

		/*
		 * compare the db version of remote.db and remote_temp.db.
		 * 
		 * use the latest db and copy the device info to latest db. cover
		 * current db file with the latest one.
		 */
		private void doDbUpdate() {
			return;
		}

		@Override
		protected Integer doInBackground(Integer... params) {

			try {
				// if the data is already init , we jump out this method.
				// if(RemoteUi.getHandle()!=null) return 0;

				RemoteUi.init();

				// copys the UI XML file to sdcard.
				FileManager.saveAs(DeviceActivity.this, R.raw.remote,
						RemoteUi.INTERNAL_DATA_DIRECTORY, RemoteUi.UI_XML_FILE);

				doDbUpdate();

				/*
				 * loads the UI component information.
				 */
				XmlManager xm = new XmlManager();
				xm.loadData(RemoteUi.getHandle(),
						RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
								+ RemoteUi.UI_XML_FILE);

				/*
				 * loads ircode information to memory for adding device.
				 */
				boolean load_uird_brand = DeviceActivity.this.getResources()
						.getBoolean(R.bool.load_uird_brand);
				loadCategoryAndBrand(load_uird_brand ? BrandListType.UIRD
						: BrandListType.BuildIn);

				initData();

				/*
				 * finds all key Buttons and create a keyLayout template.
				 */
				LayoutInflater inflater = (LayoutInflater) DeviceActivity.this
						.getSystemService(LAYOUT_INFLATER_SERVICE);

				Looper.prepare();

				View vgKey = inflater.inflate(R.layout.devicekey, null);

				/*
				 * finds all key Buttons
				 */
				Map<Integer, KeyButton> keyBtnMap = new Hashtable<Integer, KeyButton>();

				ViewGroup vgKeyLayout = (ViewGroup) vgKey
						.findViewById(R.id.id_key_layout);

				findKeyButtons(vgKeyLayout, keyBtnMap);

				/*
				 * save key button informations to the template map.
				 */
				Map<Integer, Key> map = RemoteUi.getHandle()
						.getTemplateKeyMap();

				Integer invalidKeyId = DeviceActivity.this.getResources()
						.getInteger(R.integer.key_id_invalid);

				for (KeyButton keyBtn : keyBtnMap.values()) {
					Key temp = new Key();
					temp.setKeyId(keyBtn.getKeyId());
					temp.setText(keyBtn.getText().toString());
					temp.setVisible(keyBtn.getVisibility() == View.VISIBLE);
					map.put(temp.getKeyId(), temp);
				}
			} catch (Exception ex) {
				Log.e(TAG, ex.getMessage());
			}

			return 0;
		}

		@Override
		protected void onPreExecute() {
			DeviceActivity.this.showDialog(DeviceActivity.PROGRESS_DIALOG);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Integer result) {

			displayDevices();

			DeviceActivity.this.removeDialog(DeviceActivity.PROGRESS_DIALOG);
		}
	}

	/*
	 * AsyncTask for App Initializing.
	 */
	private class AddDevTask extends
			android.os.AsyncTask<Integer, Integer, Integer> {

		private int nodeId=-1;
		/*
		 * compare the db version of remote.db and remote_temp.db.
		 * 
		 * use the latest db and copy the device info to latest db. cover
		 * current db file with the latest one.
		 */
		private void doDbUpdate() {
			return;
		}

		@Override
		protected Integer doInBackground(Integer... params) {

			nodeId = -1;
			try {
					nodeId = mZController.AddDevice();
					
					if(nodeId!=-1){
					
					Device device= Device.createDevice(DeviceActivity.this);
					
					device.setIrCode(nodeId);
					device.setName(String.format("Device-%d", device.getIrCode()));
					
					/*
					 * save the device object.
					 */
					RemoteUi.getHandle().getChildren().add(device);
					XmlManager xmlManager = new XmlManager();
					xmlManager.saveData(RemoteUi.getHandle(),
							RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
									+ RemoteUi.UI_XML_FILE);

				}
			} catch (Exception ex) {
				nodeId = -1;
				ex.printStackTrace();
			}

			return nodeId;

		}

		@Override
		protected void onPreExecute() {
			DeviceActivity.this.showDialog(DeviceActivity.PROGRESS_DIALOG);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Integer result) {

		    DeviceActivity.this.removeDialog(DeviceActivity.PROGRESS_DIALOG);
			
			displayDevices();
		    
			if(result==-1){
				Toast.makeText(DeviceActivity.this, "Failed to add device!", Toast.LENGTH_SHORT).show();
			}else{
				
				Toast.makeText(DeviceActivity.this, "Add device successfully!", Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
	/*
	 * AsyncTask for App Initializing.
	 */
	private class DelDevTask extends
			android.os.AsyncTask<Integer, Integer, Integer> {

		private int nodeId=-1;
		/*
		 * compare the db version of remote.db and remote_temp.db.
		 * 
		 * use the latest db and copy the device info to latest db. cover
		 * current db file with the latest one.
		 */
		private void doDbUpdate() {
			return;
		}

		@Override
		protected Integer doInBackground(Integer... params) {

			nodeId = -1;
			try {
					nodeId = mZController.DelDevice();
					
					if(nodeId!=-1){
					
					/*
					 * save the device object.
					 */
					List<Device> devs=RemoteUi.getHandle().getChildren();
					
					int i=0;
					while(i<devs.size()){
					  Device dev=devs.get(i);
					  if(dev.getIrCode()==nodeId){
						  devs.remove(i);
					  }else{
						  i++;
					  }
					}
					
					XmlManager xmlManager = new XmlManager();
					xmlManager.saveData(RemoteUi.getHandle(),
							RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
									+ RemoteUi.UI_XML_FILE);

				}
			} catch (Exception ex) {
				nodeId = -1;
				ex.printStackTrace();
			}

			return nodeId;

		}

		@Override
		protected void onPreExecute() {
			DeviceActivity.this.showDialog(DeviceActivity.PROGRESS_DIALOG);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Integer result) {

		    DeviceActivity.this.removeDialog(DeviceActivity.PROGRESS_DIALOG);
			
			displayDevices();
		    
			if(result==-1){
				Toast.makeText(DeviceActivity.this, "Failed to delete device!", Toast.LENGTH_SHORT).show();
			}else{
				
				Toast.makeText(DeviceActivity.this, "Delete device successfully!", Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
}