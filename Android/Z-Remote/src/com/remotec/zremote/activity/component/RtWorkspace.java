package com.remotec.zremote.activity.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
 
public class RtWorkspace extends ViewGroup {
   
	public RtWorkspace(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    public RtWorkspace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			child.measure(r - l, b - t);
			child.layout(0, 0, r - l, b - t);
		}
	}
}
