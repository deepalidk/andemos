package com.remotec.universalremote.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.remotec.universalremote.data.RemoteUi;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbManager {

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
	public int getDevTypeByName(String typeName) {
		int devType = 1;
		try {

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			// 查询test_listview数据
			cursor = db.rawQuery(String.format(
					"Select devType from category where name='%1'", typeName),
					null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {

				devType = cursor.getInt(0);
			}
		} catch (Exception e) {
			devType = 1;
		}

		return devType;
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
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			// 查询test_listview数据
			cursor = db.rawQuery("Select name from category", null);
			// 通过强大的cursor把数据库的资料一行一行地读取出来
			while (cursor.moveToNext()) {
				temp = cursor.getString(0);

				devTypes.add(temp);
			}
		} catch (Exception e) {
		}
	}

	public void loadIrBrand() {
		try {
			// If there are code library files, add each one to the ArrayAdapter
			String brandName;
			String devType;
			Map<String, List<String>> map = RemoteUi.getHandle()
					.getIrBrandMap();
			List<String> tempList;

			SQLiteDatabase db = getDataBase();
			// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
			Cursor cursor = null;

			// 查询test_listview数据
			cursor = db.rawQuery("Select distinct(brandName),name devType"
					+ "From MAIN.[irCodeList] A left join category B "
					+ "on A.devType=B.devType", null);
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
		} catch (Exception e) {
		}
	}
}
