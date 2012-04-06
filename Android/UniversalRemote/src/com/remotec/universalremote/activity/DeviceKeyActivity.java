/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
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
import com.remotec.universalremote.data.Key;
import com.remotec.universalremote.data.RemoteUi;
import com.remotec.universalremote.irapi.BtConnectionManager;
import com.remotec.universalremote.irapi.IrApi;
import com.remotec.universalremote.persistence.XmlManager;

/*
 *Displays device key for UI. 
 */
public class DeviceKeyActivity extends Activity {
	
	// Debugging Tags 
	private static final String TAG = "DeviceKeyActivity";
	private static final boolean D = false;
	
	//dialog ids
	private static final int PROGRESS_DIALOG = 0;
	
	private Device mDevice;
	
	private ProgressDialog mProgressDialog;
	
	/*
	 * all bottom Bar buttons in key layout
	 */
	private List<BottomBarButton> mBottomBarButtonList=null;
	
	/*
	 * all key buttons in key layout
	 */
	private Map<Integer,KeyButton> mKeyButtonMap=null;
	
	//Control key View Group
	private ViewGroup mVgControl=null;
	
	//Menu key View Group
	private ViewGroup mVgMenu=null;
	
	//Media key View Group
	private ViewGroup mVgMedia=null;
	
	//Title
	private TextView mTitleLeft=null;	
	
	//Textviews
	private TextView mVolLabel=null;
	
	//Textviews
	private TextView mChLabel=null;
	
	//Textviews
	private TextView mBrLabel=null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //remove the tile.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.devicekey);    
        
        
        //Initializing data.
        InitAppTask initor= new InitAppTask();
        initor.execute(0);
        
    }
    
    protected Dialog onCreateDialog(int id) {  
        switch(id) {  
        case PROGRESS_DIALOG:   
        	 mProgressDialog =new ProgressDialog(this);
        	 mProgressDialog.setTitle("");
        	 mProgressDialog.setMessage(getResources().getText(R.string.initial_waiting));
        	 mProgressDialog.setIndeterminate(true);
        	 mProgressDialog.setCanceledOnTouchOutside(false);
        	 mProgressDialog.setCancelable(false);
            return mProgressDialog;  
        default:  
            return null;  
        }  
    } 
   
    /*
     * inits the data for device activity
     */
    private void initData(){
    	
        Bundle bdl = getIntent().getExtras();
        
        /*
         * global current active device store in RemoteUi.
         */
        mDevice= RemoteUi.getHandle().getActiveDevice();
   	
        mTitleLeft=(TextView) this.findViewById(R.id.devicekey_title_left_text);
        mTitleLeft.setText(mDevice.getName());
        
        mChLabel=(TextView)findViewById(R.id.key_id_ch);
        mVolLabel=(TextView)findViewById(R.id.key_id_vol);
        mBrLabel=(TextView)findViewById(R.id.key_id_br);
        
        mVgControl=(ViewGroup)this.findViewById(R.id.id_key_control_layout);
        mVgMenu=(ViewGroup)this.findViewById(R.id.id_key_menu_layout);
        mVgMedia=(ViewGroup)this.findViewById(R.id.id_key_media_layout);
    	
    	/*
    	 * finds all BottomBar Buttons
    	 */
    	mBottomBarButtonList=new ArrayList<BottomBarButton>();
    	
        ViewGroup vg=(ViewGroup)findViewById(R.id.id_key_bottombar);        
        
        findBottomBarButtons(vg,mBottomBarButtonList,mBottomBarOnClickListener);
    	
    	/*
    	 * finds all key Buttons
    	 */
        mKeyButtonMap=new Hashtable<Integer,KeyButton>();
    	
        vg=(ViewGroup)findViewById(R.id.id_key_layout);        
        
        findKeyButtons(vg,mKeyButtonMap,mKeyOnClickListener);
        
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
	public static void findKeyButtons(ViewGroup vg, Map<Integer,KeyButton> bMap,OnClickListener listener) {
    	
    	for(int i=0;i<vg.getChildCount();i++){
        	View v=vg.getChildAt(i);
            
        	if( v instanceof KeyButton){
        		KeyButton btn=(KeyButton)v;
        		if(btn.getKeyId()!=-1)
        		{
	        		bMap.put(btn.getKeyId(), btn);
	        		v.setOnClickListener(listener);
        		}
        	}
        	else if(v instanceof ViewGroup)
        	{
        		findKeyButtons((ViewGroup) v,bMap,listener);
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

			if(RemoteUi.getEmulatorTag())return;
			IrApi irController=IrApi.getHandle();
			
			if (irController != null) {
                Key tempKey=(Key) v.getTag();
                
                if(tempKey!=null)
                {
						boolean result = irController.transmitPreprogramedCode(
						(byte) 0x81, (byte) (mDevice.getIrCode() % 10), mDevice.getIrCode() / 10,
						(byte) tempKey.getKeyId());
                }

			}
		}
    	
    };
    
    /*
     * displays the devices on the screen.
     */
    private void displayKeys(){
    	
    	List<Key> keyList=mDevice.getChildren();
    	
    	for(KeyButton keyBtn:this.mKeyButtonMap.values()){
    		keyBtn.setVisibility(View.INVISIBLE);
    	}
    	
    	for(Key key:keyList){
    		
    			if(mKeyButtonMap.containsKey(key.getKeyId())){
    				
    				KeyButton keyBtn=mKeyButtonMap.get(key.getKeyId());
    				
    				if(!keyBtn.getIsIconButton()){
    				   keyBtn.setText(key.getText());
    				}
    				
    				if(key.getVisible()){
    				 keyBtn.setVisibility(View.VISIBLE);
    				}else{
    				 keyBtn.setVisibility(View.INVISIBLE);
    				}
    				
    				keyBtn.setTag(key);
    			}
    		
    	}
    	    
    	displayLabel();
    }
    
    /*
     * set the ch , vol, br label
     */
    private void displayLabel(){
    	KeyButton tempAddBtn;
    	KeyButton tempMinusBtn; 
    	Key tempKey;
    	
    	/*set void label*/
    	tempAddBtn=	mKeyButtonMap.get(getResources().getInteger(R.integer.key_id_vol_up));
    	tempMinusBtn=mKeyButtonMap.get(getResources().getInteger(R.integer.key_id_vol_up));
    
    	//any one is visible then label is visible
    	if(tempAddBtn.getVisibility()==View.VISIBLE||tempMinusBtn.getVisibility()==View.VISIBLE){
    		tempKey=(Key)tempAddBtn.getTag();
    	    mVolLabel.setText(tempKey.getText());
    	}else{
    		mVolLabel.setVisibility(View.INVISIBLE);
    	}
    	
    	/*set ch label*/
    	tempAddBtn=	mKeyButtonMap.get(getResources().getInteger(R.integer.key_id_ch_up));
    	tempMinusBtn=mKeyButtonMap.get(getResources().getInteger(R.integer.key_id_ch_up));
    
    	//any one is visible then label is visible
    	if(tempAddBtn.getVisibility()==View.VISIBLE||tempMinusBtn.getVisibility()==View.VISIBLE){
    		tempKey=(Key)tempAddBtn.getTag();
    	    mChLabel.setText(tempKey.getText());
    	}else{
    		mChLabel.setVisibility(View.INVISIBLE);
    	}
    	
    	/*set void label*/
    	tempAddBtn=	mKeyButtonMap.get(getResources().getInteger(R.integer.key_id_br_up));
    	tempMinusBtn=mKeyButtonMap.get(getResources().getInteger(R.integer.key_id_br_up));
    
    	//any one is visible then label is visible
    	if(tempAddBtn.getVisibility()==View.VISIBLE||tempMinusBtn.getVisibility()==View.VISIBLE){
    		tempKey=(Key)tempAddBtn.getTag();
    	    mBrLabel.setText(tempKey.getText());
    	}else{
    		mBrLabel.setVisibility(View.INVISIBLE);
    	}
    
    }
    
    
    /*
     * AsyncTask for App Initializing.
     */
    private class InitAppTask extends android.os.AsyncTask<Integer, Integer, Integer> {


		@Override
    	protected Integer doInBackground(Integer... params) {

			initData();
              
    		return 0;
    	}

    	@Override
        protected void onPreExecute() { 		
    		DeviceKeyActivity.this.showDialog(DeviceKeyActivity.PROGRESS_DIALOG);
        }

    	@Override
        protected void onProgressUpdate(Integer... progress) {
           
        }
    	
    	@Override
        protected void onPostExecute(Integer result) {
    		displayKeys();
    		DeviceKeyActivity.this.removeDialog(DeviceKeyActivity.PROGRESS_DIALOG);
            
    	}
    }
    
}