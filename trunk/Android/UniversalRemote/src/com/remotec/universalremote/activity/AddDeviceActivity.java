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
	
    private Spinner spinner2;       
    private ArrayAdapter adapter2;  
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_device_wizard);
        
        
        spinner2 = (Spinner) findViewById(R.id.Spinner01);     
//      view2 = (TextView) findViewById(R.id.spinnerText02);       
      
        //将可选内容与ArrayAdapter连接起来        
        adapter2 = ArrayAdapter.createFromResource(this, R.array.dev_category, R.layout.irremote_spinner_item);       
      
        //设置下拉列表的风格         
        adapter2.setDropDownViewResource(R.layout.irremote_spinner_item);       
      
        //将adapter2 添加到spinner中        
        spinner2.setAdapter(adapter2);       
      
//添加事件Spinner事件监听          
//      spinner2.setOnItemSelectedListener(new SpinnerXMLSelectedListener());       
      
        //设置默认值        
        spinner2.setVisibility(View.VISIBLE);       

    }
}