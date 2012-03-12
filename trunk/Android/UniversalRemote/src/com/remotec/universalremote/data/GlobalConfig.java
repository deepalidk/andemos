package com.remotec.universalremote.data;

/*
 * 
 */
public class GlobalConfig {

	private static GlobalConfig mHandle=null;
	
	public static GlobalConfig getHandle()
	{
		if(mHandle==null)
		{
			mHandle=new GlobalConfig();
		}
		
		return mHandle;
	}
	
	private GlobalConfig()
	{
		
	}
}
