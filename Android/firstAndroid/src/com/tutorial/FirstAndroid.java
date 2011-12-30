package com.tutorial;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
 
public class FirstAndroid extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

//        findViewById(R.id.myTextView).setVisibility(View.INVISIBLE);
//        TextView myView=(TextView)this.findViewById(R.id.myTextView);
//        String str=(String)myView.getText();
    }
}