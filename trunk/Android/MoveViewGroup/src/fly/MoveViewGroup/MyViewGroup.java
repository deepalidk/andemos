
package fly.MoveViewGroup;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class MyViewGroup extends ViewGroup implements OnGestureListener {

	private float mLastMotionX;// 最后点击的点
	private GestureDetector detector;
	int move = 0;// 移动距离
	int MAXMOVE = 850;// 最大允许的移动距离
	private Scroller mScroller;
	int up_excess_move = 0;// 往上多移的距离
	int down_excess_move = 0;// 往下多移的距离
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mTouchSlop;
	private int mTouchState = TOUCH_STATE_REST;
	Context mContext;
	

	public MyViewGroup(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
		setBackgroundResource(R.drawable.background);
		  
		mScroller = new Scroller(context);
		detector = new GestureDetector(this);
 
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		// 获得可以认为是滚动的距离
		mTouchSlop = configuration.getScaledTouchSlop();
 
		// 添加子View
		for (int i = 0; i < 100; i++) { 
			final Button 	MButton = new Button(context);
		   
			MButton.setBackgroundResource(R.drawable.skyblue_button_rectangle_50_50); 
			MButton.getBackground().setAlpha(180);
			MButton.setText("" + (i + 1));
			MButton.setTextColor(Color.BLACK);
			MButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(mContext, MButton.getText(), Toast.LENGTH_SHORT).show(); 
				}
			});
			addView(MButton);
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			// 返回当前滚动X方向的偏移
//			scrollTo(0, mScroller.getCurrY());
			scrollTo(mScroller.getCurrX(),0);
			postInvalidate();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();

		final float x = ev.getX();
		switch (ev.getAction())
		{
		case MotionEvent.ACTION_DOWN:

			mLastMotionX = x;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			final int yDiff = (int) Math.abs(x - mLastMotionX);
			boolean yMoved = yDiff > mTouchSlop;
			// 判断是否是移动
			if (yMoved) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		// final int action = ev.getAction();

		final float x = ev.getX();
		switch (ev.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.forceFinished(true);
				move = mScroller.getFinalX();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			if (ev.getPointerCount() == 1) {
				
				// 随手指 拖动的代码
				int deltaX = 0;
				deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				Log.d("move", "" + move);
				if (deltaX < 0) {
					// 下移
					// 判断上移 是否滑过头
					if (up_excess_move == 0) {
						if (move > 0) {
							int move_this = Math.max(-move, deltaX);
							move = move + move_this;
							scrollBy(move_this,0);
						} else if (move == 0) {// 如果已经是最顶端 继续往下拉
							Log.d("down_excess_move", "" + down_excess_move);
							down_excess_move = down_excess_move - deltaX / 2;// 记录下多往下拉的值
							scrollBy(deltaX / 2,0);
						}
					} else if (up_excess_move > 0)// 之前有上移过头
					{					
						if (up_excess_move >= (-deltaX)) {
							up_excess_move = up_excess_move + deltaX;
							scrollBy(deltaX,0);
						} else {						
							up_excess_move = 0;
							scrollBy(-up_excess_move,0);				
						}
					}
				} else if (deltaX > 0) {
					// 上移
					if (down_excess_move == 0) {
						if (MAXMOVE - move > 0) {
							int move_this = Math.min(MAXMOVE - move, deltaX);
							move = move + move_this;
							scrollBy(move_this,0);
						} else if (MAXMOVE - move == 0) {
							if (up_excess_move <= 100) {
								up_excess_move = up_excess_move + deltaX / 2;
								scrollBy(deltaX / 2,0);
							}
						}
					} else if (down_excess_move > 0) {
						if (down_excess_move >= deltaX) {
							down_excess_move = down_excess_move - deltaX;
							scrollBy(deltaX,0);
						} else {
							down_excess_move = 0;
							scrollBy(down_excess_move,0);
						}
					}
				}		
			} 
			break;
		case MotionEvent.ACTION_UP:			
			// 多滚是负数 记录到move里
			if (up_excess_move > 0) {
				// 多滚了 要弹回去
				scrollBy(-up_excess_move,0);
				invalidate();
				up_excess_move = 0;
			}
			if (down_excess_move > 0) {
				// 多滚了 要弹回去
				scrollBy(down_excess_move,0);
				invalidate();
				down_excess_move = 0;
			}
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return this.detector.onTouchEvent(ev);
	}

	int Fling_move = 0;

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		 //随手指 快速拨动的代码
		Log.d("onFling", "onFling");
		if (up_excess_move == 0 && down_excess_move == 0) {

			int slow = -(int) velocityX * 3 / 4;
			mScroller.fling( move, 0, slow,0, 0, MAXMOVE, 0, 0);
			move = mScroller.getFinalX();
			computeScroll();
		}
		return false;
	}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// // TODO Auto-generated method stub
	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childTop = 10;
		int childLeft = 10;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				child.setVisibility(View.VISIBLE);
				child.measure(r - l, b - t);
				child
						.layout(childLeft, childTop, childLeft + 80,
								childTop + 80);
				if (childLeft < 800) {
					childLeft += 80;
				} else {
					childLeft = 10;
					childTop += 80;
				}
			}
		}
	}

}