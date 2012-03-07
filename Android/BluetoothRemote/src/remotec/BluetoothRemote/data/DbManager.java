package remotec.BluetoothRemote.data;

import java.io.File;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbManager {	
	
	private static SQLiteDatabase mDataBase=null;
	
	public static SQLiteDatabase Handle()
	{
		if(mDataBase==null)
		{
			mDataBase=openDatabase();
		}
		
		return mDataBase;
	}
	
	private DbManager(){}
	
	private static SQLiteDatabase openDatabase() { 
        try { 
            // 获得dictionary.db文件的绝对路径 
            File dbf = new File(Config.CR_DBPATH); 
            // 打开/sdcard/dictionary目录中的dictionary.db文件 
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase( 
            		dbf, null); 
            return database; 
        } catch (Exception e) { 
        } 
        return null; 
    } 
	
	public static String getConfig(SQLiteDatabase db,String addr,String stateName,String sDefault)
	{
		String result=sDefault;
		
		// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
		Cursor cursor = null;

		// 查询test_listview数据
		cursor = db.query("tbConfig", null, "devAddr='"+addr+"' and stateName='"+stateName+"'", null, null,
				null, null);
		// 通过强大的cursor把数据库的资料一行一行地读取出来
		if (cursor.moveToNext()) {
			result=cursor.getString(cursor.getColumnIndex("stateValue"));
		}
		
		return result;
	}
}
