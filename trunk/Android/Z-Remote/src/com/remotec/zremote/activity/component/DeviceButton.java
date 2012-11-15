/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remotec.zremote.activity.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.remotec.zremote.data.Device;

/*
 * Performs as a device.
 */
public class DeviceButton extends Button {

	private Device mDevice;
	
	public void setDevice(Device dev){
		mDevice=dev;
	}
	
	public Device getDevice(){
	  return mDevice;
	}
	
	
	public DeviceButton(Context context) {
		super(context, null);
	}

	public DeviceButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DeviceButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
