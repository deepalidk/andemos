/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.activity;


import java.util.ArrayList;
import java.util.List;

import com.common.FileManager;
import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;
import com.remotec.universalremote.activity.component.DeviceButton;
import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.Extender;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.persistence.XmlManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

/*
 *Displays device for UI. 
 */
public class DeviceActivity extends Activity {
	
	// Debugging Tags 
	private static final String TAG = "UniversalRemoteActivity";
	private static final boolean D = false;
	
	private List<DeviceButton> mDevButtonList=null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device);   
      
       
        //Initializing data.
        InitAppTask initor= new InitAppTask();
        initor.execute(0);           
    }
    
    /*
     * inits the data for device activity
     */
    private void initData(){
    	
    	//we already load the device object infos now,
    	//but the resId of icon can only access during run time.
        //get the resId of icon with the icon name, and set to device object.
    	initDeviceIconId();
    	
    	mDevButtonList=new ArrayList<DeviceButton>();
    	
        ViewGroup tbLayout=(ViewGroup)findViewById(R.id.device_table);
        
        findDeviceButtons(tbLayout,mDevButtonList);
    	
    }
    
    /*
     * Inits the Device object Icon res id.
     */
    private void initDeviceIconId()
    {	    	
    	List<Device> devList=RemoteUi.getHandle().getChildren();
    	
    	for(Device dev:devList){
    	   int resId=getResources().getIdentifier(dev.getIconName(), "drawable",
    			   getApplicationInfo().packageName);
    	   dev.setIconResId(resId);
    	}
    }
    
    /*
     * finds all the device buttons in the layout.
     */
    private void findDeviceButtons(ViewGroup vg,List<DeviceButton> devList)
    {
    	for(int i=0;i<vg.getChildCount();i++){
        	View v=vg.getChildAt(i);
        	if(v instanceof DeviceButton){
        		devList.add((DeviceButton) v);
        		v.setOnClickListener(mDevButtonOnClickListener);
        	}
        	else if(v instanceof ViewGroup)
        	{
        		findDeviceButtons((ViewGroup) v,devList);
        	}
        }
    }
    
    /*
     * displays the devices on the screen.
     */
    private void displayDevices(){
    	
    	if(RemoteUi.getHandle().getChildren().size()>0)
    	{
    		List<Device> devList=RemoteUi.getHandle().getChildren();
    		
    		for(int i=0;i<devList.size();i++)
    		{
    			displayDevice(devList.get(i),mDevButtonList.get(i));
    		}
           
    		//add device button
    		displayAddDevice(mDevButtonList.get(devList.size()));
    		
    		for(int i=devList.size()+1;i<mDevButtonList.size();i++)
    		{
    			mDevButtonList.get(i).setVisibility(View.INVISIBLE);
    		}
    	}
    	else
    	{
    		for(int i=1;i<mDevButtonList.size();i++)
    		{
    			mDevButtonList.get(i).setVisibility(View.INVISIBLE);
    		}
    		
    		//add device button
    		displayAddDevice(mDevButtonList.get(0));
    	}
    }
    
    /*
     * displays a device button with device button object information.
     */
    private void displayDevice(Device dev,DeviceButton devButton){
    	devButton.setDevice(dev);
    	devButton.setVisibility(View.VISIBLE);
    	devButton.setText(dev.getName());
    	setDevButtonIcon(devButton,dev.getIconResId());	
    	
    }
    
    /*
     * displays add device button.
     */
    private void displayAddDevice(DeviceButton devButton){
    	devButton.setDevice(null);
    	devButton.setVisibility(View.VISIBLE);
    	devButton.setText(R.string.add_device);
    	setDevButtonIcon(devButton,R.drawable.img_add);	
    }
    
    /*
     * Sets the device button Icon
     */
    private void setDevButtonIcon(DeviceButton devButton,int resId)
    {
    	Drawable topD=this.getResources().getDrawable(resId);
    	if(topD!=null)
    	{
	    	topD.setBounds(0, 0, topD.getMinimumWidth(), topD.getMinimumHeight());
	    	devButton.setCompoundDrawables(null, topD, null, null);	
    	}
    }
    
    private OnClickListener mDevButtonOnClickListener=new OnClickListener(){

		@Override
		public void onClick(View v) {
			DeviceButton devButton=(DeviceButton)v;
			
			//identifies add device button or device button
			if(devButton.getDevice()!=null){
				/* crate a intent object, then call the device activity class */         
				Intent devKeyIntent = new Intent(DeviceActivity.this, DeviceKeyActivity.class);
			    Bundle bdl=new Bundle();
			    bdl.putSerializable(DeviceKeyActivity.DEVICE_OBJECT, devButton.getDevice());
			    devKeyIntent.putExtras(bdl);
				startActivity(devKeyIntent);            

			}else{
				Device dev=new Device();
				dev.setIconName("icon_device_tv_theather2");
				dev.setName("Theather2");
				RemoteUi.getHandle().getChildren().add(dev);
				initDeviceIconId();
				displayDevices();
			}
						
		}
    	
    };
    
    /*
     * AsyncTask for App Initializing.
     */
    private class InitAppTask extends android.os.AsyncTask<Integer, Integer, Integer> {
	
    	private ProgressDialog mProgressDialog;


		@Override
    	protected Integer doInBackground(Integer... params) {
    		// TODO Auto-generated method stub
			//copys the UI XML file to sdcard.
            FileManager.saveAs(DeviceActivity.this, R.raw.remote, 
					RemoteUi.INTERNAL_DATA_DIRECTORY, RemoteUi.UI_XML_FILE);
			
            XmlManager xm=new XmlManager();
            xm.loadData(RemoteUi.getHandle(), RemoteUi.INTERNAL_DATA_DIRECTORY+"/"+RemoteUi.UI_XML_FILE);
            
            initData();      
              
    		return 0;
    	}

    	@Override
        protected void onPreExecute() { 		
            mProgressDialog = ProgressDialog.show(DeviceActivity.this,     
                    "",getResources().getText(R.string.initial_waiting), true);
        }

    	@Override
        protected void onProgressUpdate(Integer... progress) {
           
        }
    	
    	@Override
        protected void onPostExecute(Integer result) {
    		
    		displayDevices();
    		
    		mProgressDialog.dismiss();
        }
    }
}