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
public class AddDeviceActivity extends Activity {

	private static final int REQUEST_SELECT_ICON = 1;
	// Debugging Tags
	private static final String TAG = "AddDeviceActivity";
	private static final boolean D = false;

	private Spinner mSpinerCategory;
	private Spinner mSpinerManufacturer;
	private Spinner mSpinerModel;

	private RtArrayAdapter<String> mCategoryAdapter;
	private RtArrayAdapter<String> mManufacturerAdapter;
	private RtArrayAdapter<String> mCodeAdapter;

	private ViewGroup mVgSelectDevice;
	private ViewGroup mVgDeviceInfo;

	private Button mBtnCancel;
	private Button mBtnNext;
	private Button mBtnBack;
	private Button mBtnFinish;
	private Button mBtnTest;

	private EditText mDeviceName; // name edittext

	private ImageView mDeviceIcon;

	private TextView mCategory;
	private TextView mManufacturer;
	private TextView mCodeNum;
	
	//mark if the device is edit, then we should generate the key for it.
	private boolean mDeviceEditTag;

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

		mDevice = Device.createDevice(this);

		mDeviceEditTag=true;
		
		updateControls();

	}

	// init the ui for add device.
	void initCurrentPage(eCurrentPage curPage) {
		mCurPage = curPage;
		if (curPage == eCurrentPage.eSelectDevice) {
			mBtnNext.setVisibility(View.VISIBLE);
			mBtnFinish.setVisibility(View.INVISIBLE);
			mBtnBack.setVisibility(View.INVISIBLE);
			mVgSelectDevice.setVisibility(View.VISIBLE);
			mVgDeviceInfo.setVisibility(View.INVISIBLE);
		} else if (curPage == eCurrentPage.eDeviceInfo) {

			mBtnNext.setVisibility(View.INVISIBLE);
			mBtnFinish.setVisibility(View.VISIBLE);
			mBtnBack.setVisibility(View.VISIBLE);
			mVgSelectDevice.setVisibility(View.INVISIBLE);
			mVgDeviceInfo.setVisibility(View.VISIBLE);

			InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mDeviceName.getWindowToken(), 0);
		}
	}

	/*
	 * Choose if want to exit.
	 */
	protected void cancelDialog() {

		/*build a dialog, ask if want to close*/
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
		List<String> temp = dbm.getCodesList(category, manufacturer);
		mCodeAdapter.setData(temp);
		mSpinerModel.setAdapter(mCodeAdapter);
		
		/*
		 * set the device type id.
		 */
		setDeviceTypeId(dbm);

	}
	
	/*
	 * Get the device type id to transmit ir.
	 */
	private void setDeviceTypeId(DbManager dbm)
	{
		mDevice.setDeviceTypeId(dbm.getDevTypeIdByName(mDevice.getDeviceType()));
	}

	/*
	 * init the controls
	 */
	private void initControls() {

		mVgSelectDevice = (ViewGroup) findViewById(R.id.select_ircode);
		mVgDeviceInfo = (ViewGroup) findViewById(R.id.device_info);

		mBtnCancel = (Button) findViewById(R.id.btn_footer_cancel);
		mBtnCancel.setOnClickListener(mOnCancelListener);

		mBtnNext = (Button) findViewById(R.id.btn_footer_next);
		mBtnNext.setOnClickListener(mOnNextListener);

		mBtnFinish = (Button) findViewById(R.id.btn_footer_finish);
		mBtnFinish.setOnClickListener(mOnFinishListener);

		mBtnBack = (Button) findViewById(R.id.btn_footer_back);
		mBtnBack.setOnClickListener(mOnBackListener);

		mBtnTest = (Button) findViewById(R.id.btn_test);
		mBtnTest.setOnClickListener(mOnTestListener);

		mDeviceName = (EditText) findViewById(R.id.name_edit);
		mDeviceName.addTextChangedListener(mDeviceNameWatcher);
		mCategory = (TextView) findViewById(R.id.category_textview);
		mManufacturer = (TextView) findViewById(R.id.manufacturer_textview);
		mCodeNum = (TextView) findViewById(R.id.model_textview);

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
			loadCodeNum(category, manufacturer);
			
			/*save data to the device object*/
			mDevice.setName(category+"-"+manufacturer);
			mDevice.setDeviceType(category);
			mDevice.setManufacturer(manufacturer);
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
				mDeviceEditTag=true;
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
	 * Deals back button click.
	 */
	private OnClickListener mOnBackListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AddDeviceActivity.this.initCurrentPage(eCurrentPage.eSelectDevice);
		}
	};

	/*
	 * Deals next button click.
	 */
	private OnClickListener mOnNextListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			updateControls();
			AddDeviceActivity.this.initCurrentPage(eCurrentPage.eDeviceInfo);
		}
	};

	/*
	 * Deals finish button click.
	 */
	private OnClickListener mOnFinishListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			try {
				//generate the device key.
				if(AddDeviceActivity.this.mDeviceEditTag)
				{
					boolean result=setDeviceKeys(AddDeviceActivity.this.mDevice, RemoteUi.getHandle()
										.getTemplateKeyMap());
					
					if(!result){
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
			
			//generate the device key.
			if(AddDeviceActivity.this.mDeviceEditTag)
			{
				boolean result=setDeviceKeys(AddDeviceActivity.this.mDevice, RemoteUi.getHandle()
						.getTemplateKeyMap());
				if(!result){
					Toast.makeText(getApplicationContext(),
							R.string.please_try_again_, Toast.LENGTH_SHORT)
							.show();
					
					return;
				}
			}

			/* crate a intent object, then call the device activity class */
			Intent devKeyIntent = new Intent(AddDeviceActivity.this,
					DeviceKeyActivity.class);
		    RemoteUi.getHandle().setActiveDevice(mDevice);
			startActivity(devKeyIntent);
		}
	};

	/*
	 * Sets the key layout to display device key.
	 */
	private boolean setDeviceKeys(Device dev, Map<Integer, Key> map) {
			
		/*get the valid key ids.*/
		byte[] keyFlags =  IrApi.getHandle().
		        getKeyFlag((byte)dev.getDeviceTypeId(),dev.getIrCode()/10);

        if(keyFlags==null||keyFlags.length!=9){
        	return false;
        }
		
		dev.getChildren().clear();

		int k = 0;
		for (int i = 1; i < 9; i++) {
			byte keyFlag = keyFlags[i];
			for (int j = 7; j >-1; j--) {
				if (map.containsKey(k)) {
					Key newKey = map.get(k).colonel();

					if ((keyFlag & (0x01 << j)) != 0) {
						newKey.setVisible(true);
					} else {
						newKey.setVisible(false);
					}

					dev.getChildren().add(newKey);
				}
				k++;
			}
		}
		
		mDeviceEditTag=false;
		
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