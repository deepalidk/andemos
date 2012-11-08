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

package com.remote.universalremote.activity;

import java.net.Socket;
import java.util.Set;

import com.remote.universalremote.data.RemoteUi;
import com.remote.universalremote.activity.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android_serialport_api.SerialPortFinder;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class BtDeviceListActivity extends Activity {
	// Debugging
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = false;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	private int mThreadCount;

	private static final int MESSAGE_WIFI_ADDRESS = 1;
	private static final int MESSAGE_SERIALPORT_ADDRESS = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.bt_device_list);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.bt_device_list_item);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.bt_device_list_item);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		if (RemoteUi.communicationMode()==RemoteUi.BT_MODE) {
			SetupBtDiscover();
		} else if (RemoteUi.communicationMode()==RemoteUi.WIFI_MODE){
			setProgressBarIndeterminateVisibility(true);
            scanButton.setVisibility(View.GONE);
            newDevicesListView.setVisibility(View.GONE);
            findViewById(R.id.title_paired_devices).setVisibility(View.GONE);
            findViewById(R.id.title_new_devices).setVisibility(View.GONE);
			discoverWifiDevice();
		}else{
			discoverSerialDevice();
		}
	}

	private void SetupBtDiscover() {
		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				if(device.getName().contains("BXT")){
					mPairedDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired)
					.toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(RemoteUi.communicationMode()==RemoteUi.BT_MODE){
			// Make sure we're not doing discovery anymore
			if (mBtAdapter != null) {
				mBtAdapter.cancelDiscovery();
			}
	
			// Unregister broadcast listeners
			this.unregisterReceiver(mReceiver);
		}
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		// Turn on sub-title for new devices
		findViewById(R.id.new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			if (RemoteUi.communicationMode()==RemoteUi.BT_MODE) {
				// Cancel discovery because it's costly and we're about to
				// connect
				mBtAdapter.cancelDiscovery();
			}

			Intent intent = new Intent(); // 申请Bundle变量

			try {
				// Get the device MAC address, which is the last 17 chars in the
				// View
				String info = ((TextView) v).getText().toString();
				String address="";
				
				address = info.substring(info.lastIndexOf('\n')+1,info.length()).trim();

				intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
				BtDeviceListActivity.this.setResult(Activity.RESULT_OK, intent);
				BtDeviceListActivity.this.finish();
			} catch (Exception ex) {

			}

		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if(device.getName().contains("BXT")){
						mNewDevicesArrayAdapter.add(device.getName() + "\n"
								+ device.getAddress());
					}
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

	// The Handler that gets information back from the BluetoothRemoteService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_WIFI_ADDRESS: {
				if (--mThreadCount == 0) {// last Ip.
					setProgressBarIndeterminateVisibility(false);
				}

				mPairedDevicesArrayAdapter.add("Remotec Wifi Extender" + "\n"
						+ msg.getData().getString(EXTRA_DEVICE_ADDRESS));
			}
				break;

			}
		}
	};

	// get local IP Address
	public String getWifiIpAddress() {

		// 获取wifi服务
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// 判断wifi是否开启
		if (!wifiManager.isWifiEnabled()) {

			wifiManager.setWifiEnabled(true);

		}

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();

		int ipAddress = wifiInfo.getIpAddress();

		String ip = intToIp(ipAddress);

		return ip;
	}

	// convert int ip addr to String;
	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	// discover the device in local net work.
	private void discoverWifiDevice() {
		// get local area net ip address
		String strLocalIp = getWifiIpAddress();

		int index = strLocalIp.lastIndexOf('.');
		// get xx.xx.xx.
		String strNetwork = strLocalIp.substring(0, index+1);

		// init mThread counter.
		mThreadCount = 255 - 1;

		for (int i = 1; i < 255; i++) {
			// combine "xx.xx.xx."+ "xx"
			String ip = strNetwork + i;
			DiscoverWifiDeviceThread Thread = new DiscoverWifiDeviceThread(ip,
					mHandler);
			Thread.setName("Check IP:"+ ip);
			Thread.start();
		}

	}
	

	//check if an device ip has an extender listening services.
	public class DiscoverWifiDeviceThread extends Thread {
		private String mIpAddress;
		private Handler mHandle;

		public DiscoverWifiDeviceThread(String ipAddress, Handler handle) {
			mIpAddress = ipAddress;
			mHandle = handle;
		}

		public void run() {

			try {
				Socket socket = new Socket(mIpAddress, 0x4000); //default 0x4000
				Message msg = mHandle.obtainMessage(MESSAGE_WIFI_ADDRESS);
				socket.close();
				msg.getData().putString(EXTRA_DEVICE_ADDRESS, mIpAddress);
				msg.sendToTarget();
			} catch (Exception e) {
				System.out.println(mIpAddress + " is not an Extender");
			}
		}

	}
	
	// discover the device in local net work.
	private void discoverSerialDevice() {
		// get local area net ip address
        SerialPortFinder serialPortFinder=new SerialPortFinder();

		for (String path:serialPortFinder.getAllDevicesPath()) {
			
			mPairedDevicesArrayAdapter.add("Remotec Extender" + "\n" + path);
		}

	}

}
