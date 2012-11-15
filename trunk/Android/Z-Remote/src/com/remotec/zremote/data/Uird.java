/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remotec.zremote.data;

/*
 * Holds the Uird data to send ir code.
 */
public class Uird {
   
       
       //the key id;
       private int mKeyId;
       
       public int getKeyId(){
    	   return mKeyId;
       }
       
       public void setKeyId(int keyId){
    	   mKeyId=keyId;
       }
       
       //the data 
       private byte[] mUirdData;
       
       public byte[] getUirdData(){
    	   return mUirdData;
       }
              
       public void setUirdData(byte[] uirdData){
    	   mUirdData=uirdData;
       }
     
       public Uird()
       {
       }
}
