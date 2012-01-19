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
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class CodelibListActivity extends Activity implements OnClickListener {
	// Debugging
	private static final String TAG = "CodelibListActivity";
	private static final boolean D = false;

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
	private Button updateButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.codelib_list);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		mmIrController = IrApi.getHandle();

		// Initialize the button to perform device discovery
		updateButton = (Button) findViewById(R.id.button_update);
		updateButton.setOnClickListener(this);

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mCodelibArrayAdapter = new ArrayAdapter<String>(this,
				R.drawable.listview_layout); 
 
		mSelectedIndex = -1;

		if (mSelectedIndex == -1) {
			updateButton.setEnabled(false);
		}

		// Find and set up the ListView for paired devices
		codelibListView = (ListView) findViewById(R.id.codelib_list);
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

		// If there are code library files, add each one to the ArrayAdapter
		if (mFiles.size() > 0) {
			findViewById(R.id.title_codelib_files).setVisibility(View.VISIBLE);
			for (File f : mFiles) {
				mCodelibArrayAdapter.add(f.getName().substring(0,
						f.getName().length() - 5));
			}
		} else {
			findViewById(R.id.title_codelib_files)
					.setVisibility(View.INVISIBLE);
			String noDevices = getResources().getText(
					R.string.none_codelib_file).toString();
			mCodelibArrayAdapter.add(noDevices);
		}
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

		// Make sure we're not doing discovery anymore
		// if (mBtAdapter != null) {
		// mBtAdapter.cancelDiscovery();
		// }

		// Unregister broadcast listeners
		// this.unregisterReceiver(mReceiver);
	}

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

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

			if(mFiles.size()>0)
			{
//			 codelibListView.setSelection(arg2);
			 v.setSelected(true);
			 mSelectedIndex = arg2;
			 updateButton.setEnabled(mSelectedIndex != -1);
			}
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		v.setClickable(false);
		boolean result=doUpdate();
		v.setClickable(true);
		
		if(result)
		{
			setResult(Activity.RESULT_OK);
			this.finish();
		}
	}

}
