/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *
 * Author: Walker
 */
package com.remotec.zremote.activity;

import com.remotec.zremote.activity.R;
import com.remotec.zremote.activity.component.ImageAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

/*
 *Provides a icon gridview to User to select an Icon.
 */
public class SelectIconDialog extends Activity {
	
	public static final String IMAGE_RES_ID = "image_res_id";
	Button mBtnCancel;
	private GridView mGridview;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.select_icon_dialog); 
        
        mGridview=(GridView)findViewById(R.id.gridview_icons);//找到main.xml中定义gridview 的id
        mGridview.setAdapter(new ImageAdapter(this));//调用ImageAdapter.java
        mGridview.setOnItemClickListener(mOnItemClick);
        
        mBtnCancel=(Button)findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(mOnCancelClick);
    }
    
    /*
     * handles cancel button click event.
     */
    private OnClickListener mOnCancelClick=new OnClickListener(){
		@Override
		public void onClick(View v) {
			SelectIconDialog.this.setResult(Activity.RESULT_CANCELED);
			SelectIconDialog.this.finish();
		}		 
    };
    
    /*
     * handles Item click event.
     */
    private OnItemClickListener mOnItemClick=new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

	    		Intent intent = new Intent(); // 申请Bundle变量

	    		try {
	    			intent.putExtra(IMAGE_RES_ID,(Integer)view.getTag());
	    			setResult(Activity.RESULT_OK, intent);
	    			SelectIconDialog.this.finish();
	    		} catch (Exception ex) {

	    		}		
	    	}
		
    };
    
     
}