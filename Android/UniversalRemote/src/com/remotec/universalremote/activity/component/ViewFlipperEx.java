package com.remotec.universalremote.activity.component;

import com.remotec.universalremote.activity.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

public class ViewFlipperEx extends ViewFlipper{
	private static boolean D = false;
	private static String TAG = "ViewFlipperEx";
	
	 /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */ 
    private static final int SNAP_VELOCITY = 1000;
	private int mLastLeftEdge;// the screen left edge when finger down.
	private float mLastMotionPosX; // record last posX since the finger move.
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mTouchSlop;
    private Context mContext;
	private VelocityTracker mVelocityTracker;
	private float mMaximumVelocity;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	 
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
	
	public void setGestureDetector(GestureDetector gDetector){
	}
	
	public ViewFlipperEx(Context context) {
		super(context);

		initView(context);
	}

	public ViewFlipperEx(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context);
	}

	private void initView(Context context) {

		mContext = context;
		
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		// 获得可以认为是滚动的距离
		mTouchSlop = configuration.getScaledTouchSlop();
		
		slideLeftIn = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils
				.loadAnimation(mContext, R.anim.slide_left_out);
		slideRightIn = AnimationUtils
				.loadAnimation(mContext, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_right_out);

	}
   
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final float x = ev.getX();
		int touchState = TOUCH_STATE_REST;

		if (D)
			Log.d(TAG, "onInterceptTouchEvent ev.getAction=" + ev.getAction());

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
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
//			final int yDiff2 = (int) Math.abs(mLastMotionPosX - x);
//
//			boolean yMoved2 = yDiff2 > mTouchSlop;
//			// 判断是否是移动
//			if (yMoved2) {
//				touchState = TOUCH_STATE_SCROLLING;
//			}
//			break;
		}
		return touchState != TOUCH_STATE_REST;
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		
//		if (D)
//			Log.d(TAG, "onTouchEvent ev.getAction=" + event.getAction()+" ev.getX="+event.getX()+" ev.getRawX="+event.getRawX());
//		event.setAction(0);
//		if (mGestureDetector.onTouchEvent(event))
//			return true;
//		else
//			return false;
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (D)
		Log.d(TAG, "onTouchEvent ev.getAction=" + ev.getAction()+" ev.getX="+ev.getX()+" ev.getRawX="+ev.getRawX());

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
		
		final float x = ev.getX();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastLeftEdge = this.getScrollX();
			mLastMotionPosX = x;

			break;
		case MotionEvent.ACTION_MOVE: {
//			// 随手指 拖动的代码
//			int deltaX = 0;
//			deltaX = (int) (x - mLastMotionPosX);
//
//			if (D)
//				Log.d(TAG, "deltaX=" + deltaX);
//			scrollBy(-deltaX, 0);
//
//			if ((getScrollX() < -mScreenWidth / 3)) {
//				scrollBy(deltaX, 0);
//			}
//
//			if ((getScrollX() > (mMaxPagePosX + mScreenWidth / 3))) {
//				scrollBy(deltaX, 0);
//			}
//
//			mLastMotionPosX = x;
		}
			break;
		case MotionEvent.ACTION_UP:
			// Move to the threshold of page
             
			  final VelocityTracker velocityTracker = mVelocityTracker;
              velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
              int velocityX = (int) velocityTracker.getXVelocity();

              if (velocityX > SNAP_VELOCITY) {
                  // Fling hard enough to move left
//                  snatchLast();
					setInAnimation(slideRightIn);
					setOutAnimation(slideRightOut);
					showPrevious();
              } else if (velocityX < -SNAP_VELOCITY ) {
                  // Fling hard enough to move right
//                  snatchNext();
            	  
            	    setInAnimation(slideLeftIn);
					setOutAnimation(slideLeftOut);
					showNext();
              } else {
//                  SnatchTarget();
              }
			
              if (mVelocityTracker != null) {
                  mVelocityTracker.recycle();
                  mVelocityTracker = null;
              }
              
			break;
		}
		return true;
	}
	
	
}