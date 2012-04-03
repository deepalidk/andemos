/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remotec.universalremote.activity.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.remotec.universalremote.data.Device;

/*
 * Performs as a remote key.
 */
public class KeyButton extends Button {
	
	/*
	 * the key id , when send ir command.
	 */
	private int mKeyId;
	
	public int getKeyId(){
		return mKeyId;
	}
	
	/*
	 * we can't change the text 
	 * label of an Icon button. 
	 */
	private boolean mIsIconBtn;
	
	public boolean getIsIconButton(){
		return mIsIconBtn;
	}
	
	public KeyButton(Context context) {
		super(context, null);
	}

	public KeyButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KeyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
    
}
