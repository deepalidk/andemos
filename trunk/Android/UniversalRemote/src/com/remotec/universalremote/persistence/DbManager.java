/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.remotec.universalremote.data.RemoteUi;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbManager {

	// Debugging Tags 
	private static final String TAG = "DbManager";
	private static final boolean D = false;
	
	private static SQLiteDatabase mDataBase = null;

	public static SQLiteDatabase getDataBase() {
		if (mDataBase == null) {
			mDataBase = openDatabase(RemoteUi.INTERNAL_DATA_DIRECTORY + "/"
					+ RemoteUi.UI_DB_FILE);
		}

		return mDataBase;
	}

	public DbManager() {
	}

	private static SQLiteDatabase openDatabase(String dbPath) {
		try {
			// ���dictionary.db�ļ��ľ���·��
			File dbf = new File(dbPath);
			// ��/sdcard/dictionaryĿ¼�е�dictionary.db�ļ�
			SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(dbf,
					null);
			return database;
		} catch (Exception e) {
		}
		return null;
	}

	/*
	 * Gets the device type by the name of device.
	 */
	public int getDevTypeIdByName(String typeName) {
		int devType = 1;
		try {

			SQLiteDatabase db = getDataBase();
			// ����Cursor�α�,���ڹ������ݣ����������ݿ��ÿһ������
			Cursor cursor = null;

			// ��ѯtest_listview����
			cursor = db.rawQuery(String.format(
					"Select devType from category where name='%1'", typeName),
					null);
			// ͨ��ǿ���cursor�����ݿ������һ��һ�еض�ȡ����
			while (cursor.moveToNext()) {

				devType = cursor.getInt(0);
			}
		} catch (Exception e) {
			devType = 1;
		}

		return devType;
	}
	
	/*
	 * get codes of the specific category and brandName.
	 */
	public List<String> getCodesList(String category,String brandName)
	{
		List<String> result=new ArrayList<String>();
		
		try {

			SQLiteDatabase db = getDataBase();
			// ����Cursor�α�,���ڹ������ݣ����������ݿ��ÿһ������
			Cursor cursor = null;

			String sql=String.format("Select ircodenum From irCodeList" +
					" left join category on irCodeList.devType=category.devType" +
					"  where name like \'%s\' and brandName like \'%s\'", category,brandName);
			// ��ѯtest_listview����
			cursor = db.rawQuery(sql,null);
			// ͨ��ǿ���cursor�����ݿ������һ��һ�еض�ȡ����
			while (cursor.moveToNext()) {

				result.add(cursor.getString(0));
				
			}
		} catch (Exception e) {
			result = null;
		}

		return result;
	}

	/*
	 * loads the device category from database.
	 */
	public void loadDevCategory() {
		try {
			// If there are code library files, add each one to the ArrayAdapter
			String temp;
			List<String> devTypes = RemoteUi.getHandle().getCategoryList();

			SQLiteDatabase db = getDataBase();
			// ����Cursor�α�,���ڹ������ݣ����������ݿ��ÿһ������
			Cursor cursor = null;

			// ��ѯtest_listview����
			cursor = db.rawQuery("Select name from category", null);
			// ͨ��ǿ���cursor�����ݿ������һ��һ�еض�ȡ����
			while (cursor.moveToNext()) {
				temp = cursor.getString(0);

				devTypes.add(temp);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/*
	 * loads all the ir code information to RemoteUi object.
	 * This will cost more memory and achieve a better performance when adding device.
	 */
	public void loadIrBrand() {
		try {
			// If there are code library files, add each one to the ArrayAdapter
			String brandName;
			String devType;
			Map<String, List<String>> map = RemoteUi.getHandle()
					.getIrBrandMap();
			List<String> tempList;

			SQLiteDatabase db = getDataBase();
			// ����Cursor�α�,���ڹ������ݣ����������ݿ��ÿһ������
			Cursor cursor = null;

			// ��ѯtest_listview����
			cursor = db.rawQuery("Select distinct(brandName),name devType"
					+ " From irCodeList A left join category B "
					+ "on A.devType=B.devType", null);
			// ͨ��ǿ���cursor�����ݿ������һ��һ�еض�ȡ����
			while (cursor.moveToNext()) {
				brandName = cursor.getString(0);
				devType = cursor.getString(1);

				if (map.containsKey(devType)) {
					tempList = map.get(devType);
				} else {
					tempList = new ArrayList<String>();
					map.put(devType, tempList);
				}

				tempList.add(brandName);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	
}