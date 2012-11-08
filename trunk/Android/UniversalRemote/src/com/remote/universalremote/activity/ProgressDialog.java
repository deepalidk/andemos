package com.remote.universalremote.activity;

import com.remote.universalremote.activity.R;
import com.remote.universalremote.activity.R.layout;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

public class ProgressDialog extends Dialog {
    public ProgressDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.progressbar);         
    }
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  {  
    	/*skip back key*/
        if (keyCode == KeyEvent.KEYCODE_BACK) {   
          //DO SOMETHING      
        	return true;
        }else{
            return super.onKeyDown(keyCode, event);  
        }
    }  
}