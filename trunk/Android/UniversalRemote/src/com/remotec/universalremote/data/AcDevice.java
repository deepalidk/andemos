/**
 * 
 */
package com.remotec.universalremote.data;

import java.util.Hashtable;
import java.util.Map;

import com.remotec.universalremote.activity.component.KeyButton;
import com.remotec.universalremote.data.Device;

/**
 * @author walker
 *
 */
public class AcDevice extends Device {
	
	/*
	 * all key buttons in key layout
	 */
	private Map<String, Key> mLearnKeyMap = null;
	
	public AcDevice(){
		mLearnKeyMap=new Hashtable<String,Key>();
	}

	public void setLearnKey(String key,Key data){
		mLearnKeyMap.put(key, data);
	}
	
	public Key getLearnKey(String key){
		return mLearnKeyMap.get(key);
	}
	
	public Map<String,Key> getLearnKeyMap(){
		
		return mLearnKeyMap;
		
	}
}
