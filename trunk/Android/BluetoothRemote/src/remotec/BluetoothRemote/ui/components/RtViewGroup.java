package remotec.BluetoothRemote.ui.components;


import java.util.List;

import remotec.BluetoothRemote.activities.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
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

public class RtViewGroup extends AbsoluteLayout implements View.OnClickListener {
	private static boolean D = false;
	private static String TAG = "ViewGroup";
	
	 /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */
    private static final int SNAP_VELOCITY = 1000;
	private int mLastLeftEdge;// the screen left edge when finger down.
	private float mLastMotionPosX; // record last posX since the finger move.
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mScreenWidth = 600;
	private int mMaxPagePosX = 600;
	private int mCurrentPage = 1;
	private int mMaxPageNum=1;
	private eTouchState mTouchState = eTouchState.none;
	private int mTouchSlop;
	private Scroller mScroller;
	private ViewGroup mDotPanel;
	private boolean bFirstInit=true;
	Context mContext;
	private VelocityTracker mVelocityTracker;
	private float mMaximumVelocity;
	
	private enum eSnatch
	{
		last,
		current,
		next
	};
	
	private enum eTouchState
	{
		none,
		down,
		move,
		up,
		flipping
	};

	public RtViewGroup(Context context) {
		super(context);

		initView(context);
	}

	public RtViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context);
	}

	private void initView(Context context) {

		mContext = context;
		mScroller = new Scroller(getContext());
		
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		// 获得可以认为是滚动的距离
		mTouchSlop = configuration.getScaledTouchSlop();

	}
	
    @Override
    protected void onLayout(boolean changed, int l, int t,
            int r, int b) {
    	
    	super.onLayout(changed, l, t, r, b);

    	if(bFirstInit)
    	{
    		mDotPanel = (ViewGroup)findViewById(R.id.dot_holder);
    		
    		if(mDotPanel!=null)
    		{
    		 mMaxPageNum=mDotPanel.getChildCount();
    		 for(int i=0;i<mMaxPageNum;i++)
    		 {
    			 View view= mDotPanel.getChildAt(i);
    		     view.setOnClickListener(this);		 
    		 }
    		}
        	
    		mScreenWidth=r-l;
    		mMaxPagePosX=mScreenWidth;
    		
    		bFirstInit=false;
    	}
      }
   

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (D)
				Log.d(TAG, "computeScroll " + mScroller.getCurrX() + " "
						+ mScroller.getCurrY());
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else {
			if (D)
				Log.d(TAG, "computeScroll(FALSE) " + mScroller.getCurrX() + " "
						+ mScroller.getCurrY());
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final float x = ev.getX();
		int touchState = TOUCH_STATE_REST;

		if (D)
			Log.d(TAG, "onInterceptTouchEvent ev.getAction=" + ev.getAction());

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastLeftEdge = this.getScrollX();
			mLastMotionPosX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			final int yDiff = (int) Math.abs(mLastMotionPosX - x);

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
	 * @param canvas
	 *            the canvas on which the background will be drawn
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (D)
			Log.d(TAG, "onTouchEvent ev.getAction=" + ev.getAction());

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
		
		final float x = ev.getX();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastLeftEdge = this.getScrollX();
			mLastMotionPosX = x;
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE: {
			// 随手指 拖动的代码
			int deltaX = 0;
			deltaX = (int) (x - mLastMotionPosX);

			if (D)
				Log.d(TAG, "deltaX=" + deltaX);
			scrollBy(-deltaX, 0);

			if ((getScrollX() < -mScreenWidth / 3)) {
				scrollBy(deltaX, 0);
			}

			if ((getScrollX() > (mMaxPagePosX + mScreenWidth / 3))) {
				scrollBy(deltaX, 0);
			}

			mLastMotionPosX = x;
		}
			break;
		case MotionEvent.ACTION_UP:
			// Move to the threshold of page
            
			  final VelocityTracker velocityTracker = mVelocityTracker;
              velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
              int velocityX = (int) velocityTracker.getXVelocity();

              if (velocityX > SNAP_VELOCITY) {
                  // Fling hard enough to move left
                  snatchLast();
              } else if (velocityX < -SNAP_VELOCITY ) {
                  // Fling hard enough to move right
                  snatchNext();
              } else {
                  SnatchTarget();
              }
			
              if (mVelocityTracker != null) {
                  mVelocityTracker.recycle();
                  mVelocityTracker = null;
              }
              
			break;
		}
		return true;
	}

	
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		// 随手指 快速拨动的代码
		if (D)
			Log.d(TAG, "onFling");
		if (D)
			Log.d(TAG, "VelocityY" + velocityX);

		int targetX = 0;

		if (velocityX < 0) // r 2 l
		{
			targetX = mLastLeftEdge + mScreenWidth;
			if (targetX > mMaxPagePosX) {
				targetX = mMaxPagePosX;
			}
			this.snatchTo(targetX, 0);
		} else // l 2 r
		{
			targetX = mLastLeftEdge - mScreenWidth;
			if (targetX < 0) {
				targetX = 0;
			}
			this.snatchTo(targetX, 0);
		}

		return false;
	}

	/**
	 * This is called in response to an internal scroll in this view (i.e., the
	 * view scrolled its own contents). This is typically as a result of
	 * {@link #scrollBy(int, int)} or {@link #scrollTo(int, int)} having been
	 * called.
	 * 
	 * @param l
	 *            Current horizontal scroll origin.
	 * @param t
	 *            Current vertical scroll origin.
	 * @param oldl
	 *            Previous horizontal scroll origin.
	 * @param oldt
	 *            Previous vertical scroll origin.
	 */
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (D)
			Log.d(TAG, "onScrollChanged");

		int count = getChildCount();

		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child instanceof ViewGroup) {
				if((child.getTag()!=null)&&(child.getTag().equals("fixed")))
				{
				if (child.getVisibility() != GONE) {

					AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) child
							.getLayoutParams();

					int childLeft = this.getPaddingLeft() + lp.x+l;
					int childTop = this.getPaddingTop() + lp.y;
					child.layout(childLeft, childTop,
							childLeft + child.getMeasuredWidth(), childTop
									+ child.getMeasuredHeight());
				}
				}
			}	
		}

	}

	protected void snatchNext()
	{
		if(mCurrentPage<mMaxPageNum)
		{
		   mCurrentPage++;
		}
		snatchCurrent();
	}
	
	protected void snatchLast()
	{
		if(mCurrentPage>1)
		{
		   mCurrentPage--;
		}
		snatchCurrent();		
	}
	
	//scroll back to current page.
	protected void snatchCurrent()
	{
		final int targetX=(mCurrentPage-1)*mScreenWidth;
		
		snatchTo(targetX,0);	
		
		if(mDotPanel!=null)
		{
			int childCount=mDotPanel.getChildCount();
			if(mCurrentPage<=childCount)
			{
				for(int i=0;i<childCount;i++)
				{
					View view=mDotPanel.getChildAt(i);
					view.setEnabled(true);
				}
				
				mDotPanel.getChildAt(mCurrentPage-1).setEnabled(false);		
			}
		}
		
	}
	

	//calculate the position to snatch when finger up.
	protected void SnatchTarget()
	{
		int curX = this.getScrollX();

		if (curX < 0) {
				snatchCurrent();
		} 
		else 
		{	
			int disX=curX-(mScreenWidth*(mCurrentPage-1));
			
			if(disX<-mScreenWidth / 2)
			{
				snatchLast();
			}
			else if(disX>mScreenWidth / 2)
			{
				snatchNext();
			}
			else
			{
				snatchCurrent();
			}
			
		}	
	}
	
	//snatch To the postion.
	protected void snatchTo(int x, int y) {
		if (D)
			Log.d(TAG, "SnatchTo x=" + x + " y=" + y + " startX="
					+ getScrollX());

		int startX = getScrollX();

		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		mScroller.startScroll(startX, getScrollY(), x - startX, getScrollY(),
				Math.abs(x - startX) * 2);
		invalidate();
	}

	@Override
	public void onClick(View v) {
		
		int childCount=mDotPanel.getChildCount();
        int clickedChildIndex=0;
        for(int i=0;i<childCount;i++)
        {
           if(	v.equals(mDotPanel.getChildAt(i)))
           {
        	   clickedChildIndex=i;
        	   break;
           }
        }
        
        if(clickedChildIndex<mCurrentPage)
        {
        	snatchLast();
        }
        else
        {
        	snatchNext();
        }	
	}

}