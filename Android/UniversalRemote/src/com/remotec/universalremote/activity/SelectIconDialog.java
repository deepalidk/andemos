/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *
 * Author: Walker
 */
package com.remotec.universalremote.activity;

import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;
import com.remotec.universalremote.activity.component.ImageAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

/*
 *Provides a icon gridview to User to select an Icon.
 */
public class SelectIconDialog extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.select_icon_dialog); 
        
        GridView gridview=(GridView)findViewById(R.id.image_gridview);//找到main.xml中定义gridview 的id
        gridview.setAdapter(new ImageAdapter(this));//调用ImageAdapter.java
        gridview.setOnItemClickListener(new OnItemClickListener(){//监听事件

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			 Toast.makeText(SelectIconDialog.this, ""+position,Toast.LENGTH_SHORT).show();//显示信息;
		}
        });

    }
    
}