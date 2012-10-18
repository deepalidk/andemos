/**
 * 
 */
package com.remotec.universalremote.data;

import java.util.ArrayList;
import java.util.List;

import com.remotec.universalremote.data.Device;

/**
 * @author walker
 *
 */
public class AvDevice extends Device {

	private List<Key> mChildren;

	@Override public List<Key> getChildren() {
		return mChildren;
	}

	public AvDevice(){
		
		mChildren = new ArrayList<Key>();
	}
	
}
