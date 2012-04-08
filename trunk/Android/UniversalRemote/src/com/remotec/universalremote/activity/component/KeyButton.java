/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remotec.universalremote.activity.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.Key;

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
		
		  TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyButton);
		  mKeyId=a.getInteger(R.styleable.KeyButton_key_id, -1);
		  mIsIconBtn=a.getBoolean(R.styleable.KeyButton_is_icon_btn, false);
		            
	      a.recycle();   
	}

	public KeyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	 /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {
        
    	Key key=(Key)getTag();
    	
    	if(!mIsIconBtn)
    	{
	    	if(key!=null){
	    		if((!key.getVisible())&&(this.getVisibility()==View.VISIBLE))	{
	                  this.setTextColor(Color.GRAY);
	    		}else
	    		{
	    			 this.setTextColor(Color.BLACK);
	    			 
	    		}
	    	}else{
	    		 this.setTextColor(Color.BLACK); 		
	    	}
    	}
    	
        super.onDraw(canvas); 	  
    }
	
    
}
