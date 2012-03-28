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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

/*
 *Displays device for UI.
 */
public class AddDeviceActivity extends Activity {
	
	// Debugging Tags
	private static final String TAG = "AddDeviceActivity";
	private static final boolean D = false;
	 
	private List<DeviceButton> mDevButtonList=null;
	
    private Spinner mSpinerCategory;    
    private Spinner mSpinerManufacturer;   
    private Spinner mSpinerModel;   
    private ArrayAdapter adapter2;  
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_device_wizard);
        
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        mSpinerCategory = (Spinner) findViewById(R.id.spiner_category);     
        mSpinerManufacturer = (Spinner) findViewById(R.id.spiner_manufacturer);   
        mSpinerModel = (Spinner) findViewById(R.id.spiner_model);   
        //view2 = (TextView) findViewById(R.id.spinnerText02);       
      
        //����ѡ������ArrayAdapter��������        
        adapter2 = ArrayAdapter.createFromResource(this, R.array.dev_category, R.layout.irremote_spinner);       
      
        //���������б��ķ��         
        adapter2.setDropDownViewResource(R.layout.irremote_spinner_item);       
      
        //��adapter2 ���ӵ�spinner��        
        mSpinerCategory.setAdapter(adapter2);   
        mSpinerManufacturer.setAdapter(adapter2);
        mSpinerModel.setAdapter(adapter2);
        
      
		//�����¼�Spinner�¼�����          
		//spinner2.setOnItemSelectedListener(new SpinnerXMLSelectedListener());       
        

    }
}