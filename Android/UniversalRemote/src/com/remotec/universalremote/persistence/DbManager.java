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
import com.remotec.universalremote.data.RemoteUi.BrandListType;
import com.remotec.universalremote.data.Uird;

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
			// 获得dictionary.db文件的绝对路径
			File dbf = new File(dbPath);
			// 打开/sdcard/dictionary目录中的dictionary.db文件
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
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			// 查询test_listview数据
			cursor = db.rawQuery(
					String.format(
							"Select devType from category where name = '%s'",
							typeName), null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {

				devType = cursor.getInt(0);
			}

			cursor.close();
		} catch (Exception e) {
			devType = 1;
		}

		return devType;
	}

	/*
	 * get codes of the specific category and brandName.
	 */
	public List<String> getCodesList(String category, String brandName,
			boolean isUirdLib) {
		List<String> result = new ArrayList<String>();

		try {

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;
			int irSrc = isUirdLib ? 2 : 1;

			String sql = String
					.format("Select distinct(ircodenum) From irCodeList"
							+ " left join category on irCodeList.devType=category.devType"
							+ "  where irCodeSrc=%d and name like \'%s\' and brandName like \'%s\'",
							irSrc, category, brandName);
			// 查询test_listview数据
			cursor = db.rawQuery(sql, null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {

				result.add(cursor.getString(0));

			}

			cursor.close();
		} catch (Exception e) {
			result = null;
		}

		return result;
	}

	public List<Uird> getUirdData(int devType, int irCode) {
		List<Uird> mDataList = new ArrayList<Uird>();

		try {

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			String sql = String
					.format("Select keyId,data From tbUirdData where codenum=%d and devtype=%d",
							irCode, devType);
			// 查询test_listview数据
			cursor = db.rawQuery(sql, null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {
				Uird temp = new Uird();
				temp.setKeyId(cursor.getInt(0));
				byte[] uirdData = XmlManager.hexStringToByteArray(cursor
						.getString(1));
				temp.setUirdData(uirdData);
				mDataList.add(temp);

			}

			cursor.close();
		} catch (Exception e) {
		}

		return mDataList;
	}

	public Uird getUirdData(int devType, int irCode, int keyId) {
		Uird result = null;

		try {

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			String sql = String
					.format("Select data From tbUirdData where codenum=%d and devtype=%d and keyId=%d",
							irCode, devType, keyId);
			// 查询test_listview数据
			cursor = db.rawQuery(sql, null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {
				result = new Uird();
				result.setKeyId(keyId);
				byte[] uirdData = XmlManager.hexStringToByteArray(cursor
						.getString(0));
				result.setUirdData(uirdData);

			}

			cursor.close();
		} catch (Exception e) {
		}

		return result;
	}

	public Uird getUirdData(int irCode, int keyId, String powerStatus,
			String modeStatus, String tempStatus, String swingStatus,
			String fanStatus) {

		Uird result = null;

		try {

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			String sql = String
					.format("Select data From tbAcUirdData where codenum=%d and keyId=%d and powerStatus like '%s' and modeStatus like '%s' and tempStatus like '%s' and swingStatus like '%s' and fanStatus like '%s'",
							irCode, keyId, powerStatus, modeStatus, tempStatus,
							swingStatus, fanStatus);
			// 查询test_listview数据
			cursor = db.rawQuery(sql, null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {
				result = new Uird();
				result.setKeyId(keyId);
				byte[] uirdData = XmlManager.hexStringToByteArray(cursor
						.getString(0));
				result.setUirdData(uirdData);

			}

			cursor.close();
		} catch (Exception e) {
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

			if (devTypes == null) {
				return;
			} else {
				devTypes.clear();
			}

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			// 查询test_listview数据
			cursor = db.rawQuery("Select name from category", null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {
				temp = cursor.getString(0);

				devTypes.add(temp);
			}

			cursor.close();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/*
	 * loads all the ir code information to RemoteUi object. This will cost more
	 * memory and achieve a better performance when adding device.
	 */
	public void loadIrBrand(BrandListType type) {
		try {
			// If there are code library files, add each one to the ArrayAdapter
			String brandName;
			String devType;
			Map<String, List<String>> map = RemoteUi.getHandle()
					.getIrBrandMap();
			List<String> tempList;

			if (map == null) {
				return;
			} else {
				for (List<String> l : map.values()) {
					l.clear();
				}

				map.clear();
			}

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			String sql = String.format(
					"Select distinct(brandName),name devType"
							+ " From irCodeList A left join category B "
							+ "on A.devType=B.devType where irCodeSrc=%d",
					(type == BrandListType.UIRD) ? 2 : 1);

			// 查询test_listview数据
			cursor = db.rawQuery(sql, null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
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

			cursor.close();
			RemoteUi.getHandle().setBrandListType(type);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/*
	 * Gets the data base version.
	 */
	public static String getConfig(SQLiteDatabase db, String addr,
			String stateName, String sDefault) {
		String result = sDefault;
		// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
		Cursor cursor = null;
		try {

			// 查询test_listview数据
			cursor = db.query("tbConfig", null, "devAddr='" + addr
					+ "' and stateName='" + stateName + "'", null, null, null,
					null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			if (cursor.moveToNext()) {
				result = cursor.getString(cursor.getColumnIndex("stateValue"));
			}

			cursor.close();
		} catch (Exception ex) {
			if (cursor != null) {
				cursor.close();
			}
		}

		return result;
	}
}
