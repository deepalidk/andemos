package remotec.BluetoothRemote.data;

import java.io.File;


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
}
