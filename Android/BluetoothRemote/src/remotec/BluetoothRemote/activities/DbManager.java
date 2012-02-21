package remotec.BluetoothRemote.activities;

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
            // ���dictionary.db�ļ��ľ���·�� 
            File dbf = new File(Config.CR_DBPATH); 
            // ��/sdcard/dictionaryĿ¼�е�dictionary.db�ļ� 
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase( 
            		dbf, null); 
            return database; 
        } catch (Exception e) { 
        } 
        return null; 
    } 
}