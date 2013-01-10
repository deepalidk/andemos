/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remotec.zremote.activity.component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.remotec.zremote.activity.R;
import com.remotec.zremote.data.Device;

public class ImageAdapter extends BaseAdapter {


	private Context mContext;

	public ImageAdapter(Context c) {
		mContext = c;
	}

	@Override
	public int getCount() {
		return mThumbIds.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageview;
		if (convertView == null) {
			imageview = new ImageView(mContext);
			imageview.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageview.setPadding(8, 8, 8, 8);
		} else {
			imageview = (ImageView) convertView;
		}
		imageview.setImageResource(mThumbIds.get(position));
		imageview.setTag(mThumbIds.get(position));
		return imageview;
	}

	// ��ȡ����������ͼƬ��ID��Լ������ͼƬID��p_��ͷ��
	public static List<Integer> getImageValues()
	{
		try
		{
			//�õ�R.drawable���е�����, ����ȡdrawableĿ¼�µ�����ͼƬ
			Field[] drawableFields = R.drawable.class.getFields();

			List<Integer> resourceValues = new ArrayList();

			for (Field field : drawableFields){
				// �����Field��������p_��ͷ
				if ((field.getName().indexOf("icon_") != -1)&&(field.getName().endsWith("_s")==true)){
					resourceValues.add(field.getInt(R.drawable.class));
				}
			}

			return resourceValues;

		}catch (Exception e){
			return null;
		}

	}

	private  static List<Integer> mThumbIds = getImageValues();

}