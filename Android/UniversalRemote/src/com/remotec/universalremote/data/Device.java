/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.data;

import java.util.ArrayList;
import java.util.List;

/*
 * Holds the data needed to run the UI.
 */
public class Device {
              
       private List<Key> mChildren;
       
       public List<Key> getChildren(){
    	   return mChildren;
       }
       
       private String mName;
       
       public String getName(){
    	   return mName;
       }
       
       public void setName(String name){
    	   mName=name;
       }
       
       /*
        * the name of icon name
        */
       private String mIconName;
       
       public String getIconName(){
    	   return mIconName;
       }
       
       public void setIconName(String picName){
    	   mIconName=picName;
       }
       
       public Device()
       {
    	   mChildren=new ArrayList<Key>();
       }
}
