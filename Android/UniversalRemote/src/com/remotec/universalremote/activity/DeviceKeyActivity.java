/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.common.FileManager;
import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.component.BottomBarButton;
import com.remotec.universalremote.activity.component.DeviceButton;
import com.remotec.universalremote.activity.component.KeyButton;
import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.Key;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.irapi.BtConnectionManager;
import com.remotec.universalremote.irapi.IrApi;
import com.remotec.universalremote.persistence.XmlManager;

/*
 *Displays device key for UI. 
 */
public class DeviceKeyActivity extends Activity {

	// Debugging Tags
	private static final String TAG = "DeviceKeyActivity";
	private static final boolean D = false;

	// the activit mode of key layout activity
	public static final String ACTIVITY_MODE = "ACTIVITY_MODE";
	public static final int ACTIVITY_CONTROL = 0;
	public static final int ACTIVITY_EDIT = 1;

	// dialog ids
	private static final int PROGRESS_DIALOG = 0;

	private Device mDevice;

	private ProgressDialog mProgressDialog;

	/*
	 * all bottom Bar buttons in key layout
	 */
	private List<BottomBarButton> mBottomBarButtonList = null;

	/*
	 * all key buttons in key layout
	 */
	private Map<Integer, KeyButton> mKeyButtonMap = null;

	// Control key View Group
	private ViewGroup mVgControl = null;

	// Menu key View Group
	private ViewGroup mVgMenu = null;

	// Media key View Group
	private ViewGroup mVgMedia = null;

	// Title
	private TextView mTitleLeft = null;

	// Textview for vol key.
	private TextView mVolLabel = null;

	// Textview for ch key.
	private TextView mChLabel = null;

	// Textview for br key
	private TextView mBrLabel = null;

	private int mActivityMode = ACTIVITY_CONTROL;

	private enum EditKeyType {
		label_key_visible, 
		label_key_invisible, 
		icon_key_visible, 
		icon_key_invisible, 
		key_error
	};

	// mark to determine how to edit key.
	private EditKeyType mCurEditKeyType;

	// store the cur edit key, for edit.
	private KeyButton mCurActiveKey;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.devicekey);

		mActivityMode = getIntent()
				.getIntExtra(ACTIVITY_MODE, ACTIVITY_CONTROL);
		// Initializing data.
		InitAppTask initor = new InitAppTask();
		initor.execute(0);

	}

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

	/*
	 * inits the data for device activity
	 */
	private void initData() {

		Bundle bdl = getIntent().getExtras();

		/*
		 * global current active device store in RemoteUi.
		 */
		mDevice = RemoteUi.getHandle().getActiveDevice();

		mTitleLeft = (TextView) this
				.findViewById(R.id.devicekey_title_left_text);
		mTitleLeft.setText(mDevice.getName());

		mChLabel = (TextView) findViewById(R.id.key_id_ch);
		mVolLabel = (TextView) findViewById(R.id.key_id_vol);
		mBrLabel = (TextView) findViewById(R.id.key_id_br);

		mVgControl = (ViewGroup) this.findViewById(R.id.id_key_control_layout);
		mVgMenu = (ViewGroup) this.findViewById(R.id.id_key_menu_layout);
		mVgMedia = (ViewGroup) this.findViewById(R.id.id_key_media_layout);

		/*
		 * finds all BottomBar Buttons
		 */
		mBottomBarButtonList = new ArrayList<BottomBarButton>();

		ViewGroup vg = (ViewGroup) findViewById(R.id.id_key_bottombar);

		findBottomBarButtons(vg, mBottomBarButtonList,
				mBottomBarOnClickListener);

		/*
		 * finds all key Buttons
		 */
		mKeyButtonMap = new Hashtable<Integer, KeyButton>();

		vg = (ViewGroup) findViewById(R.id.id_key_layout);

		findKeyButtons(vg, mKeyButtonMap, mKeyOnClickListener);

	}

	/*
	 * finds all BottomBar Buttons
	 */
	private void findBottomBarButtons(ViewGroup vg,
			List<BottomBarButton> bList, OnClickListener listener) {

		for (int i = 0; i < vg.getChildCount(); i++) {
			View v = vg.getChildAt(i);

			if (v instanceof BottomBarButton) {
				bList.add((BottomBarButton) v);
				v.setOnClickListener(listener);
			} else if (v instanceof ViewGroup) {
				findBottomBarButtons((ViewGroup) v, bList, listener);
			}
		}
	}

	/*
	 * finds all key Buttons
	 */
	public static void findKeyButtons(ViewGroup vg,
			Map<Integer, KeyButton> bMap, OnClickListener listener) {

		for (int i = 0; i < vg.getChildCount(); i++) {
			View v = vg.getChildAt(i);

			if (v instanceof KeyButton) {
				KeyButton btn = (KeyButton) v;
				if (btn.getKeyId() != -1) {
					bMap.put(btn.getKeyId(), btn);
					v.setOnClickListener(listener);
				}
			} else if (v instanceof ViewGroup) {
				findKeyButtons((ViewGroup) v, bMap, listener);
			}
		}
	}

	/*
	 * bottom bar click listener.
	 */
	private OnClickListener mBottomBarOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			/*
			 * change the btn state, and so the background will change.
			 */
			for (Button btn : DeviceKeyActivity.this.mBottomBarButtonList) {
				btn.setEnabled(true);
			}

			v.setEnabled(false);

			if (v.getId() == R.id.btn_bottombar_control) {
				mVgControl.setVisibility(View.VISIBLE);
				mVgMenu.setVisibility(View.GONE);
				mVgMedia.setVisibility(View.GONE);
			} else if (v.getId() == R.id.btn_bottombar_menu) {
				mVgControl.setVisibility(View.GONE);
				mVgMenu.setVisibility(View.VISIBLE);
				mVgMedia.setVisibility(View.GONE);
			} else if (v.getId() == R.id.btn_bottombar_media) {
				mVgControl.setVisibility(View.GONE);
				mVgMenu.setVisibility(View.GONE);
				mVgMedia.setVisibility(View.VISIBLE);
			}

		}

	};

	/*
	 * Key click listener.
	 */
	private OnClickListener mKeyOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (RemoteUi.getEmulatorTag())
				return;

			mCurActiveKey = (KeyButton) v;

			if (mActivityMode == ACTIVITY_CONTROL) {
				emitKeyIR(mCurActiveKey);
			} else if (mActivityMode == ACTIVITY_EDIT) {
				showKeyEditMenu(mCurActiveKey);
			}
		}

	};
	private EditText mLabelEdit;


	/*
	 * displays the devices on the screen.
	 */
	private void displayKeys() {

		List<Key> keyList = mDevice.getChildren();

		for (KeyButton keyBtn : this.mKeyButtonMap.values()) {
			keyBtn.setVisibility(View.VISIBLE);
			keyBtn.setDuplicateParentStateEnabled(false);
		}

		for (Key key : keyList) {

			if (mKeyButtonMap.containsKey(key.getKeyId())) {

				KeyButton keyBtn = mKeyButtonMap.get(key.getKeyId());

				// set button text, notice that icon button will not be set
				// text.
				if (!keyBtn.getIsIconButton()) {
					keyBtn.setText(key.getText());
				}

				if (mActivityMode == ACTIVITY_CONTROL) {
					if (key.getVisible()) {
						keyBtn.setVisibility(View.VISIBLE);
					} else {
						keyBtn.setVisibility(View.INVISIBLE);
					}
				} else if (mActivityMode == ACTIVITY_EDIT) {// all key will be
															// display in Edit
															// mode.
					keyBtn.setVisibility(View.VISIBLE);
				}

				keyBtn.setTag(key);
			}

		}

		displayLabel();
	}

	/*
	 * set the ch , vol, br label
	 */
	private void displayLabel() {
		KeyButton tempAddBtn;
		KeyButton tempMinusBtn;
		Key tempKey;

		/* set void label */
		tempAddBtn = mKeyButtonMap.get(getResources().getInteger(
				R.integer.key_id_vol_up));
		tempMinusBtn = mKeyButtonMap.get(getResources().getInteger(
				R.integer.key_id_vol_up));

		// any one is visible then label is visible
		if (tempAddBtn.getVisibility() == View.VISIBLE
				|| tempMinusBtn.getVisibility() == View.VISIBLE) {
			tempKey = (Key) tempAddBtn.getTag();
			mVolLabel.setText(tempKey.getText());
			mVolLabel.setVisibility(View.VISIBLE);
		} else {
			mVolLabel.setVisibility(View.INVISIBLE);
		}

		/* set ch label */
		tempAddBtn = mKeyButtonMap.get(getResources().getInteger(
				R.integer.key_id_ch_up));
		tempMinusBtn = mKeyButtonMap.get(getResources().getInteger(
				R.integer.key_id_ch_up));

		// any one is visible then label is visible
		if (tempAddBtn.getVisibility() == View.VISIBLE
				|| tempMinusBtn.getVisibility() == View.VISIBLE) {
			tempKey = (Key) tempAddBtn.getTag();
			mChLabel.setText(tempKey.getText());
		} else {
			mChLabel.setVisibility(View.INVISIBLE);
		}

		/* set void label */
		tempAddBtn = mKeyButtonMap.get(getResources().getInteger(
				R.integer.key_id_br_up));
		tempMinusBtn = mKeyButtonMap.get(getResources().getInteger(
				R.integer.key_id_br_down));

		// any one is visible then label is visible
		if (tempAddBtn.getVisibility() == View.VISIBLE
				|| tempMinusBtn.getVisibility() == View.VISIBLE) {
			tempKey = (Key) tempAddBtn.getTag();
			mBrLabel.setText(tempKey.getText());
		} else {
			mBrLabel.setVisibility(View.INVISIBLE);
		}

	}

	/*
	 * Emits key IR.
	 */
	private void emitKeyIR(KeyButton keyBtn) {
		IrApi irController = IrApi.getHandle();

		if (irController != null) {
			Key tempKey = (Key) keyBtn.getTag();

			if (tempKey != null) {
//				boolean result = irController.transmitPreprogramedCode(
//						(byte) 0x81, (byte) (mDevice.getIrCode() % 10),
//						mDevice.getIrCode() / 10, (byte) tempKey.getKeyId());
				String data="04032202000800000110001135003202B308190DD4D8092BAE00C74AAE06807F418D2323240032323317312324012432378D232300000000000000000000000000000000000000000000000000000000";
				boolean result=irController.transmitIrData((byte) 0x81,data);
				if(D)
		          Log.d(TAG, ""+result);
			}
		}
	}

	/*
	 * provide key labels and commands
	 */
	private void showKeyEditMenu(KeyButton keyBtn) {
		
		mCurEditKeyType=getEditKeyType(keyBtn);
		
		if(mCurEditKeyType==EditKeyType.icon_key_invisible){
			//To do start learn activity
			displayLearnDlg();
		}else{
			displayEditChoiceMenu(mCurEditKeyType);
		}	
	}
	
	/*
	 * display a choice menu
	 */
	private void displayEditChoiceMenu(EditKeyType type) {
		AlertDialog dlg;
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.edit_buttons);

		int resStringArray=this.getMenuResource(type);
		// 设置可供选择的ListView
		builder.setItems(resStringArray,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						menuItemSelected(which);
						dialog.dismiss();
					}
				});

		dlg = builder.create();

		dlg.show();
	}
    
	/*
	 * a menu is selected
	 */
	private void menuItemSelected(int which){
		
		switch(mCurEditKeyType){
		case label_key_visible:
			visibleLabelKeySelected(which);
			break;
		case label_key_invisible:
			invisibleLabelKeySelected(which);
			break;
			
		case icon_key_visible:
			visibleIconKeySelected(which);
			break;
		}
		
	}
	
	/*
	 * a visible label key menu item seleted.
	 */
	private void visibleLabelKeySelected(int which){
		switch(which)
		{
			case 0: //learn
				displayLearnDlg();
				break;
			case 1://delete
				displayDeleteConfirmDlg();
				break;
			case 2://edit
				displayEditLabelDlg();
				break;
		}
	}
	
	/*
	 * a visible icon key menu item seleted.
	 */
	private void visibleIconKeySelected(int which){
		switch(which)
		{
			case 0: //learn
				displayLearnDlg();
				break;
			case 1://delete
				displayDeleteConfirmDlg();
				break;
		}
	}
	
	/*
	 * a visible label key menu item seleted.
	 */
	private void invisibleLabelKeySelected(int which){
		switch(which)
		{
			case 0: //learn
				displayLearnDlg();
				break;
			case 1://edit
				displayEditLabelDlg();
				break;
		}
	}
	
	/*
	 * displays the delete confirm dialog.
	 */
	private void displayEditLabelDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle(R.string.edit_key_label);
		
		mLabelEdit=new EditText(this);
		
		Key key=(Key)mCurActiveKey.getTag();
		mLabelEdit.setText(key.getText());
		
		builder.setView(mLabelEdit);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Key key=(Key)mCurActiveKey.getTag();
						String text=mLabelEdit.getText().toString();
						key.setText(text);
					     
						/*
						 * save the data.
						 */
						XmlManager xmlManager = new XmlManager();
						xmlManager.saveData(RemoteUi.getHandle(),
								RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
										+ RemoteUi.UI_XML_FILE);

						dialog.dismiss();
						displayKeys();
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
	 * displays the learn dialog.
	 */
	private void displayLearnDlg(){
		Toast.makeText(getApplicationContext(),
				"Learn function to be implemented later!", Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * displays the delete confirm dialog.
	 */
	private void displayDeleteConfirmDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setMessage(R.string.remove_key_message);

		builder.setTitle(R.string.remove_key_title);

		builder.setIcon(android.R.drawable.ic_dialog_alert);

		builder.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Key key=(Key)mCurActiveKey.getTag();
						key.setVisible(false);
					     
						/*
						 * save the data.
						 */
						XmlManager xmlManager = new XmlManager();
						xmlManager.saveData(RemoteUi.getHandle(),
								RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
										+ RemoteUi.UI_XML_FILE);

						dialog.dismiss();
						displayKeys();
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
	 * get menu resource from edit type.
	 */
	private int getMenuResource(EditKeyType type){
		int resResult=-1;
		switch(type)
		{
		case icon_key_visible:
			resResult=R.array.menu_icon_key_visible;
			break;
		case label_key_invisible:
			resResult=R.array.menu_label_key_invisible;
			break;
		case label_key_visible:
			resResult=R.array.menu_label_key_visible;
			break;
		}
		
		return resResult;
	}
	
	/*
	 *Gets edit key type from keyBtn 
	 */
	private EditKeyType getEditKeyType(KeyButton keyBtn) {
		EditKeyType result = EditKeyType.key_error;

		Key key = (Key) keyBtn.getTag();

		if (key != null) {  //if no key object, it's a error key.
			
			if (key.getVisible() == true) {
				if (keyBtn.getIsIconButton()) {
					result=EditKeyType.icon_key_visible;
				} else {
					result=EditKeyType.label_key_visible;
				}
			} else {
				if (keyBtn.getIsIconButton()) {
					result=EditKeyType.icon_key_invisible;
				} else {
					result=EditKeyType.label_key_invisible;
				}
			}
		}

		return result;
	}

	/*
	 * AsyncTask for App Initializing.
	 */
	private class InitAppTask extends
			android.os.AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {

			initData();

			return 0;
		}

		@Override
		protected void onPreExecute() {
			DeviceKeyActivity.this
					.showDialog(DeviceKeyActivity.PROGRESS_DIALOG);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Integer result) {
			displayKeys();
			DeviceKeyActivity.this
					.removeDialog(DeviceKeyActivity.PROGRESS_DIALOG);

		}
	}

}