package remotec.BluetoothRemote.data;

public class Config {
    //获取SDcard路径
    public static String CR_SDCARD = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    
    //定义SDcard存放数据的文件夹
    public static final String CR_DRECTORY= "/remotec";
    public static final String CR_PATH = CR_SDCARD+CR_DRECTORY;
    public static final String CR_DBNAME = "remote.db";
    public static final String CR_DBPATH = CR_PATH+"/"+CR_DBNAME;//database file
}