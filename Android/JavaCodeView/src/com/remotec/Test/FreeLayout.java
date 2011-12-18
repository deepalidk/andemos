package com.remotec.Test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ViewGroup;

public class FreeLayout extends ViewGroup {
	Paint paintText;
	
	public FreeLayout(Context context) {
		super(context);
		this.setWillNotDraw(false);
		// TODO Auto-generated constructor stub
		paintText = new Paint();
		paintText.setColor(Color.GREEN);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}
	
    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	
    	this.dispatchDraw(canvas);
    
//    	canvas.drawColor(Color.WHITE);
//    	canvas.clipRect(120, 100, 240, 280);
//    	canvas.drawText("I'm FreeLayout", 10, 10, paintText);
    	
    }

}
