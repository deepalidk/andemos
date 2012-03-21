/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.activity;


import com.common.FileManager;
import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.persistence.XmlManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/*
 *Displays device for UI. 
 */
public class DeviceActivity extends Activity {
	
	// Debugging Tags 
	private static final String TAG = "UniversalRemoteActivity";
	private static final boolean D = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device);   
      
        //Initializing data.
        (new InitAppTask()).execute(0);
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
    		mProgressDialog.dismiss();
        }
    }
}