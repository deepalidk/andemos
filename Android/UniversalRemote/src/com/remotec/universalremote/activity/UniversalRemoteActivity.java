package com.remotec.universalremote.activity;


import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

public class UniversalRemoteActivity extends Activity {
	
	// Debugging 
	private static final String TAG = "UniversalRemoteActivity";
	private static final boolean D = false;
	private static final boolean emulatorTag = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);   
      
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

            try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    		return null;
    	}

    	@Override
        protected void onPreExecute() { 		
            mProgressDialog = ProgressDialog.show(UniversalRemoteActivity.this,     
                    "", "Please wait...", true);
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