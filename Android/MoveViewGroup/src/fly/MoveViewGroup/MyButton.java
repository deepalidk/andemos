package fly.MoveViewGroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
//import android.view.View.AttachInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;

public class MyButton extends TextView {
	private static boolean D=true;
    private static String TAG="MyButton";
    
    private boolean mIsFixPosition=false;
    
     
    
	public MyButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.setClickable(true);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyButton);

        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.MyButton_fix_position:
                    mIsFixPosition = a.getBoolean(attr,true);
                    break;
                case R.styleable.MyButton_left:
                	left= a.getDimensionPixelOffset(attr,0);
                	break;
                case R.styleable.MyButton_right:
                	right= a.getDimensionPixelOffset(attr,0);
                	break;
                case R.styleable.MyButton_top:
                	top= a.getDimensionPixelOffset(attr,0);
                	break;
                case R.styleable.MyButton_bottom:
                	bottom= a.getDimensionPixelOffset(attr,0);
                	break;
            }
        }
        
        this.setFrame(left, top, right, bottom);
        
        a.recycle();
                    
    }

    public boolean getIsFixPostion()
    {
    	return mIsFixPosition;
    }
    
    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {
    	if(D)Log.d(TAG, "ondraw");
    	Paint paintText=new Paint();
//    	paintText.setDither(false);
    	paintText.setAntiAlias(true);
    	paintText.setColor(Color.WHITE);
//    	paintText.setShadowLayer(10, 30, 30, Color.WHITE);
//    	paintText.setShadowLayer(5, 10, 10, Color.WHITE);
    	
    	paintText.setAlpha(100);
//        canvas.drawRect(getScrollX(), getScrollY(),getScrollX()+ 30, getScrollY()+30, paintText); 	
    	
//        canvas.drawRect(100, 300,110, 310, paintText);
        
    	if(D)Log.d(TAG, "this.l="+ this.getLeft()+" this.t="+this.getTop()+" this.r="+getRight()+" this.b="+getBottom());
        RectF rc=new RectF();
        rc.set(0, 0,getRight()-getLeft(),getBottom()-getTop());
        if(isPressed())
        {
        canvas.drawRoundRect(rc, 5, 5, paintText);
        }
               
        super.onDraw(canvas);
    }
    
	@Override
	public void computeScroll() {
		
//		super.setFrame(0, 0, 120, 120);
		
//		if(D)Log.d(TAG, "this.x="+this.getLeft()+" this.y="+this.getTop());
//		    this.layout(0+this.getParent(), 0, 100, 100);
//            scrollTo(this.getLeft(), this.getTop()); 
	}
	
}
