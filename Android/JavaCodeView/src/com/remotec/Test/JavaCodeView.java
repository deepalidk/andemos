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
       
//        LinearLayout lo = new LinearLayout(this); //����Ϊ100px����Ϊ����Ӧ��С�ĸ߶�
//        setContentView(lo);
//        // setOrientation(VERTICAL); ���ò���Ϊ��ֱ
//
//        TextView textControl = new TextView(this);//�����һ��XXXLayout.������LinearLayoutΪView�Ļ���ʱ����thisӦ�û���Ϊ���������Context 
//        textControl.setText("Android��������ӭ��"); 
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