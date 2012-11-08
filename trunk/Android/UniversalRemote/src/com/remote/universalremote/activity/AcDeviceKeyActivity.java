/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remote.universalremote.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.common.FileManager;
import com.remote.universalremote.activity.AddDeviceActivity.eCurrentPage;
import com.remote.universalremote.activity.component.BottomBarButton;
import com.remote.universalremote.activity.component.DeviceButton;
import com.remote.universalremote.activity.component.KeyButton;
import com.remote.universalremote.activity.component.ViewFlipperEx;
import com.remote.universalremote.data.AcDevice;
import com.remote.universalremote.data.Device;
import com.remote.universalremote.data.Extender;
import com.remote.universalremote.data.Key;
import com.remote.universalremote.data.RemoteUi;
import com.remote.universalremote.data.Uird;
import com.remote.universalremote.data.Key.Mode;
import com.remote.universalremote.data.RemoteUi.BrandListType;
import com.remote.universalremote.irapi.BtConnectionManager;
import com.remote.universalremote.irapi.EmitTask;
import com.remote.universalremote.irapi.IrApi;
import com.remote.universalremote.persistence.DbManager;
import com.remote.universalremote.persistence.XmlManager;
import com.remote.universalremote.activity.R;

/*
 *Displays device key for UI. 
 */
public class AcDeviceKeyActivity extends Activity {

	// Debugging Tags
	private static final String TAG = "AcDeviceKeyActivity";
	private static final boolean D = false;

	public static final String TOAST = "toast";
	// the activit mode of key layout activity
	public static final String ACTIVITY_MODE = "ACTIVITY_MODE";
	public static final int ACTIVITY_CONTROL = 0;
	public static final int ACTIVITY_EDIT = 1;
	// Message types sent from the Bluetooth connect manager Handler
	public static final int CONNECTTION_STATE_CHANGE = 1;
	public static final int MESSAGE_TOAST = 5;

	// dialog ids
	private static final int PROGRESS_DIALOG = 0;

	private AcDevice mDevice;

	private ProgressDialog mProgressDialog;

	private ImageView mModeImageview;
	private TextView mTempTextview;
	private TextView mFanTextview;
	private Button mButtonLearn;

	/*
	 * all key buttons in key layout
	 */
	private Map<Integer, KeyButton> mKeyButtonMap = null;

	private int mActivityMode = ACTIVITY_CONTROL;

	// mark disconnect when on resume.
	private boolean mDisconnectTag = true;

	/* to identify current transmission type */
	private boolean mContinuousTag = false;

	private enum EditKeyType {
		label_key_visible, label_key_invisible, icon_key_visible, icon_key_invisible, key_error
	};

	// mark to determine how to edit key.
	private EditKeyType mCurEditKeyType;

	// store the cur edit key, for edit.
	private KeyButton mCurActiveKey;

	// the object of learning dialog.
	private AlertDialog mLearningDlg;

	private TextView mTitleRight = null;

	/* fliping animation */
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final int REQUEST_ENABLE_BT = 0;

	private String[] modes = { "auto", "cool", "dry", "fan", "heat" };
	private String[] fans = { "FAN AUTO", "HIGH", "LOW" };

	private int temp = 25;
	private int mode = 1;
	private int fan = 0;
	private boolean power = false;
	private ViewGroup mVgPanel = null;

	private void nextMode() {

		mode = (mode + 1) % 5;
		fan = 0;
		temp = 25;
		
	}

	private void nextFan() {
		fan = (fan + 1) % 3;
	}

	private void resetStatus() {
		mode = 1;
		fan = 0;
		temp = 25;
		power = !power;
	}

	void displayPower() {

		if (power) {

			mVgPanel.setBackgroundColor(Color.BLUE);

		} else {

			mVgPanel.setBackgroundColor(Color.LTGRAY);

		}

	}

	private void nextTemp() {
		if (temp < 31)
			temp++;

	}

	private void lastTemp() {

		if (temp > 17) {

			temp--;
		}
	}

	private void display() {

		displayMode();
		displayTemp();
		displayFan();
		displayPower();

	}

	private void displayTemp() {

		if (mTempTextview != null) {
			mTempTextview.setText(String.format("%d℃", temp));
		}

	}

	private void displayFan() {

		if (mFanTextview != null) {
			
			String strFanMode=fans[fan].replaceFirst("FAN ", "");
			
			mFanTextview.setText(strFanMode);
		}

	}

	private void displayMode() {

		if (mModeImageview != null) {
			switch (mode) {

			case 0:
				mModeImageview.setImageResource(R.drawable.auto);
				break;
			case 1:
				mModeImageview.setImageResource(R.drawable.cool);
				break;
			case 2:
				mModeImageview.setImageResource(R.drawable.dry);
				break;
			case 3:
				mModeImageview.setImageResource(R.drawable.fan);
				break;
			case 4:
				mModeImageview.setImageResource(R.drawable.heat);
				break;
			}
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ac_device_key);
		//
		 mActivityMode = getIntent()
		 .getIntExtra(ACTIVITY_MODE, ACTIVITY_CONTROL);
		
		// for null pointer bug, move the find right title text before bt init.
		mTitleRight = (TextView) findViewById(R.id.title_right_text);

		if (RemoteUi.getHandle().getActiveExtender() != null) {
			mTitleRight.setText(R.string.title_connected_to);
			mTitleRight.append(RemoteUi.getHandle().getActiveExtender()
					.getName());
		} else {
			mTitleRight.setText(R.string.title_not_connected);
		}

		// If the adapter is null, then Bluetooth is not supported
		if (!RemoteUi.getEmulatorTag()) {
			if (!RemoteUi.getHandle().getConnectionManager()
					.isAdapterAvailable()) {
				Toast.makeText(this, "Bluetooth is not available",
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		// Initializing data.
		// InitAppTask initor = new InitAppTask();
		// initor.execute(0);

		initData();

		if(mActivityMode==ACTIVITY_CONTROL){
			mButtonLearn.setVisibility(View.GONE);
			mTitleRight.setVisibility(View.VISIBLE);
		}else{
			mButtonLearn.setVisibility(View.VISIBLE);
			mTitleRight.setVisibility(View.GONE);
		}
		
		display();

	}

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
					v.setOnClickListener(mKeyOnClickListener);
				}
			} else if (v instanceof ViewGroup) {
				findKeyButtons((ViewGroup) v, bMap);
			}
		}
	}

	@Override
	public void onResume() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON resume ++");

		mDisconnectTag = true;

		if (!RemoteUi.getEmulatorTag()) {
			// If BT is not on, request that it be enabled.
			// setupChat() will then be called during onActivityResult
			if (!RemoteUi.getHandle().getConnectionManager().isAdapterEnabled()) {
				mDisconnectTag = false;
				RemoteUi.getHandle().getConnectionManager()
						.makeAdapterEnabled(this);
				// Otherwise, setup the chat session
			} else {
				setupBluetooth();
			}

			RemoteUi.getHandle().getConnectionManager()
					.setHandler(this.mHandler);
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (RemoteUi.getHandle().getConnectionManager().getState() == BtConnectionManager.STATE_NONE) {
				// Start the Bluetooth chat services
				RemoteUi.getHandle().getConnectionManager().start();

				if (RemoteUi.getHandle().getLastActiveExtender() != null) {
					String deviceAddr = RemoteUi.getHandle()
							.getLastActiveExtender().getAddress();

					// Attempt to connect to the device
					RemoteUi.getHandle().getConnectionManager()
							.connect(deviceAddr);
				}
			}
		}
	}
	
	private void setupBluetooth() {
		Log.d(TAG, "setupBluetooth()");

		// Initialize the BluetoothRemoteService to perform bluetooth
		// connection
		RemoteUi.getHandle().getConnectionManager().setHandler(mHandler);

	}

	@Override
	public void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "++ ON Stop ++");

		// Stop the Bluetooth chat services
		if (RemoteUi.getHandle().getConnectionManager() != null
				&& mDisconnectTag == true) {
			RemoteUi.getHandle().getConnectionManager().stop();
		}

		mDisconnectTag = true;
	}

	// The Handler that gets information back from the BluetoothRemoteService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONNECTTION_STATE_CHANGE:
				switch (msg.arg1) {
				case BtConnectionManager.STATE_CONNECTED: {
					mTitleRight.setText(R.string.title_connected_to);
					mTitleRight.append(RemoteUi.getHandle()
							.getLastActiveExtender().getName());
					break;
				}
				case BtConnectionManager.STATE_CONNECTING:
					mTitleRight.setText(R.string.title_connecting);
					break;
				case BtConnectionManager.STATE_NONE:
					mTitleRight.setText(R.string.title_not_connected);
					AcDeviceKeyActivity.this.finish();
					break;
				}
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/* skip back key */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mDisconnectTag = false;
		}

		return super.onKeyDown(keyCode, event);
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
		mDevice =(AcDevice) RemoteUi.getHandle().getActiveDevice();

		mModeImageview = (ImageView) findViewById(R.id.imageview_mode);
		mFanTextview = (TextView) findViewById(R.id.textview_fanmode);
		mTempTextview = (TextView) findViewById(R.id.textview_temp);
		mVgPanel = (ViewGroup) findViewById(R.id.id_panel);
		mButtonLearn=(Button)findViewById(R.id.buttonLearn);
		mButtonLearn.setOnClickListener(mButtonLearnOnClickListener);

		/*
		 * finds all key Buttons
		 */
		mKeyButtonMap = new Hashtable<Integer, KeyButton>();

		ViewGroup vg = (ViewGroup) findViewById(R.id.id_key_layout);

		findKeyButtons(vg, mKeyButtonMap);
		
	
	}

	/*
	 * Key click listener.
	 */
	private OnClickListener mButtonLearnOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			displayPreLearnDlg();
		}
   };
	
	/*
	 * Key click listener.
	 */
	private OnClickListener mKeyOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			mCurActiveKey = (KeyButton) v;

			// if (mActivityMode == ACTIVITY_EDIT) {
			// showKeyEditMenu(mCurActiveKey);
			// }
			int keyId = mCurActiveKey.getKeyId();

			if (power != false || keyId == 0x01) {
				switch (keyId) {

				case 0x08: // fan

					nextFan();

					break;
				case 0x01: // power

					resetStatus();
					break;
				case 0x03: // mode
					nextMode();
					break;
				case 0x0e: // temp down
					lastTemp();
					break;
				case 0x0d: // temp up

					nextTemp();

					break;

				}
				
				emitKeyIR(mCurActiveKey,(byte)0x81);
			}
			display();

		}

	};

	private EditText mLabelEdit;
	private float mLastMotionPosX;

	private Key getCurKey(KeyButton keyBtn) {

		Key result=getLearnKey();
		
		if(result==null){
		
			DbManager dbm = new DbManager();
			String powerStatus= power ? "ON" : "OFF";
			String modeStatus = modes[mode];
			String tempStatus = String.format("%d", temp);
			String swingStatus = String.format("%s", "ON");
			String fanStatus = fans[fan];
	
			Uird uird = dbm.getUirdData(mDevice.getIrCode(), keyBtn.getKeyId(),
					powerStatus, modeStatus, tempStatus, swingStatus, fanStatus);

			if(uird!=null){
				
				result=new Key();
				result.setMode(Mode.UIRD);
				result.setData(uird.getUirdData());
			}
		}
      
		return result;
	}
	
	private Key getLearnKey(){
		
	  return mDevice.getLearnKey(getLearnKeyString());
		
	}
	
	private void setLearnKey(Key data){
		
		mDevice.setLearnKey(getLearnKeyString(), data);
		
	}
	
	private String getLearnKeyString(){
		String powerStatus= power ? "ON" : "OFF";
		String modeStatus = modes[mode];
		String tempStatus = String.format("%d", temp);
		String swingStatus = String.format("%s", "ON");
		String fanStatus = fans[fan];
		String result=String.format("%s_%s_%s_%s_%s", powerStatus,modeStatus,tempStatus,swingStatus,fanStatus);
	    return result;
	}

	/*
	 * Emits key IR.
	 * 
	 * @emitType: 0x01: continuous transmission. need send stop command to stop
	 * emit. 0x81: single transmission.
	 */
	private void emitKeyIR(KeyButton keyBtn, byte emitType) {

		Key key = getCurKey(keyBtn);

		if(key!=null){
			EmitTask task = new EmitTask(mDevice, key, emitType);
			task.execute(0);
		}
	}

	/*
	 * provide key labels and commands
	 */
	private void showKeyEditMenu(KeyButton keyBtn) {

		mCurEditKeyType = getEditKeyType(keyBtn);

		if (mCurEditKeyType == EditKeyType.icon_key_invisible) {
			// To do start learn activity
			displayLearnDlg();
		} else {
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

		int resStringArray = this.getMenuResource(type);
		// 设置可供选择的ListView
		builder.setItems(resStringArray, new DialogInterface.OnClickListener() {

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
	private void menuItemSelected(int which) {

		switch (mCurEditKeyType) {
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
	private void visibleLabelKeySelected(int which) {
		switch (which) {
		case 0: // learn
			displayLearnDlg();
			break;
		case 1:// delete
			displayDeleteConfirmDlg();
			break;
		case 2:// edit
			displayEditLabelDlg();
			break;
		}
	}

	/*
	 * a visible icon key menu item seleted.
	 */
	private void visibleIconKeySelected(int which) {
		switch (which) {
		case 0: // learn
			displayLearnDlg();
			break;
		case 1:// delete
			displayDeleteConfirmDlg();
			break;
		}
	}

	/*
	 * a visible label key menu item seleted.
	 */
	private void invisibleLabelKeySelected(int which) {
		switch (which) {
		case 0: // learn
			displayLearnDlg();
			break;
		case 1:// edit
			displayEditLabelDlg();
			break;
		}
	}

	/*
	 * displays the learn failed dialog.
	 */
	private void displayLearnFailed() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.learn_failed_title);
		builder.setMessage(R.string.learn_failed_msg);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						displayPreLearnDlg();
					}
				});

		builder.create().show();
	}

	/*
	 * displays the learn failed dialog.
	 */
	private void displayLearnSucess() {

		Toast.makeText(getApplicationContext(), R.string.learning_succeed_msg,
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * displays the learning dialog.
	 */
	private void displayLearningDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle(R.string.Learn_dialog_title);

		ViewGroup vg = (ViewGroup) this.getLayoutInflater().inflate(
				R.layout.learn_dialog, null);

		builder.setView(vg);

		mLearningDlg = builder.create();

		mLearningDlg.show();
	}

	/*
	 * displays the learning dialog.
	 */
	private void displayTestLearningKeyDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle("Test the learned key!");

		ViewGroup vg = (ViewGroup) this.getLayoutInflater().inflate(
				R.layout.learn_dialog, null);

		vg.findViewById(R.id.progressBar1).setVisibility(View.GONE);

		builder.setView(vg);

		builder.setPositiveButton(R.string.btn_test,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							// 不关闭对话框
							Field field = dialog.getClass().getSuperclass()
									.getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(dialog, false);

							emitKeyIR(mCurActiveKey, (byte) 0x81);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		builder.setNeutralButton(R.string.btn_learn,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {

							dialog.dismiss();

							LearningTask task = new LearningTask();
							task.execute(0);
							// 关闭对话框
							Field field = dialog.getClass().getSuperclass()
									.getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(dialog, true);

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

		builder.setNegativeButton(R.string.btn_done,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {

							dialog.dismiss();
							display();

							// 关闭对话框
							Field field = dialog.getClass().getSuperclass()
									.getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(dialog, true);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				});

		builder.create().show();
	}

	/*
	 * displays the prepare learn dialog.
	 */
	private void displayPreLearnDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle(R.string.learn_remote_commands);

		ViewGroup vg = (ViewGroup) this.getLayoutInflater().inflate(
				R.layout.prelearn_dialog, null);

		builder.setView(vg);
		builder.setPositiveButton(R.string.btn_start,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();

						LearningTask task = new LearningTask();
						task.execute(0);
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
	 * displays the delete confirm dialog.
	 */
	private void displayEditLabelDlg() {
		/* build a dialog, ask if want to close */
		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle(R.string.edit_key_label);

		mLabelEdit = new EditText(this);

		Key key = (Key) mCurActiveKey.getTag();
		mLabelEdit.setText(key.getText());

		builder.setView(mLabelEdit);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Key key = (Key) mCurActiveKey.getTag();
						String text = mLabelEdit.getText().toString();
						key.setText(text);

						/*
						 * save the data.
						 */
						XmlManager xmlManager = new XmlManager();
						xmlManager.saveData(RemoteUi.getHandle(),
								RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
										+ RemoteUi.UI_XML_FILE);

						dialog.dismiss();
						display();
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
	private void displayLearnDlg() {
		if (RemoteUi.getHandle().getActiveExtender().getSupportLearning()
				|| RemoteUi.getEmulatorTag()) {
			displayPreLearnDlg();
		} else {
			Toast.makeText(this, R.string.no_learning, Toast.LENGTH_SHORT)
					.show();
		}
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

						Key key = (Key) mCurActiveKey.getTag();
						key.setVisible(false);

						/*
						 * save the data.
						 */
						XmlManager xmlManager = new XmlManager();
						xmlManager.saveData(RemoteUi.getHandle(),
								RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
										+ RemoteUi.UI_XML_FILE);

						dialog.dismiss();
						display();
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
	private int getMenuResource(EditKeyType type) {
		int resResult = -1;
		switch (type) {
		case icon_key_visible:
			resResult = R.array.menu_icon_key_visible;
			break;
		case label_key_invisible:
			resResult = R.array.menu_label_key_invisible;
			break;
		case label_key_visible:
			resResult = R.array.menu_label_key_visible;
			break;
		}

		return resResult;
	}

	/*
	 * Gets edit key type from keyBtn
	 */
	private EditKeyType getEditKeyType(KeyButton keyBtn) {
		EditKeyType result = EditKeyType.key_error;

		Key key = (Key) keyBtn.getTag();

		if (key != null) { // if no key object, it's a error key.

			if (key.getVisible() == true) {
				if (keyBtn.getIsIconButton()) {
					result = EditKeyType.icon_key_visible;
				} else {
					result = EditKeyType.label_key_visible;
				}
			} else {
				if (keyBtn.getIsIconButton()) {
					result = EditKeyType.icon_key_invisible;
				} else {
					result = EditKeyType.label_key_invisible;
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
			AcDeviceKeyActivity.this
					.showDialog(AcDeviceKeyActivity.PROGRESS_DIALOG);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Integer result) {
			display();
			AcDeviceKeyActivity.this
					.removeDialog(AcDeviceKeyActivity.PROGRESS_DIALOG);

		}
	}

	/*
	 * AsyncTask for Learning.
	 */
	private class LearningTask extends
			android.os.AsyncTask<Integer, Integer, Integer> {

		private static final byte LEARN_LOCATION = 0;
		// learning result
		byte[] mLearningResult = null;

		public boolean getLeaningResult() {
			return mLearningResult != null;
		}

		// learning result
		byte[] mData = null;

		public byte[] getLeaningData() {
			return mData;
		}

		@Override
		protected Integer doInBackground(Integer... params) {

			IrApi irController = IrApi.getHandle();

			// learn at loc 0
			mLearningResult = irController.learnIrCode((byte)1);

			if (getLeaningResult()) {

				// get the data at loc 0
				Key key = new Key();
				key.setData(mLearningResult);
				key.setMode(Mode.Learn);
				key.setVisible(true);
				
				setLearnKey(key);
				

				/*
				 * save the data.
				 */
				XmlManager xmlManager = new XmlManager();
				xmlManager.saveData(RemoteUi.getHandle(),
						RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
								+ RemoteUi.UI_XML_FILE);
			}

			return 0;
		}

		@Override
		protected void onPreExecute() {
			displayLearningDlg();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Integer result) {

			mLearningDlg.dismiss();
			mLearningDlg = null;
			if (getLeaningResult()) {
				displayLearnSucess();
				displayTestLearningKeyDlg();
			} else {
				displayLearnFailed();
			}
		}
	}

}