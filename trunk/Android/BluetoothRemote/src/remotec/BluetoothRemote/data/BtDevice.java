package remotec.BluetoothRemote.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BtDevice {

	private static final String TRANSMIT_TYPE = "transmitType";
	private static final String DEV_TYPE = "devType";
	private static final String IR_CODE = "irCode";
	private static final String NAME = "Name";
	private static final String BT_ADDRESS = "BtAddress";
	//Bluetooth Device address
	private String mAddr;
	
    public String getAddress()
    {
    	return mAddr;
    }
    
    public void setAddress(String addr)
    {
    	mAddr=addr;
    }
    
	//Bluetooth Device Name
	private String mName;
	
    public String getName()
    {
    	return mName;
    }
    
    public void setName(String name)
    {
    	mName=name;
    }
	
	//IR code set to RT300.
	private int mIRCode;
	
    public int getIRCode()
    {
    	return mIRCode;
    }
    
    public void setIRCode(int irCode)
    {
    	mIRCode=irCode;
    }
     
	//IR code set to RT300.
    //For more infomation about deviceType,
    //please refer to RT300 SPEC.
	private int mDeviceType;
	
    public int getDeviceType()
    {
    	return mDeviceType;
    }
    
    public void setDeviceType(int devType)
    {
    	mDeviceType=devType;
    }
    
	//IR code set to RT300.
    //For more infomation about transmitType,
    //please refer to RT300 SPEC.
	private int mTransmitType;
	
    public int getTransmitType()
    {
    	return mTransmitType;
    }
    
    public void setTransmitType(int transmitType)
    {
    	mTransmitType=transmitType;
    }
    
    
    public boolean loadData(SQLiteDatabase db,String addr)
    {
    	// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
		Cursor cursor = null;

		// 查询test_listview数据
		cursor = db.query("tbBtDevice", null, "BtAddress='"+addr+"'", null, null,
				null, null);
		// 通过强大的cursor把数据库的资料一行一行地读取出来
		if (cursor.moveToNext()) {
			mName=cursor
			.getString(cursor.getColumnIndex(NAME));
			mIRCode=cursor
			.getInt(cursor.getColumnIndex(IR_CODE));
			mDeviceType=cursor
			.getInt(cursor.getColumnIndex(DEV_TYPE));
			mTransmitType=cursor
			.getInt(cursor.getColumnIndex(TRANSMIT_TYPE));
			
			this.mAddr=addr;
			
			return true;

		}
		else
		{
			mName="No Device";
			mIRCode=251;  //philips
			mDeviceType=1; //tv
			mTransmitType=0x81; //	
			mAddr=addr;
			return false;
		}
    }
    
    public void saveData(SQLiteDatabase db)
    {
    	// 定义Cursor游标,用于管理数据，比如获得数据库的每一行数据
		Cursor cursor = null;

		// 查询test_listview数据
		cursor = db.query("tbBtDevice", null, "BtAddress='"+mAddr+"'", null, null,
				null, null);
		// 通过强大的cursor把数据库的资料一行一行地读取出来
		if (cursor.moveToNext()) { //update
			// 查询test_listview数据
			String strFilter =BT_ADDRESS+ "='" + mAddr + "'"; 
			ContentValues args = new ContentValues();
			args.put(NAME, mName); 
			args.put(IR_CODE, mIRCode); 
			args.put(DEV_TYPE, mDeviceType); 
			args.put(TRANSMIT_TYPE, mTransmitType); 
			db.update("tbBtDevice", args, strFilter, null); 
		}
		else  //insert
		{ 
			ContentValues args = new ContentValues();
			args.put(BT_ADDRESS, mAddr); 
			args.put(NAME, mName); 
			args.put(IR_CODE, mIRCode); 
			args.put(DEV_TYPE, mDeviceType); 
			args.put(TRANSMIT_TYPE, mTransmitType); 
			db.insert("tbBtDevice","", args); 
		}
    }
}

