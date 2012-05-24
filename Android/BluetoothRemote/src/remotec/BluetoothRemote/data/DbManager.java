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
	
	public static String getConfig(SQLiteDatabase db,String addr,String stateName,String sDefault)
	{
		String result=sDefault;
		
		// ����Cursor�α�,���ڹ������ݣ����������ݿ��ÿһ������
		Cursor cursor = null;

		// ��ѯtest_listview����
		cursor = db.query("tbConfig", null, "devAddr='"+addr+"' and stateName='"+stateName+"'", null, null,
				null, null);
		// ͨ��ǿ���cursor�����ݿ������һ��һ�еض�ȡ����
		if (cursor.moveToNext()) {
			result=cursor.getString(cursor.getColumnIndex("stateValue"));
		}
		
		return result;
	}
}