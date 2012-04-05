/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 * Description: The Ui data Root.
 * 
 *      Author: Walker
 */
package com.remotec.universalremote.data;

import java.io.Serializable;

/*
 * Holds the data needed to run the UI.
 */
public class Key implements Serializable {
      
	/**
	 * 
	 */
	private static final long serialVersionUID = -5996437544418198977L;
	/*
	 * the key button text;
	 */
	private String mText;
	
	public String getText(){
		return mText;
	}
	
	public void setText(String text){
		mText=text;
	}
	
	/*
	 * the key id to emit IR code;
	 */
	private int mKeyId;
	
	public int getKeyId(){
		return mKeyId;
	}
	
	public void setKeyId(int keyId){
		mKeyId=keyId;
	}
	
	/*
	 * the key id to emit IR code;
	 */
	private boolean mVisible;
	
	public boolean getVisible(){
		return mVisible;
	}
	
	public void setVisible(boolean visible){
		mVisible=visible;
	}
	
	/*
	 * send learn key or origin key;
	 */
	private boolean mIsLearned;
	
	public boolean getIsLearned(){
		return mIsLearned;
	}
	
	public void setIsLearned(boolean isLearned){
		mIsLearned=isLearned;
	} 
	
	/*
	 * create a new copy of the key object.
	 */
	public Key colonel(){
		Key result=new Key();
		
		result.mKeyId=this.mKeyId;
		result.mIsLearned=this.mIsLearned;
		result.mText=this.mText;
		result.mVisible=this.mVisible;
		
		return result;
	}
}
