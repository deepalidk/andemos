package remotec.BluetoothRemote.data;

public class Config {
    //��ȡSDcard·��
    public static String CR_SDCARD = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    
    //����SDcard������ݵ��ļ���
    public static final String CR_DRECTORY= "/remotec";
    public static final String CR_PATH = CR_SDCARD+CR_DRECTORY;
    public static final String CR_DBNAME = "remote.db";
    public static final String CR_DBPATH = CR_PATH+"/"+CR_DBNAME;//database file
}