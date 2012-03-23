/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.common.FileManager;
import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.component.BottomBarButton;
import com.remotec.universalremote.activity.component.DeviceButton;
import com.remotec.universalremote.activity.component.KeyButton;
import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.persistence.XmlManager;

/*
 *Displays device key for UI. 
 */
public class DeviceKeyActivity extends Activity {
	
	// Debugging Tags 
	private static final String TAG = "DeviceKeyActivity";
	private static final boolean D = false;
	
	//exchanges device object with deviceactivity.  
	public static final String DEVICE_OBJECT="DEVICE_OBJECT";
	
	private Device mDevice;
	
	/*
	 * all bottom Bar buttons in key layout
	 */
	private List<BottomBarButton> mBottomBarButtonList=null;
	
	/*
	 * all key buttons in key layout
	 */
	private List<KeyButton> mKeyButtonList=null;
	
	//Control key View Group
	private ViewGroup mVgControl=null;
	
	//Menu key View Group
	private ViewGroup mVgMenu=null;
	
	//Media key View Group
	private ViewGroup mVgMedia=null;
	
	//Title
	private TextView mTitleLeft=null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.devicekey);    
        
        Bundle bdl = getIntent().getExtras();
        mDevice= (Device)bdl.getSerializable(DEVICE_OBJECT);
        
        mTitleLeft=(TextView) this.findViewById(R.id.devicekey_title_left_text);
        mTitleLeft.setText(mDevice.getName());
        
        mVgControl=(ViewGroup)this.findViewById(R.id.id_key_control_layout);
        mVgMenu=(ViewGroup)this.findViewById(R.id.id_key_menu_layout);
        mVgMedia=(ViewGroup)this.findViewById(R.id.id_key_media_layout);
        
        //Initializing data.
        InitAppTask initor= new InitAppTask();
        initor.execute(0);
        
    }
    
   
    /*
     * inits the data for device activity
     */
    private void initData(){
    	
   	
    	/*
    	 * finds all BottomBar Buttons
    	 */
    	mBottomBarButtonList=new ArrayList<BottomBarButton>();
    	
        ViewGroup vg=(ViewGroup)findViewById(R.id.id_key_bottombar);        
        
        findBottomBarButtons(vg,mBottomBarButtonList,mBottomBarOnClickListener);
    	
    	/*
    	 * finds all key Buttons
    	 */
        mKeyButtonList=new ArrayList<KeyButton>();
    	
        vg=(ViewGroup)findViewById(R.id.id_key_layout);        
        
        findKeyButtons(vg,mKeyButtonList,mKeyOnClickListener);
        
    }
    
	/*
	 * finds all BottomBar Buttons
	 */
	private void findBottomBarButtons(ViewGroup vg, List<BottomBarButton> bList,OnClickListener listener) {
    	
    	for(int i=0;i<vg.getChildCount();i++){
        	View v=vg.getChildAt(i);
            
        	if( v instanceof BottomBarButton){
        		bList.add((BottomBarButton)v);
        		v.setOnClickListener(listener);
        	}
        	else if(v instanceof ViewGroup)
        	{
        		findBottomBarButtons((ViewGroup) v,bList,listener);
        	}
        }
    }
	
	/*
	 * finds all key Buttons
	 */
	private void findKeyButtons(ViewGroup vg, List<KeyButton> bList,OnClickListener listener) {
    	
    	for(int i=0;i<vg.getChildCount();i++){
        	View v=vg.getChildAt(i);
            
        	if( v instanceof KeyButton){
        		bList.add((KeyButton)v);
        		v.setOnClickListener(listener);
        	}
        	else if(v instanceof ViewGroup)
        	{
        		findKeyButtons((ViewGroup) v,bList,listener);
        	}
        }
    }
    
   
    /*
     * bottom bar click listener.
     */
    private OnClickListener mBottomBarOnClickListener=new OnClickListener(){

		@Override
		public void onClick(View v) {

			/*
			 * change the btn state, 
			 * and so the background will change.
			 */
			for(Button btn:DeviceKeyActivity.this.mBottomBarButtonList)
			{
				btn.setEnabled(true);
			}
			
			v.setEnabled(false);
			
			if(v.getId()==R.id.btn_bottombar_control)
			{
				mVgControl.setVisibility(View.VISIBLE);	
				mVgMenu.setVisibility(View.GONE);
				mVgMedia.setVisibility(View.GONE);
			}else if(v.getId()==R.id.btn_bottombar_menu){
				mVgControl.setVisibility(View.GONE);	
				mVgMenu.setVisibility(View.VISIBLE);
				mVgMedia.setVisibility(View.GONE);
			}else if(v.getId()==R.id.btn_bottombar_media){
				mVgControl.setVisibility(View.GONE);	
				mVgMenu.setVisibility(View.GONE);
				mVgMedia.setVisibility(View.VISIBLE);
			}
			
		}
    	
    };
    
    /*
     * Key click listener.
     */
    private OnClickListener mKeyOnClickListener=new OnClickListener(){

		@Override
		public void onClick(View v) {

			
		}
    	
    };
    
    /*
     * AsyncTask for App Initializing.
     */
    private class InitAppTask extends android.os.AsyncTask<Integer, Integer, Integer> {
	
    	private ProgressDialog mProgressDialog;


		@Override
    	protected Integer doInBackground(Integer... params) {

			initData();
              
    		return 0;
    	}

    	@Override
        protected void onPreExecute() { 		
            mProgressDialog = ProgressDialog.show(DeviceKeyActivity.this,     
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