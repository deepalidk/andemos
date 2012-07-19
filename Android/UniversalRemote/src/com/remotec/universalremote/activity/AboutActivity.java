package com.remotec.universalremote.activity;

import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.activity.R.layout;
import com.remotec.universalremote.data.RemoteUi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

public class AboutActivity extends Activity {
 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
        // Setup the window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about_activity);
        TextView tv=(TextView)this.findViewById(R.id.txtComment);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        TextView tvAppVer=(TextView)this.findViewById(R.id.textView_appVer);
        TextView tvExtVer=(TextView)this.findViewById(R.id.textView_extVer);
        
        
		String msg=String.format("Version:  %s", getVersionName());
		tvAppVer.setText(msg);
		
		if(RemoteUi.getHandle().getActiveExtender()!=null){
			msg=String.format("Extender: %s",RemoteUi.getHandle().getActiveExtender().getVersion().toUpperCase());
		}else{
			msg=String.format("Extender: not connected");
		}
		
		tvExtVer.setText(msg);
        
		}
	
	/*
	 * get Current apk version
	 */
	private String getVersionName() {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		String version = "";
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return version;
	}
}