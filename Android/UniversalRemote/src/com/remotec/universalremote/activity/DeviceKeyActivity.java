/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.data.Device;

/*
 *Displays device key for UI. 
 */
public class DeviceKeyActivity extends Activity {
	
	// Debugging Tags 
	private static final String TAG = "DeviceKeyActivity";
	private static final boolean D = false;
	
	//exchanges device object with deviceactivity.  
	public static final String DEVICE_OBJECT="DEVICE_OBJECT";
	
	private Device mDevice;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.devicekey);    
        
        Bundle bdl = getIntent().getExtras();
        mDevice= (Device)bdl.getSerializable(DEVICE_OBJECT);
        
       TextView tv=(TextView) this.findViewById(R.id.devicekey_title_left_text);
       tv.setText(mDevice.getName());
    }
}