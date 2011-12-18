package com.remotec.Test;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class JavaCodeView extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
//        LinearLayout lo = new LinearLayout(this); //宽度为100px，高为自适应最小的高度
//        setContentView(lo);
//        // setOrientation(VERTICAL); 设置布局为垂直
//
//        TextView textControl = new TextView(this);//如果从一个XXXLayout.，比如LinearLayout为View的基类时这里this应该换成为创建改类的Context 
//        textControl.setText("Android开发网欢迎您"); 
//    
//        FreeButton fb=new FreeButton(this);
//        fb.setWillNotDraw(false);
//        addContentView(fb, new LinearLayout.LayoutParams(1000, 1000) );
        
        FreeLayout fl=new FreeLayout(this);
        setContentView(fl);
        
        FreeButton fb=new FreeButton(this);
        fl.addView(fb);
        
//        addContentView(fb);


    }
}