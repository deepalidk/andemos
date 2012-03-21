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
import android.os.Bundle;
import android.view.View;
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
    	mDevButtonList=new ArrayList<DeviceButton>();
    	
        ViewGroup tbLayout=(ViewGroup)findViewById(R.id.device_table);
        
        findDeviceButtons(tbLayout,mDevButtonList);
    	
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
    		Extender ext=RemoteUi.getHandle().getChildren().get(0);
    		
    		for(int i=0;i<ext.getChildren().size();i++)
    		{
    			displayDevice(ext.getChildren().get(i),mDevButtonList.get(i));
    		}
           
    		//add device button
    		mDevButtonList.get(ext.getChildren().size()).setText(R.string.add_device);
    		
    		for(int i=ext.getChildren().size()+1;i<mDevButtonList.size();i++)
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
           
    		mDevButtonList.get(0).setText(R.string.add_device);
    	}
    }
    
    private void displayDevice(Device dev,DeviceButton devButton)
    {
    	devButton.setTag(dev);
    	devButton.setText(dev.getName());
    }

    
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
            
            RemoteUi.getHandle().setVersion("2.0.0");
            xm.saveData(RemoteUi.getHandle(), RemoteUi.INTERNAL_DATA_DIRECTORY+"/"+RemoteUi.UI_XML_FILE);
            
            initData();
            
            try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              
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