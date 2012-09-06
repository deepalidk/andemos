package com.remotec.universalremote.irapi;

import android.app.Activity;
import android.os.Handler;

public interface IConnectionManager {
		
	//check if Device Adapter available
	public abstract boolean isAdapterAvailable();
	//check if Device Adapter is enabled
	public abstract boolean isAdapterEnabled();
	//Enable the Adapter
	public abstract void makeAdapterEnabled(Activity activity);
	
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing												// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
	//get connection state.
	public abstract int getState();
	
	//stop the connection.
	public abstract void stop();
	
	//start the connection.
	public abstract void start();
	
	//set an message handle 
	public abstract void setHandler(Handler handler);
	
	//connect the device.
	public abstract void connect(String deviceAddr);
}
