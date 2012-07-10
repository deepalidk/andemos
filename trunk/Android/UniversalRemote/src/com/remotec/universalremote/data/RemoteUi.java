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

import android.bluetooth.BluetoothAdapter;

import com.remotec.universalremote.irapi.BtConnectionManager;

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
	    
	    public static String UI_DB_FILE="codelib.db";
	    
	    public static String UI_DB_FILE_TEMP="codelib_temp.db";
	    
	    /*
	    * singleton
	    */
	   private static RemoteUi sRemoteUi=null;
       
	    static {
	        // The default protection domain grants access to these properties.
	    	WORK_DIRECTORY = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/remotec/bRemote"; //$NON-NLS-1$ //$NON-NLS-2$
	    	INTERNAL_DATA_DIRECTORY = WORK_DIRECTORY+"/data";
	        UI_XML_FILE = "remote.xml";
	        UI_DB_FILE="codelib.db";
	    }
	    
	   public enum BrandListType{
	    	empty,
            BuildIn,
            UIRD;
	    };
	   
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
       private Map<String,Extender> mExtenderMap;
       
       public Map<String,Extender> getExtenderMap(){
    	   return mExtenderMap;
       }
       
       /*
        * get last active extender , if no last active extender, return null.
        */
       public Extender getLastActiveExtender(){
    	 
    	   Extender result=null;
    	  
    	   /*
    	    * clear last active flag.
    	    */
			for (Extender ext : mExtenderMap.values()) {
				if(ext.isLastactive()){
					result=ext;
				}
			}
			
			return result;
       }
       
       
       /*
        * active extender object for run time use.
        */
       private Extender mActiveExtender;
       
       public void setActiveExtender(Extender extender){
    	   mActiveExtender=extender;
    	   
    	   /*
    	    * clear last active flag.
    	    */
			for (Extender ext : mExtenderMap.values()) {
				ext.setIsLastActive(false);
			}
			
			// set current active flag
			mActiveExtender.setIsLastActive(true);
    	   
       }
       
       public Extender getActiveExtender(){
    	   if(RemoteUi.getEmulatorTag()){
    		 Extender ex=new Extender();
    		 ex.setAddress("00");
    		 ex.setName("BF10");
    		 ex.setVersion("ffff");
    		 return ex;
    	   }else{
    	     return mActiveExtender;
    	   }
       }
       
       /*
        * Holds the children objects.
        */
       private List<Device> mChildren;
       
       public List<Device> getChildren(){
    	   return mChildren;
       }
       
       /*
        * global variable provide to DeviceKeyAvtivity.
        */
       private Device mCurActiveDevice=null;
       
       public Device getActiveDevice(){
    	   return mCurActiveDevice;
       }
       
       public void setActiveDevice(Device dev){
    	   mCurActiveDevice=dev;
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
       
       //Mark current brand list infomations.
       private BrandListType mBrandListType=BrandListType.empty;
       
       public BrandListType getBrandListType() {
    	   return mBrandListType;
       }
       
       public void setBrandListType(BrandListType type){
    	   mBrandListType=type;
       }
       
       /*
        * Holds the template key maps
        */
       private Map<Integer, Key> mTemplateKeyMap;
       
       public Map<Integer, Key> getTemplateKeyMap(){
    	   return mTemplateKeyMap;
       }
       
	   // Bluetooth adapter
	   private BluetoothAdapter mBluetoothAdapter = null;
	   
	   public BluetoothAdapter getBluetoothAdapter(){
		   return mBluetoothAdapter;
	   }
	   
	   public void setBluetoothAdapter(BluetoothAdapter ba){
		   mBluetoothAdapter=ba;
	   }
	   
	   // Member object for the BT services
	   private BtConnectionManager mBtConnectMgr = null;
	   
	   public BtConnectionManager getBtConnectionManager(){
		   return mBtConnectMgr;
	   }
	   
	   public void setBtConnectionManager(BtConnectionManager bm){
		   mBtConnectMgr=bm;
	   }
	       
       private RemoteUi()
       {
    	   mChildren=new ArrayList<Device>();
    	   mExtenderMap=new Hashtable<String,Extender>();
    	   mCategoryList=new ArrayList<String>();
    	   mIrBrandMap=new Hashtable<String,List<String>>();
    	   mTemplateKeyMap=new Hashtable<Integer,Key>();
       }
       
       /*
        * Clear the categories which has not brand under it.
        */
       public void clearEmptyCategory(){
    	   for(int i=0;i<mCategoryList.size();){
    		  String temp= mCategoryList.get(i);
    		  
    		  if(!mIrBrandMap.containsKey(temp)){
    			  mCategoryList.remove(i);
    		  }else{
    			  i++;
    		  }
    	   }
       }
}
