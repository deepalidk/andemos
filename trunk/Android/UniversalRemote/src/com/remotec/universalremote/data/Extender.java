/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remotec.universalremote.data;

import java.util.ArrayList;
import java.util.List;

/*
 * Holds the data needed to run the UI.
 */
public class Extender {
             
       private List<Device> mChildren;
       
       //the device name;
       private String mName;
       
       public String getName(){
    	   return mName;
       }
       
       public void setName(String name){
    	   mName=name;
       }
       
       //the device address for connection.
       private String mAddress;
       
       public String getAddress(){
    	   return mAddress;
       }
              
       public void setAddress(String address){
    	   mAddress=address;
       }
       
       //the device is last connected device.
       private boolean mIsLastActive=false;
       
       public boolean isLastactive(){
    	   return mIsLastActive;
       }
              
       public void setIsLastActive(boolean isLastActive){
    	   mIsLastActive=isLastActive;
       }
       
       //the version of the extender.
       private String mVersion;
       
       public String getVersion(){
    	   return mVersion;
       }
              
       public void setVersion(String ver){
    	   setCapability(ver);
    	   mVersion=ver;
       }
       
       //the extender can supportLearning.
       private boolean mSupportLearning=false;
       public boolean getSupportLearning(){
    	   return mSupportLearning;
       }
       
       //the extender can supportInternalLib.
       private boolean mSupportInternalLib=false;
       public boolean getSupportInternalLib(){
    	   return mSupportInternalLib;
       }
       
       //the extender can supportUIRDLib.
       private boolean mSupportUirdLib=false;
       public boolean getSupportUirdLib(){
    	   return mSupportUirdLib;
       }
       
       private void setCapability(String ver){
    	   if(ver.equals("0009")){
    		   mSupportInternalLib=true;
    		   mSupportLearning=false;
    		   mSupportUirdLib=false;
    	   }else if(ver.equals("ffff")){
    		   mSupportInternalLib=false;
    		   mSupportLearning=false;
    		   mSupportUirdLib=true;
    	   }else if(ver.equals("000a")){
    		   mSupportInternalLib=true;
    		   mSupportLearning=true;
    		   mSupportUirdLib=false;
    	   }else if(ver.equals("000b")){
    		   mSupportInternalLib=true;
    		   mSupportLearning=true;
    		   mSupportUirdLib=false;
    	   }  
       }
         
       public Extender()
       {
       }
}
