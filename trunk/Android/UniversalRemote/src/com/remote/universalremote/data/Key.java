/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 * Description: The Ui data Root.
 * 
 *      Author: Walker
 */
package com.remote.universalremote.data;

import java.io.Serializable;

/*
 * Holds the data needed to run the UI.
 */
public class Key implements Serializable {
    
	/*
	 * To identify the key data.
	 */
	public enum Mode{
		Null(0),
		BuildIn(1),
		Learn(2),
		UIRD(3);
		 
	    private final int val;  
	  
	    private Mode(int value) {  
	        val = value;  
	    }  
	  
	    public int getValue() {  
	        return this.val;  
	    }  
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5996437544418198977L;
	/*
	 * the key button text;
	 */
	private String mText="";
	
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
	 * The mode to send key.
	 * 
	 * BuildIn: The key code is already build in RT 300.
	 * 
	 * Learn: The key code store at data array.
	 * 
	 * null: empty code.
	 */
	private Mode mMode=Mode.BuildIn;
	
	public Mode getMode(){
		return mMode;
	}
	
	public void setMode(Mode mode){
		mMode=mode;
	} 
	
	/*
	 * the data to send to RT300.
	 */
	private byte[] mData=null;
	
	public byte[] getData(){
		return mData;
	}
	
	public void setData(byte[] data){
		mData=data;
	}
	
	/*
	 * the learn data saved location.
	 */
	private byte mLearnLocation;
	
	public byte getLearnLocation(){
		return mLearnLocation;
	}
	
	public void setLearnLocation(byte loc){
		mLearnLocation=loc;
	}
	
	/*
	 * create a new copy of the key object.
	 */
	public Key colonel(){
		Key result=new Key();
		
		result.mKeyId=this.mKeyId;
		result.mMode=this.mMode;
		result.mText=this.mText;
		result.mVisible=this.mVisible;
		result.mLearnLocation=this.mLearnLocation;
		
		if(mData!=null){
			result.mData=mData.clone();
		}
		
		return result;
	}
}
