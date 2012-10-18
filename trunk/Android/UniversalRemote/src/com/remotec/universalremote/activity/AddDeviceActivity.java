/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.common.FileManager;
import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;
import com.remotec.universalremote.activity.component.DeviceButton;
import com.remotec.universalremote.activity.component.KeyButton;
import com.remotec.universalremote.activity.component.RtArrayAdapter;
import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.Extender;
import com.remotec.universalremote.data.Key;
import com.remotec.universalremote.data.Key.Mode;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.data.Uird;
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
import android.os.Handler;
import android.os.Message;
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
public class AddDeviceActivity extends Activity {

	private static final int AutoSearchTimer = 3500;
	private static final int REQUEST_SELECT_ICON = 1;
	// Debugging Tags
	private static final String TAG = "AddDeviceActivity";
	private static final boolean D = false;

	private static final int MSG_TIMER = 1;

	private Spinner mSpinerCategory;
	private Spinner mSpinerManufacturer;
	private Spinner mSpinerModel;

	private RtArrayAdapter<String> mCategoryAdapter;
	private RtArrayAdapter<String> mManufacturerAdapter;
	private RtArrayAdapter<String> mCodeAdapter;

	private Button mBtnCancel;
	private Button mBtnFinish;
	private Button mBtnTest;
	private Button mBtnAutoSearch;

	private EditText mDeviceName; // name edittext

	private ImageView mDeviceIcon;
	
	private Timer mTimer = null;
	private TimerTask mTimeTask = null;

	// mark if the device is edit, then we should generate the key for it.
	private boolean mDeviceEditTag;
	private int mNextAutoPosition;

	enum eCurrentPage {
		eSelectDevice, eDeviceInfo
	};

	/*
	 * marks the current page.
	 */
	private eCurrentPage mCurPage;

	private Device mDevice;

	private AlertDialog mCancelDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_device_wizard);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setResult(RESULT_CANCELED);

		initControls();

		mCurPage = eCurrentPage.eSelectDevice;

		mTimer = new Timer();

		mDevice = Device.createDevice(this);

		mDeviceEditTag = true;

		updateControls();

	}

	/*
	 * Choose if want to exit.
	 */
	protected void cancelDialog() {

		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(AddDeviceActivity.this);

		builder.setMessage(R.string.cancel_adding_msg);

		builder.setTitle(R.string.cancel_adding_title);

		builder.setIcon(android.R.drawable.ic_dialog_alert);

		builder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						AddDeviceActivity.this.setResult(RESULT_CANCELED);
						AddDeviceActivity.this.finish();

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
	 * loads the manufacturer adapter with specific category.
	 */
	private void loadManufacturer(String category) {

		List<String> temp = RemoteUi.getHandle().getIrBrandMap().get(category);
		mManufacturerAdapter.setData(temp);
		mSpinerManufacturer.setAdapter(mManufacturerAdapter);

	}

	/*
	 * loads the manufacturer adapter with specific category.
	 */
	private void loadCodeNum(String category, String manufacturer) {

		DbManager dbm = new DbManager();
		Extender curExtender = RemoteUi.getHandle().getActiveExtender();

		List<String> temp = null;
		temp = dbm.getCodesList(category, manufacturer,
				curExtender.getSupportUirdLib());
		mCodeAdapter.setData(temp);
		mSpinerModel.setAdapter(mCodeAdapter);

		if (temp.size() == 0) {
			mDevice.setIrCode(-1);
		}

		/*
		 * set the device type id.
		 */
		setDeviceTypeId(dbm);

	}

	/*
	 * Get the device type id to transmit ir.
	 */
	private void setDeviceTypeId(DbManager dbm) {
		mDevice.setDeviceTypeId(dbm.getDevTypeIdByName(mDevice.getDeviceType()));
	}

	/*
	 * init the controls
	 */
	private void initControls() {

		mBtnCancel = (Button) findViewById(R.id.btn_footer_cancel);
		mBtnCancel.setOnClickListener(mOnCancelListener);

		mBtnFinish = (Button) findViewById(R.id.btn_footer_finish);
		mBtnFinish.setOnClickListener(mOnFinishListener);

		mBtnAutoSearch = (Button) findViewById(R.id.btn_autosearch);
		mBtnAutoSearch.setOnClickListener(mOnAutosearchListener);

		mBtnTest = (Button) findViewById(R.id.btn_test);
		mBtnTest.setOnClickListener(mOnTestListener);

		mDeviceName = (EditText) findViewById(R.id.name_edit);
		mDeviceName.addTextChangedListener(mDeviceNameWatcher);

		mDeviceIcon = (ImageView) findViewById(R.id.device_img);
		mDeviceIcon.setOnClickListener(mOnIconListener);

		mSpinerCategory = (Spinner) findViewById(R.id.spiner_category);
		mSpinerCategory.setOnItemSelectedListener(mCategoryListener);

		mSpinerManufacturer = (Spinner) findViewById(R.id.spiner_manufacturer);
		mSpinerManufacturer.setOnItemSelectedListener(mManufacturerListener);

		mSpinerModel = (Spinner) findViewById(R.id.spiner_model);
		mSpinerModel.setOnItemSelectedListener(mModelListener);

		// 将可选内容与ArrayAdapter连接起来
		mCategoryAdapter = new RtArrayAdapter<String>(this,
				R.layout.irremote_spinner, 0, RemoteUi.getHandle()
						.getCategoryList());
		mCategoryAdapter
				.setDropDownViewResource(R.layout.irremote_spinner_item);
		// 将adapter2 添加到spinner中
		mSpinerCategory.setAdapter(mCategoryAdapter);

		mManufacturerAdapter = new RtArrayAdapter<String>(this,
				R.layout.irremote_spinner, 0, RemoteUi.getHandle()
						.getIrBrandMap().get(mCategoryAdapter.getItem(0)));
		mManufacturerAdapter
				.setDropDownViewResource(R.layout.irremote_spinner_item);
		mSpinerManufacturer.setAdapter(mManufacturerAdapter);

		mCodeAdapter = new RtArrayAdapter<String>(this,
				R.layout.irremote_spinner, 0);
		mCodeAdapter.setDropDownViewResource(R.layout.irremote_spinner_item);
		mSpinerModel.setAdapter(mCodeAdapter);

	}

	/*
	 * handle the selection events of spinner Category
	 */
	private OnItemSelectedListener mCategoryListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			String s = mSpinerCategory.getSelectedItem().toString();
			loadManufacturer(s);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}

	};

	/*
	 * handle the selection events of spinner Manufacturer
	 */
	private OnItemSelectedListener mManufacturerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			String manufacturer = mSpinerManufacturer.getSelectedItem()
					.toString();
			String category = mSpinerCategory.getSelectedItem().toString();
			
			/* save data to the device object */
			mDevice.setName(category + "-" + manufacturer);
			mDevice.setDeviceType(category);
			
			loadCodeNum(category, manufacturer);


			mDevice.setManufacturer(manufacturer);
			
			updateControls();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}

	};

	/*
	 * handle the selection events of spinner Model
	 */
	private OnItemSelectedListener mModelListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			String codeNum = mSpinerModel.getSelectedItem().toString();

			if ((codeNum != null) && (codeNum.length() > 0)) {

				mDevice.setIrCode(Integer.parseInt(codeNum));
				mDeviceEditTag = true;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}

	};

	/*
	 * Deals cancel button click.
	 */
	private OnClickListener mOnCancelListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			cancelDialog();

		}

	};

	/*
	 * Deals finish button click.
	 */
	private OnClickListener mOnFinishListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			try {
				// generate the device key.
				if (AddDeviceActivity.this.mDeviceEditTag) {
					boolean result = setDeviceKeys(
							AddDeviceActivity.this.mDevice, RemoteUi
									.getHandle().getTemplateKeyMap());

					if (!result) {
						Toast.makeText(getApplicationContext(),
								R.string.please_try_again_, Toast.LENGTH_SHORT)
								.show();
						return;
					}
				}
				/*
				 * save the device object.
				 */
				RemoteUi.getHandle().getChildren().add(mDevice);
				XmlManager xmlManager = new XmlManager();
				xmlManager.saveData(RemoteUi.getHandle(),
						RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
								+ RemoteUi.UI_XML_FILE);

				AddDeviceActivity.this.setResult(Activity.RESULT_OK);
				AddDeviceActivity.this.finish();
			} catch (Exception ex) {

			}

		}
	};

	/*
	 * Deals finish button click.
	 */
	private OnClickListener mOnTestListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			testRemoteKeys();
		}
	};

	/*
	 * Sets the key layout to display device key
	 */
	private boolean setDeviceKeys(Device dev, Map<Integer, Key> map) {

		boolean result = false;
		// UIRD version Extender.
		if (RemoteUi.getHandle().getActiveExtender().getSupportUirdLib()) {
			result = setDeviceKeysUird(dev, map);
		} else {
			result = setDeviceKeysInternal(dev, map);
		}

		return result;
	}

	/*
	 * Sets the key layout to display device key（Internal Lib version）.
	 */
	private boolean setDeviceKeysInternal(Device dev, Map<Integer, Key> map) {

		/* get the valid key ids. */
		byte[] keyFlags = IrApi.getHandle().getKeyFlag(
				(byte) dev.getDeviceTypeId(), dev.getIrCode());

		if (keyFlags == null || keyFlags.length != 9) {
			return false;
		}

		dev.getChildren().clear();

		int k = 0;
		for (int i = 1; i < 9; i++) {
			byte keyFlag = keyFlags[i];
			for (int j = 7; j > -1; j--) {
				if (map.containsKey(k)) {
					Key newKey = map.get(k).colonel();

					if ((keyFlag & (0x01 << j)) != 0) {
						newKey.setVisible(true);
					} else {
						newKey.setVisible(false);
					}

					newKey.setMode(Mode.BuildIn);
					dev.getChildren().add(newKey);
				}
				k++;
			}
		}

		mDeviceEditTag = false;

		return true;
	}

	/*
	 * Sets the key layout to display device key（Uird Lib version）.
	 */
	private boolean setDeviceKeysUird(Device dev, Map<Integer, Key> map) {

		/* get the valid key ids. */
		DbManager dbm = new DbManager();

		List<Uird> keyList = dbm.getUirdData(dev.getDeviceTypeId(),
				dev.getIrCode());

		dev.getChildren().clear();

		for (int i = 0; i < 64; i++) {
			if (map.containsKey(i)) {
				Key newKey = map.get(i).colonel();
				Uird temp = null;
				for (Uird ud : keyList) {
					if (ud.getKeyId() == i) {
						temp = ud;
						break;
					}
				}
				if (temp != null) {
					newKey.setData(temp.getUirdData());
					newKey.setMode(Mode.UIRD);
					newKey.setVisible(true);
				} else {
					newKey.setVisible(false);
				}

				dev.getChildren().add(newKey);
			}
		}

		mDeviceEditTag = false;

		return true;
	}

	/*
	 * Deals imageview click.
	 */
	private OnClickListener mOnIconListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent addDeviceIntent = new Intent(AddDeviceActivity.this,
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/* skip back key */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cancelDialog();

			return super.onKeyDown(keyCode, event);	
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

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

	/*
	 * Deals Auto Search button click.
	 */
	private OnClickListener mOnAutosearchListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String sAutoSearch = getResources().getString(
					R.string.btn_autosearch);

			if (mTimer != null) {
				mTimer.cancel();
				// mTimeTask.cancel();
				mTimer = null;
			}

			if (mBtnAutoSearch.getText().equals(sAutoSearch))// not time was
																// init
			{
				displayPreAutosearchDlg();

			} else {
				mBtnAutoSearch.setText(sAutoSearch);
				
				testRemoteKeys();
			}
		}
	};

	// The Handler that gets information back from the timetask
	private Handler mTimeHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_TIMER: {

				if (mNextAutoPosition < mSpinerModel.getCount()) {

					mSpinerModel.setSelection(mNextAutoPosition);
					String btnText = String.format("%s(%d/%d)", getResources()
							.getString(R.string.btn_autosearch_stop),
							mNextAutoPosition + 1, mSpinerModel.getCount());
					mBtnAutoSearch.setText(btnText);

					if (!RemoteUi.getEmulatorTag()) {
						emitPowerKeyIR();
					}

					mNextAutoPosition++;
				} else {
					mBtnAutoSearch.setText(getResources().getString(
							R.string.btn_autosearch));
					mTimer.cancel();
					// mTimeTask.cancel();
					mTimer = null;
				}
			}
				break;
			}
		}
	};

	/*
	 * Emits key IR.
	 */
	private void emitPowerKeyIR() {

		IrApi irController = IrApi.getHandle();

		if (irController != null) {

			// UIRD version Extender.
			if (RemoteUi.getHandle().getActiveExtender().getSupportUirdLib()) {
				/* get the valid key ids. */
				DbManager dbm = new DbManager();

				Uird uird = dbm.getUirdData(mDevice.getDeviceTypeId(),
						mDevice.getIrCode(), 1);
				if (uird != null) {
					boolean result = irController.transmitIrData((byte) 0x81,
							uird.getUirdData());
				}
			} else {
				boolean result = irController.transmitPreprogramedCode(
						(byte) 0x81, (byte) mDevice.getDeviceTypeId(),
						mDevice.getIrCode(), (byte) 1);
			}

		}
	}

	/*
	 * displays the prepare Auto search dialog.
	 */
	private void displayPreAutosearchDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle(R.string.auto_search_title);
		builder.setMessage(R.string.auto_search_comment);

//		ViewGroup vg = (ViewGroup) this.getLayoutInflater().inflate(
//				R.layout.auto_search_key_select_dialog, null);
//
//		builder.setView(vg);
//		
//		Spinner sp=(Spinner)vg.findViewById(R.id.spinner_auto_search);
		
		builder.setPositiveButton(android.R.string.search_go,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
						startAutoSearch();
					}
				});

		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});

		builder.create().show();
	}

	/*
	 * start time for autosearch
	 */
	private void startAutoSearch() {

		mNextAutoPosition=mSpinerModel.getSelectedItemPosition();
		mTimer = new Timer();
		mTimeTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = mTimeHandle.obtainMessage(MSG_TIMER);
				mTimeHandle.sendMessage(msg);
			}

		};
		mTimer.schedule(mTimeTask, 0, AutoSearchTimer);
	}

	private void testRemoteKeys() {
		// generate the device key.
		if (AddDeviceActivity.this.mDeviceEditTag) {
			boolean result = setDeviceKeys(AddDeviceActivity.this.mDevice,
					RemoteUi.getHandle().getTemplateKeyMap());
			if (!result) {
				Toast.makeText(getApplicationContext(),
						R.string.please_try_again_, Toast.LENGTH_SHORT)
						.show();

				return;
			}
		}

		/* crate a intent object, then call the device activity class */
		Intent devKeyIntent = new Intent(AddDeviceActivity.this,
				AcDeviceKeyActivity.class);
		devKeyIntent.putExtra(AcDeviceKeyActivity.ACTIVITY_MODE,
				AcDeviceKeyActivity.ACTIVITY_CONTROL);
		RemoteUi.getHandle().setActiveDevice(mDevice);
		startActivity(devKeyIntent);
		
//		/* crate a intent object, then call the device activity class */
//		Intent devKeyIntent = new Intent(AddDeviceActivity.this,
//				AvDeviceKeyActivity.class);
//		devKeyIntent.putExtra(AvDeviceKeyActivity.ACTIVITY_MODE,
//				AvDeviceKeyActivity.ACTIVITY_CONTROL);
//		RemoteUi.getHandle().setActiveDevice(mDevice);
//		startActivity(devKeyIntent);
	}

}