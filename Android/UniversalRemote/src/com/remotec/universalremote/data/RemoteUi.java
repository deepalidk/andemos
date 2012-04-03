/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 * Description: The Ui data Root.
 * 
 *      Author: Walker
 */
package com.remotec.universalremote.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
	    
	    public static String UI_DB_FILE;
	    
	    /*
	    * singleton
	    */
	   private static RemoteUi sRemoteUi=null;
       
	    static {
	        // The default protection domain grants access to these properties.
	    	WORK_DIRECTORY = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/remotec"; //$NON-NLS-1$ //$NON-NLS-2$
	    	INTERNAL_DATA_DIRECTORY = WORK_DIRECTORY+"/data";
	        UI_XML_FILE = "remote.xml";
	        UI_DB_FILE="codelib.db";
	    }
	   
	   /*
	    * Gets the singleton object.
	    */
       public static RemoteUi getHandle()
       {  
    	   return sRemoteUi;
       }
       
       public static void init(){

          sRemoteUi=new RemoteUi();
    	   
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
             
       private String mVersion;
       
       public String getVersion(){
    	   return mVersion;
       }
       
       public void setVersion(String ver){
    	   mVersion=ver;
       }
       
       /*
        * Holds the Extender objects.
        */
       private List<Extender> mExtenderList;
       
       public List<Extender> getExtenderList(){
    	   return mExtenderList;
       }
       
       /*
        * Holds the children objects.
        */
       private List<Device> mChildren;
       
       public List<Device> getChildren(){
    	   return mChildren;
       }
       
       /*
        * Holds the category
        */
       private List<String> mCategoryList;
       
       public List<String> getCategoryList(){
    	   return mCategoryList;
       }
       
       /*
        * Holds the category
        */
       private Map<String, List<String>> mIrBrandMap;
       
       public Map<String, List<String>> getIrBrandMap(){
    	   return mIrBrandMap;
       }
       
       public List<String> getIrcodeList(String key){
    	   return mIrBrandMap.get(key);
       }
                
       private RemoteUi()
       {
    	   mChildren=new ArrayList<Device>();
    	   mExtenderList=new ArrayList<Extender>();
    	   mCategoryList=new ArrayList<String>();
    	   mIrBrandMap=new Hashtable<String,List<String>>();
       }
}
