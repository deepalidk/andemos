/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remote.universalremote.activity.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.remote.universalremote.data.Device;
import com.remote.universalremote.data.Key;
import com.remote.universalremote.activity.R;

/*
 * Performs as a remote key.
 */
@SuppressLint("ResourceAsColor")
public class KeyButton extends Button {

	/*
	 * the key id , when send ir command.
	 */
	private int mKeyId;

	public int getKeyId() {
		
		return mKeyId;
	}

	/*
	 * we can't change the text label of an Icon button.
	 */
	private boolean mIsIconBtn;
	/*
	 * use to draw disable buttons.
	 */
	static private float mPaddingV;
	static private float mRadius;
	static private float mPaddingH;
	static private Paint mPaint=null;

	public boolean getIsIconButton() {
		return mIsIconBtn;
	}

	public KeyButton(Context context) {
		super(context, null);
	}

	public KeyButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
		
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.KeyButton);
		mKeyId = a.getInteger(R.styleable.KeyButton_key_id, -1);
		mIsIconBtn = a.getBoolean(R.styleable.KeyButton_is_icon_btn, false);

		a.recycle();
	}

	public KeyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@SuppressLint("ResourceAsColor")
	private void init() {

		if(mPaint==null)
		{	
			mPaddingH = getResources().getDimension(R.dimen.disable_key_padding_h);
			mPaddingV = getResources().getDimension(R.dimen.disable_key_padding_v);
			mRadius = getResources().getDimension(R.dimen.disable_key_mask_radius);
			
			Integer malph = getResources().getInteger(R.integer.disable_key_alph);
	
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.WHITE);
			mPaint.setAlpha(malph);
		}
	}

	/**
	 * Implement this to do your drawing.
	 * 
	 * @param canvas
	 *            the canvas on which the background will be drawn
	 */
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		
		Key key = (Key) getTag();

		if (key != null) {
			
			if ((!key.getVisible()) && (this.getVisibility() == View.VISIBLE)) {
				Rect r = getBackground().getBounds();

				RectF rf = new RectF(r.left + mPaddingH, r.top + mPaddingV,
						r.right - mPaddingH, r.bottom - mPaddingV);

				canvas.drawRoundRect(rf, mRadius, mRadius, mPaint);
			}
		}
	}

}
