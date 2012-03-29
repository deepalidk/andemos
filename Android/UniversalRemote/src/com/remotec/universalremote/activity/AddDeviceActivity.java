/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *
 * Author: Walker
 */
package com.remotec.universalremote.activity;


import java.util.ArrayList;
import java.util.List;

import com.common.FileManager;
import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;
import com.remotec.universalremote.activity.component.DeviceButton;
import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.Extender;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.persistence.XmlManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

/*
 *Displays device for UI.
 */
public class AddDeviceActivity extends Activity {
	
	private static final int REQUEST_SELECT_ICON = 1;
	// Debugging Tags
	private static final String TAG = "AddDeviceActivity";
	private static final boolean D = false;
	 
	private List<DeviceButton> mDevButtonList=null;
	
    private Spinner mSpinerCategory;    
    private Spinner mSpinerManufacturer;   
    private Spinner mSpinerModel;   
    
    private ArrayAdapter adapter2;  
    
    private ViewGroup mVgSelectDevice;
    private ViewGroup mVgDeviceInfo;
    
    private Button mBtnCancel;
    private Button mBtnNext;
    private Button mBtnBack;
    private Button mBtnFinish;
    private Button mBtnTest;
    
    private EditText mDeviceName;   //name edittext
    
    private ImageView mDeviceIcon;
	
    enum eCurrentPage
    {
    	eSelectDevice,
    	eDeviceInfo
    };
    
    private eCurrentPage mCurPage;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_device_wizard);
        
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
	    setResult(RESULT_CANCELED);
		
        initControls();
        
        mCurPage=eCurrentPage.eSelectDevice;
      
		//添加事件Spinner事件监听          
		//spinner2.setOnItemSelectedListener(new SpinnerXMLSelectedListener());       
    }
    
    void initCurrentPage(eCurrentPage curPage)
    {
		mCurPage=curPage;
    	if(curPage==eCurrentPage.eSelectDevice)
    	{
    	       mBtnNext.setVisibility(View.VISIBLE);
    	       mBtnFinish.setVisibility(View.INVISIBLE);
    	       mBtnBack.setVisibility(View.INVISIBLE);
    	       mVgSelectDevice.setVisibility(View.VISIBLE);
    	       mVgDeviceInfo.setVisibility(View.INVISIBLE);
    	}
    	else if(curPage==eCurrentPage.eDeviceInfo)
    	{
    
 	       mBtnNext.setVisibility(View.INVISIBLE);
	       mBtnFinish.setVisibility(View.VISIBLE);
	       mBtnBack.setVisibility(View.VISIBLE);
	       mVgSelectDevice.setVisibility(View.INVISIBLE);
	       mVgDeviceInfo.setVisibility(View.VISIBLE);
	       
	       InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE); 
	       imm.hideSoftInputFromWindow(mDeviceName.getWindowToken(),0);
    	}
    }

	private void initControls() {
		
		mSpinerCategory = (Spinner) findViewById(R.id.spiner_category);     
        mSpinerManufacturer = (Spinner) findViewById(R.id.spiner_manufacturer);   
        mSpinerModel = (Spinner) findViewById(R.id.spiner_model);   
        
        mVgSelectDevice=(ViewGroup)findViewById(R.id.select_ircode);
        mVgDeviceInfo=(ViewGroup)findViewById(R.id.device_info);
        
        mBtnCancel=(Button)findViewById(R.id.btn_footer_cancel);
        mBtnCancel.setOnClickListener(mOnCancelListener);
        
        mBtnNext=(Button)findViewById(R.id.btn_footer_next);
        mBtnNext.setOnClickListener(mOnNextListener);
        
        mBtnFinish=(Button)findViewById(R.id.btn_footer_finish);
        mBtnFinish.setOnClickListener(mOnFinishListener);
        
        mBtnBack=(Button)findViewById(R.id.btn_footer_back);
        mBtnBack.setOnClickListener(mOnBackListener);
        
        mBtnTest=(Button)findViewById(R.id.btn_test);
        mBtnTest.setOnClickListener(mOnTestListener);
        
        mDeviceName=(EditText)findViewById(R.id.name_edit);
        
        mDeviceIcon=(ImageView)findViewById(R.id.device_img);
        mDeviceIcon.setOnClickListener(mOnIconListener);
      
        //将可选内容与ArrayAdapter连接起来        
        adapter2 = ArrayAdapter.createFromResource(this, R.array.dev_category, R.layout.irremote_spinner);       
      
        //设置下拉列表的风格         
        adapter2.setDropDownViewResource(R.layout.irremote_spinner_item);       
      
        //将adapter2 添加到spinner中        
        mSpinerCategory.setAdapter(adapter2);   
        mSpinerManufacturer.setAdapter(adapter2);
        mSpinerModel.setAdapter(adapter2); 
	}
	
	/*
	 * Deals cancel button click.
	 */
   private OnClickListener mOnCancelListener=new OnClickListener(){

	@Override
	public void onClick(View v) {
		AddDeviceActivity.this.setResult(RESULT_CANCELED);
		AddDeviceActivity.this.finish();
	}
	   
   };
   
	/*
	 * Deals back button click.
	 */
  private OnClickListener mOnBackListener=new OnClickListener(){

	@Override
	public void onClick(View v) {
		AddDeviceActivity.this.initCurrentPage(eCurrentPage.eSelectDevice);
	}
  };
  
	/*
	 * Deals next button click.
	 */
private OnClickListener mOnNextListener=new OnClickListener(){

	@Override
	public void onClick(View v) {
		AddDeviceActivity.this.initCurrentPage(eCurrentPage.eDeviceInfo);
	}
};

/*
 * Deals finish button click.
 */
private OnClickListener mOnFinishListener=new OnClickListener(){

@Override
public void onClick(View v) {
	AddDeviceActivity.this.setResult(RESULT_OK);
	AddDeviceActivity.this.finish();
}
};

/*
 * Deals finish button click.
 */
private OnClickListener mOnTestListener=new OnClickListener(){

@Override
public void onClick(View v) {

}
};

/*
 * Deals finish button click.
 */
private OnClickListener mOnIconListener=new OnClickListener(){

@Override
public void onClick(View v) {
	Intent addDeviceIntent = new Intent(AddDeviceActivity.this, SelectIconDialog.class);
	startActivityForResult(addDeviceIntent, REQUEST_SELECT_ICON);
}
};
    
    
}