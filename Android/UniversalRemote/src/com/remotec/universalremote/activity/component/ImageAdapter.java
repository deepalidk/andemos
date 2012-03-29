/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 *      Author: Walker
 */
package com.remotec.universalremote.activity.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.remotec.universalremote.activity.R;
import com.remotec.universalremote.data.Device;


public class ImageAdapter extends BaseAdapter  {
	
	private Context mContext;
	 
	 public ImageAdapter(Context c)
	 {
	  mContext=c;
	 }
	 @Override
	 public int getCount() {
	  // TODO Auto-generated method stub
	  return mThumbIds.length;
	 }

	 @Override
	 public Object getItem(int position) {
	  // TODO Auto-generated method stub
	  return null;
	 }

	 @Override
	 public long getItemId(int position) {
	  // TODO Auto-generated method stub
	  return 0;
	 }

	 

	 @Override
	 public View getView(int position, View convertView, ViewGroup parent) {
	  // TODO Auto-generated method stub
	  
	  ImageView imageview;
	  if(convertView==null)
	  {
	   imageview=new ImageView(mContext);
	   imageview.setLayoutParams(new GridView.LayoutParams(85, 85));
	   imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
	   imageview.setPadding(8,8,8,8);
	 }
	  else
	  {
	   imageview=(ImageView) convertView;
	  }
	  imageview.setImageResource(mThumbIds[position]);
	  return imageview;
	  }

	 private Integer[] mThumbIds={//显示的图片数组
	  
	  R.drawable.icon_device_doc2_s
	 };



}
