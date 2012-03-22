/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.remotec.universalremote.activity.R;

/*
 *Displays device key for UI. 
 */
public class DeviceKeyActivity extends Activity {
	
	// Debugging Tags 
	private static final String TAG = "DeviceKeyActivity";
	private static final boolean D = false;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.devicekey);     
    }
}