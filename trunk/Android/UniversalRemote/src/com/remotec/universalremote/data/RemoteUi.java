/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 * Description: The Ui data Root.
 * 
 *      Author: Walker
 */
package com.remotec.universalremote.data;

import java.util.ArrayList;
import java.util.List;

/*
 * Holds the data needed to run the UI.
 */
public class RemoteUi {
       
	    /*
	     * Work directory. 
	     */
	    public static String WORK_DIRECTORY;
	   
	    /*
	     * Work directory. 
	     */
	    public static String INTERNAL_DATA_DIRECTORY;
	    
	    public static String UI_XML_FILE="remote.xml";
	    
	    /*
	    * singleton
	    */
	   private static RemoteUi sRemoteUi=null;
       
	    static {
	        // The default protection domain grants access to these properties.
	    	WORK_DIRECTORY = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/remotec"; //$NON-NLS-1$ //$NON-NLS-2$
	    	INTERNAL_DATA_DIRECTORY = WORK_DIRECTORY+"/data";
	        UI_XML_FILE = "remote.xml";
	    }
	   
	   /*
	    * Gets the singleton object.
	    */
       public static RemoteUi getHandle()
       {
    	   if(sRemoteUi==null)
    	   {
    		   sRemoteUi=new RemoteUi();
    	   }
    	   
    	   return sRemoteUi;
       }
       
       /*
        * Marks current running state.
        */
       private static final boolean EMULATOR_TAG = false;
       
       /*
        * Gets the current running state.
        *  True: running on emulator.
        * False: running on a device.
        */
       public static boolean getEmulatorTag()
       {
    	   return EMULATOR_TAG;
       }
       
       /*
        * Holds the children objects.
        */
       private List<Extender> mChildren;
       
       private String mVersion;
       
       public String getVersion(){
    	   return mVersion;
       }
       
       public void setVersion(String ver){
    	   mVersion=ver;
       }
       
       public List<Extender> getChildren(){
    	   return mChildren;
       }
          
       private RemoteUi()
       {
    	   mChildren=new ArrayList<Extender>();
       }
}
