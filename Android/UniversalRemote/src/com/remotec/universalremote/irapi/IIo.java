package com.remotec.universalremote.irapi;

import android.util.Log;



/**
 * the abstract class of IO.
 * @author walker
 *
 */
public abstract class IIo {
    protected IOnRead mmIOnRead;
   
    public IIo()
    {
    	mmIOnRead=null;
    }
    
    /**
     * set IOnRead Function
     * @param iorF the function to be set
     */
    public synchronized void setOnReadFunc(IOnRead iorF)
    {
    	mmIOnRead=iorF;
    }
    
	/**
	 * write data to IO.
	 * @param buffer the data to be write
	 */
	public abstract void write(byte[] buffer);
   
}
