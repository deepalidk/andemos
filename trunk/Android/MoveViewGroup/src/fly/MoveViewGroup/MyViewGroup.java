
package fly.MoveViewGroup;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.AbsoluteLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class MyViewGroup extends AbsoluteLayout {
	private static boolean D=true;
    private static String TAG="ViewGroup";
	private int mLastLeftEdge;// the screen left edge when finger down.
	private float mLastMotionPosX; //record last posX since the finger move.
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mScreenWidth=480;
	private int mMaxPagePosX=480;
	private int mTouchSlop;
	private Scroller mScroller;
	Context mContext;
	
 
	public MyViewGroup(Context context) {
		super(context);
        	
		initView(context);
	}
	
    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        initView(context);
        
    }
    
    private void initView(Context context)
    {
    	
		mContext = context;
		mScroller= new Scroller(getContext());
		 
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		// 获得可以认为是滚动的距离
		mTouchSlop = 8;
	 	
//		// 添加子View
//		for (int i = 0; i < 1; i++) { 
//			final MyButton 	MButton = new MyButton(context);
//		   
//			MButton.setBackgroundResource(R.drawable.blue_btn_circle_50_50); 
//			MButton.getBackground().setAlpha(255);
//			MButton.setText("" + (i + 1));
//			MButton.setTextColor(Color.BLACK);
//			MButton.setOnClickListener(new OnClickListener() {
//				
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					Toast.makeText(mContext, MButton.getText(), Toast.LENGTH_SHORT).show(); 
//				}
//			});
//			addView(MButton);
//		}
    }

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) { 
			if(D)Log.d(TAG, "computeScroll "+mScroller.getCurrX()+" "+mScroller.getCurrY());
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY()); 
            postInvalidate(); 
        } 
		else
		{
			if(D)Log.d(TAG, "computeScroll(FALSE) "+mScroller.getCurrX()+" "+mScroller.getCurrY());
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final float x = ev.getX();
		int touchState=TOUCH_STATE_REST;
		
		if(D)Log.d(TAG, "onInterceptTouchEvent ev.getAction=" + ev.getAction());
		
		switch (ev.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			mLastLeftEdge=this.getScrollX();
			mLastMotionPosX=x;
			break;
		case MotionEvent.ACTION_MOVE:
			final int yDiff = (int) Math.abs(mLastMotionPosX-x);
			
			boolean yMoved = yDiff > mTouchSlop;
			// 判断是否是移动
			if (yMoved) {
				touchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		} 
		return touchState != TOUCH_STATE_REST;
	}

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	
//    	Paint paintText=new Paint();
////    	paintText.setDither(false);
//    	paintText.setColor(Color.BLUE);
//    	paintText.setAntiAlias(true);
//    	paintText.setShadowLayer(10, 30, 30, Color.WHITE);
////    	paintText.getShader().
//    	
//    	paintText.setAlpha(0);
//        canvas.drawRect(getScrollX()+30, 30,getScrollX()+ 100, 100, paintText);
//    	
//        RectF rc=new RectF();
//        rc.set(150, 150,300, 300);
//        paintText.setARGB(0, 255, 255, 255);
//        
//        canvas.drawRoundRect(rc, 30, 30, paintText);
//   	    canvas.drawText("I'm FreeLayout", getScrollX()+30, 30, paintText);   
    	
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		if(D)Log.d(TAG, "onTouchEvent ev.getAction=" + ev.getAction());
		
		final float x = ev.getX();
		switch (ev.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			mLastLeftEdge=this.getScrollX();
			mLastMotionPosX=x;
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
			break;
		case MotionEvent.ACTION_MOVE:
		{
				// 随手指 拖动的代码
				int deltaX = 0;
				deltaX = (int) (x-mLastMotionPosX);
				
				if(D)Log.d(TAG, "deltaX=" + deltaX);
				scrollBy(-deltaX,0);
				
				if((getScrollX()< -mScreenWidth/3))
				{
					scrollBy(deltaX,0);
				}
				
				if((getScrollX()>(mMaxPagePosX +mScreenWidth/3)))
				{
					scrollBy(deltaX,0);
				}
				
				mLastMotionPosX=x;
		}
			break;
		case MotionEvent.ACTION_UP:			
			//Move to the threshold of page
			int curX=this.getScrollX();
			
			if(curX<0)
			{
				snatchTo(0, 0);
			}
			else if(curX>mMaxPagePosX)
			{
				snatchTo(mMaxPagePosX,0);
			}
			else
			{   
				int scrollX=((curX%mScreenWidth)>(mScreenWidth/2))?(curX-curX%mScreenWidth+mScreenWidth):(curX-curX%mScreenWidth);
			   
				snatchTo(scrollX,0);
			}

			break;
		}
		return true;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		 //随手指 快速拨动的代码
		if(D)Log.d(TAG, "onFling");
		if(D)Log.d(TAG, "VelocityY"+velocityX);

        int targetX=0;
 
        
		if(velocityX<0) //r 2 l
		{
			targetX=mLastLeftEdge+mScreenWidth;
			if(targetX>mMaxPagePosX)
			{
				targetX=mMaxPagePosX;
			}
			this.snatchTo(targetX,0);
		}
		else  // l 2 r
		{
			targetX=mLastLeftEdge-mScreenWidth;
			if(targetX<0)
			{
				targetX=0;
			}
			this.snatchTo(targetX,0);
		}
		
		return false;
	}

	
	   /**
     * This is called in response to an internal scroll in this view (i.e., the
     * view scrolled its own contents). This is typically as a result of
     * {@link #scrollBy(int, int)} or {@link #scrollTo(int, int)} having been
     * called.
     *
     * @param l Current horizontal scroll origin.
     * @param t Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
           super.onScrollChanged(l,t,oldl,oldt);
           if(D)Log.d(TAG, "onScrollChanged");
   
//           int count = getChildCount();
//
//           for (int i = 0; i < count; i++) {
//               View child = getChildAt(i);
//               if (child.getVisibility() != GONE) {
//
//                   AbsoluteLayout.LayoutParams lp =
//                           (AbsoluteLayout.LayoutParams) child.getLayoutParams();
//
//                   int childLeft = this.getPaddingLeft() + lp.x;
//                   int childTop = this.getPaddingTop() + lp.y;
//                   child.layout(childLeft, childTop,
//                           childLeft + child.getMeasuredWidth(),
//                           childTop + child.getMeasuredHeight());
//               }
//           }
           
           
      
    }
	
	protected void snatchTo(int x,int y)
	{
		if(D)Log.d(TAG, "SnatchTo x="+x+" y="+y+" startX="+getScrollX());

		int startX=getScrollX();
		
		if(!mScroller.isFinished())
		{
			mScroller.abortAnimation();
		}
		mScroller.startScroll(startX, getScrollY(), x-startX, getScrollY(), Math.abs(x-startX)*2);
		invalidate();
	}


}