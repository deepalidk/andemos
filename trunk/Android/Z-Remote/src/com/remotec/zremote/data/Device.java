/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.zremote.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.remotec.zremote.activity.R;

import android.content.Context;

/*
 * Holds the data needed to run the UI.
 */
public class Device implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<Key> mChildren;

	public List<Key> getChildren() {
		return mChildren;
	}

	/*
	 * dev name
	 */
	private String mName;

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		
		if(name.length()>10){
			name=name.substring(0, 9);
		}
		 
		mName = name;
	}

	/*
	 * the name of icon name
	 */
	private String mIconName;

	public String getIconName() {
		return mIconName;
	}

	public void setIconName(String picName) {
		mIconName = picName;
	}

	/*
	 * the res id of Icon
	 */
	private int mIconResId;

	public int getIconResId() {
		return mIconResId;
	}

	public void setIconResId(int resId) {
		mIconResId = resId;
	}
	
	/*
	 * the name of manufacturer
	 */
	private String mManufacturer;

	public String getManufacturer() {
		return mManufacturer;
	}

	public void setManufacturer(String manufacturer) {
		mManufacturer = manufacturer;
	}
	
	/*
	 * the name of dev Type
	 */
	private String mDevType;

	public String getDeviceType() {
		return mDevType;
	}

	public void setDeviceType(String devType) {
		mDevType = devType;
	}
	
	/*
	 * the id of dev Type
	 */
	private int mDevTypeId;

	public int getDeviceTypeId() {
		return mDevTypeId;
	}

	public void setDeviceTypeId(int devTypeId) {
		mDevTypeId = devTypeId;
	}
	
	/*
	 * the ir code num
	 */
	private int mIrCode;

	public int getIrCode() {
		return mIrCode;
	}

	public void setIrCode(int ircode) {
		mIrCode = ircode;
	}
	
	/*
	 * the Value
	 */
	private int mValue=0;

	public int getValue() {
		return mValue;
	}

	public void setValue(int value) {
		mValue = value;
	}
	
	
	public Device() {
		mChildren = new ArrayList<Key>();

	}
	
	public static  Device createDevice(Context context)
	{
		Device newDev=new Device();
		newDev.mIconName =context.getResources().getString(R.string.dev_icon);
		newDev.mName=context.getResources().getString(R.string.dev_name);
		newDev.mDevType=context.getResources().getString(R.string.dev_category);
		newDev.mManufacturer=context.getResources().getString(R.string.dev_manufacturer);
		newDev.mIrCode=Integer.parseInt(context.getResources().getString(R.string.dev_codenum));
		newDev.mIconResId=context.getResources().getIdentifier(newDev.mIconName, "drawable",
				context.getApplicationInfo().packageName);
		return newDev;
	}
}