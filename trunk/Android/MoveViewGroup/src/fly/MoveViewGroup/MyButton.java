package fly.MoveViewGroup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.TextView;

public class MyButton extends TextView {

	public MyButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {

    	
    	Paint paintText=new Paint();
//    	paintText.setDither(false);
    	paintText.setAntiAlias(true);
    	paintText.setColor(Color.WHITE);
    	paintText.setShadowLayer(10, 30, 30, Color.WHITE);
//    	paintText.setShadowLayer(5, 10, 10, Color.WHITE);
    	
    	paintText.setAlpha(100);
//        canvas.drawRect(getScrollX(), getScrollY(),getScrollX()+ 30, getScrollY()+30, paintText); 	
    	
//        canvas.drawRect(100, 300,110, 310, paintText);
        
        RectF rc=new RectF();
        rc.set(getScrollX(), getScrollY(),getScrollX()+ 50, getScrollY()+50);
        canvas.drawRoundRect(rc, 5, 5, paintText);
        
        super.onDraw(canvas);
    	

    }
	
}
