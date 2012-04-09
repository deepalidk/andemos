/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.common.FileManager;
import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;
import com.remotec.universalremote.activity.component.DeviceButton;
import com.remotec.universalremote.activity.component.RtArrayAdapter;
import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.Extender;
import com.remotec.universalremote.data.Key;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.irapi.IrApi;
import com.remotec.universalremote.persistence.DbManager;
import com.remotec.universalremote.persistence.XmlManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/*
 *Displays device for UI.
 */
public class EditDeviceActivity extends Activity {

	private static final int REQUEST_SELECT_ICON = 1;
	// Debugging Tags
	private static final String TAG = "AddDeviceActivity";
	private static final boolean D = false;

	private ViewGroup mVgDeviceInfo;

	private Button mBtnEditButtons;
	private Button mBtnDone;


	private EditText mDeviceName; // name edittext

	private ImageView mDeviceIcon;

	private TextView mCategory;
	private TextView mManufacturer;
	private TextView mCodeNum;
	
	private Device mDevice;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_device);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setResult(RESULT_CANCELED);

		initControls();


		mDevice = RemoteUi.getHandle().getActiveDevice();
		
		updateControls();

	}


	/*
	 * init the controls
	 */
	private void initControls() {

		mVgDeviceInfo = (ViewGroup) findViewById(R.id.device_info);

		mBtnDone = (Button) findViewById(R.id.btn_footer_done);
		mBtnDone.setOnClickListener(mOnDoneListener);

		mBtnEditButtons = (Button) findViewById(R.id.btn_edit_buttons);
		mBtnEditButtons.setOnClickListener(mOnEditButtonsListener);

		mDeviceName = (EditText) findViewById(R.id.name_edit);
		mDeviceName.addTextChangedListener(mDeviceNameWatcher);
		mCategory = (TextView) findViewById(R.id.category_textview);
		mManufacturer = (TextView) findViewById(R.id.manufacturer_textview);
		mCodeNum = (TextView) findViewById(R.id.model_textview);

		mDeviceIcon = (ImageView) findViewById(R.id.device_img);
		mDeviceIcon.setOnClickListener(mOnIconListener);

	}


	/*
	 * Deals finish button click.
	 */
	private OnClickListener mOnDoneListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			try {
				XmlManager xmlManager = new XmlManager();
				xmlManager.saveData(RemoteUi.getHandle(),
						RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
								+ RemoteUi.UI_XML_FILE);

				EditDeviceActivity.this.setResult(Activity.RESULT_OK);
				EditDeviceActivity.this.finish();
			} catch (Exception ex) {

			}

		}
	};

	/*
	 * Deals finish button click.
	 */
	private OnClickListener mOnEditButtonsListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			/* crate a intent object, then call the device activity class */
			Intent devKeyIntent = new Intent(EditDeviceActivity.this,
					DeviceKeyActivity.class);
		    RemoteUi.getHandle().setActiveDevice(mDevice);
			startActivity(devKeyIntent);
		}
	};

	/*
	 * Deals imageview click.
	 */
	private OnClickListener mOnIconListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent addDeviceIntent = new Intent(EditDeviceActivity.this,
					SelectIconDialog.class);
			startActivityForResult(addDeviceIntent, REQUEST_SELECT_ICON);
		}
	};

	/*
	 * Catch the text change event.
	 */
	private TextWatcher mDeviceNameWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			mDevice.setName(s.toString());
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

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
		case REQUEST_SELECT_ICON:

			if (resultCode == Activity.RESULT_OK) {
				Integer resId = data.getIntExtra(SelectIconDialog.IMAGE_RES_ID,
						-1);
				// the resId is the small icon picture id. now we change it to
				// the large icon id.

				// set res id.
				mDevice.setIconResId(getLargeIconId(resId, this));
				// set res name.
				mDevice.setIconName(this.getResources().getResourceName(
						mDevice.getIconResId()));

				updateControls();
			}
			break;
		}
	}

	/*
	 * updates the controls
	 */
	void updateControls() {
		mDeviceIcon.setImageResource(mDevice.getIconResId());
		mDeviceName.setText(mDevice.getName());
		mCategory.setText(mDevice.getDeviceType());
		mManufacturer.setText(mDevice.getManufacturer());
		mCodeNum.setText(mDevice.getIrCode() + "");
	}

	/*
	 * the device picture has a large form (picturename.png) and a small form
	 * (picturename_s.png) we use the small icon id to get the large icon id.
	 */
	public static int getLargeIconId(int resId, Context context) {
		int result = 0;

		String pictureName = context.getResources().getResourceName(resId);

		pictureName = pictureName.substring(0, pictureName.length() - 2);

		result = context.getResources().getIdentifier(pictureName, "drawable",
				context.getApplicationInfo().packageName);

		return result;
	}

}