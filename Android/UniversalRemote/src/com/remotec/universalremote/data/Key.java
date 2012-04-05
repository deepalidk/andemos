/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 * Description: The Ui data Root.
 * 
 *      Author: Walker
 */
package com.remotec.universalremote.data;

/*
 * Holds the data needed to run the UI.
 */
public class Key {
      
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
}
