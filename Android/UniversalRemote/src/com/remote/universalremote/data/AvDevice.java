/**
 * 
 */
package com.remote.universalremote.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import com.remote.universalremote.data.Device;

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
