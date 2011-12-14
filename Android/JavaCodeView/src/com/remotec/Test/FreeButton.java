package com.remotec.Test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

public class FreeButton extends View {

	public FreeButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {
    	
    	canvas.drawColor(Color.BLUE);
    	canvas.clipRect(120, 100, 240, 280);
    }

}
