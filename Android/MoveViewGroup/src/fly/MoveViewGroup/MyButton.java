package fly.MoveViewGroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
//import android.view.View.AttachInfo;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewDebug;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Button;

public class MyButton extends TextView {
	private static boolean D=true;
    private static String TAG="MyButton";
    
    private boolean mIsFixPosition=false;
    
    
    private final RectF mRect = new RectF();
    private Paint mPaintBackground;
    private Paint mPaintText;
    private Paint mPaintIcon;

    private boolean mBackgroundSizeChanged;
    private Drawable mBackground;

    
     
    
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
        
        Drawable bg=getBackground();
       
        
        if(D)Log.d(TAG, "MyButton.bg.with="+bg.getIntrinsicWidth()+"myButton.bg.height="+bg.getIntrinsicHeight());
        
        
        
//        Drawable bg=getBackground();
//        
//        this.setFrame(left, top, bg.getIntrinsicWidth()+left, bg.getIntrinsicHeight()+top);
//           
//
//        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mPaintBackground.setColor(Color.WHITE);
//        mPaintBackground.setAlpha(100);
//        
//        mPaintIcon=new Paint();
//        
//        	
//        mPaintText=new Paint(Paint.ANTI_ALIAS_FLAG);
//    	mPaintText.setColor(Color.WHITE);
        
        a.recycle();                 
    }

    @Override
	public boolean onTouchEvent(MotionEvent ev) {
		
        if(D)Log.d(TAG, "onTouchEvent pd.left="+this.getPaddingLeft()+"pd.top="+this.getPaddingTop()+"pd.right="+getPaddingRight()+"pd.bottom="+this.getPaddingBottom());
        
   
//    	this.invalidate(this.getLeft()-100,this.getTop()-100, this.getRight()+100,this.getBottom()+100);
		
		return super.onTouchEvent(ev);
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
    	if(D)Log.d(TAG, "this.l="+ this.getLeft()+" this.t="+this.getTop()+" this.r="+getRight()+" this.b="+getBottom());
//        RectF rc=new RectF();
//        rc.set(0, 0,getRight()-getLeft(),getBottom()-getTop());
//        if(isPressed())
//        {
//        canvas.drawRoundRect(rc, 5, 5, mPaintBackground);
//        }
//        
//        
//        Drawable background = null;
////        if (mBackground != null) {
//////            final int scrollX = getScrollX();
//////            final int scrollY = getScrollY();
////
//////            if (mBackgroundSizeChanged) {
//////                background.setBounds(0, 0,  getRight() - getLeft()-8, getBottom() - getTop()-8);
//////                mBackgroundSizeChanged = false;
//////            }
////        	background=zoomDrawable(mBackground,getRight() - getLeft()-8, getBottom() - getTop()-8);
////            canvas.save();
////            canvas.translate(4, 4);
////        	background.draw(canvas);
////        	canvas.restore();
////        }
//        
//        canvas.drawText(getText().toString(), (getRight() - getLeft())/2, (getBottom() - getTop())/2, mPaintText);
//               
        super.onDraw(canvas); 	  
    }
    
   
    /**
     * Invalidate the whole view. If the view is visible, {@link #onDraw} will
     * be called at some point in the future. This must be called from a
     * UI thread. To call from a non-UI thread, call {@link #postInvalidate()}.
     */
    public void invalidate() {   
    	        final int padding=40;
                super.invalidate(-padding, -padding, getRight() - getLeft()+padding, getBottom() - getTop()+padding);
    }
    
	@Override
	public void computeScroll() {
		
//		super.setFrame(0, 0, 120, 120);
		
//		if(D)Log.d(TAG, "this.x="+this.getLeft()+" this.y="+this.getTop());
//		    this.layout(0+this.getParent(), 0, 100, 100);
//            scrollTo(this.getLeft(), this.getTop()); 
	}
	
    Bitmap drawableToBitmap(Drawable drawable) // drawable 转换成bitmap
    {
              int width = drawable.getIntrinsicWidth();   // 取drawable的长宽
              int height = drawable.getIntrinsicHeight();
              Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;         //取drawable的颜色格式
              Bitmap bitmap = Bitmap.createBitmap(width, height, config);     // 建立对应bitmap
              Canvas canvas = new Canvas(bitmap);         // 建立对应bitmap的画布
              drawable.setBounds(0, 0, width, height);
              drawable.draw(canvas);      // 把drawable内容画到画布中
              return bitmap;
    }

    Drawable zoomDrawable(Drawable drawable, int w, int h)
    {
              int width = drawable.getIntrinsicWidth();
              int height= drawable.getIntrinsicHeight();
              Bitmap oldbmp = drawableToBitmap(drawable); // drawable转换成bitmap
              Matrix matrix = new Matrix();   // 创建操作图片用的Matrix对象
              float scaleWidth = ((float)w / width);   // 计算缩放比例
              float scaleHeight = ((float)h / height);
              matrix.postScale(scaleWidth, scaleHeight);         // 设置缩放比例
              Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       // 建立新的bitmap，其内容是对原bitmap的缩放后的图
              return new BitmapDrawable(newbmp);       // 把bitmap转换成drawable并返回
    }
}
