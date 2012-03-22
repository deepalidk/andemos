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
       
       
       public Extender()
       {
       }
}
