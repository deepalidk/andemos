package com.remotec.universalremote.irapi;

import java.util.LinkedList;

import android.text.format.Time;
import android.util.Log;

/**
 * @author walker
 * @date 2011.06.23
 */
public class IrApi implements IOnRead {

	// Debugging
	private static final String TAG = "Irapi";
	private static final boolean D = false;

	/**
	 * IO Control handle
	 */
	private IIo mmIIo;

	/**
	 * the ack packet recevied from RT300
	 */
	private LinkedList<Frame> mmFrames;

	/**
	 * the state of Parser
	 */
	private EParseState mmParseState;

	/**
	 * Frame data of RT300
	 */
	private Frame mmFrame;

	/**
	 * temp of current command id;
	 */
	private byte mmTempCmdId;

	/**
	 * time to Retransimit(ms)
	 */
	private int mmRetransimitTime = 500;

	/**
	 * time to Retransimit(ms)
	 */
	private int mmRetransimitCount = 2;

	public static IrApi getHandle() {
		return mmIrApi;
	}

	private static IrApi mmIrApi = new IrApi();

	private IrApi() {
		mmIIo = null;
		mmParseState = EParseState.cmd;
		mmFrames = new LinkedList<Frame>();
	}

	/**
	 * transmit data with RT300
	 * 
	 * @param TXbuf
	 *            data to send to RT300
	 * @param timeOut
	 *            retransmit timeout
	 * @param retransmitCount
	 *            retransmit count
	 * @return Ack packet
	 * @throws InterruptedException
	 */
	private boolean transmit_data(byte[] TXbuf,int timeOut,int retransmitCount) throws InterruptedException{
		
		if (mmIIo == null)
			return false;

		mmFrames.clear();

		 Time t1=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
		 Time t2=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
		 Time t3;
		 t1.setToNow(); // 取得系统时间。

		do {
			mmIIo.write(TXbuf);

			synchronized (mmFrames) {
				mmFrames.wait(timeOut);
				if (mmFrames.size() > 0) {
					if (mmFrames.getLast().getPayloadBuffer()[0] == EFrameStatus.Succeed
							.getValue()) {
						 t2.setToNow();
						 long t=t2.toMillis(true)-t1.toMillis(true);
						
						 String msg=String.format("%d", t);
						
						 if(D)
						 Log.d("TimeElapsed",msg);
						return true;
					} else {
						return false;
					}

				}
			}
		} while (retransmitCount-- > 0);

		return false;
	}
	
	/**
	 * transmit data with RT300
	 * 
	 * @param TXbuf
	 *            data to send to RT300
	 * @return Ack packet
	 * @throws InterruptedException
	 */
	private boolean transmit_data(byte[] TXbuf) throws InterruptedException {

		return transmit_data(TXbuf,mmRetransimitTime,mmRetransimitCount);
	}

	/**
	 * transmit data with RT300
	 * 
	 * @param TXbuf
	 *            data to send to RT300
	 * @return Ack status
	 * @throws InterruptedException
	 */
	private byte transmit_data_ex(byte[] TXbuf) throws InterruptedException {

		if (mmIIo == null)
			return (byte) EFrameStatus.ErrorGeneral.getValue();

		mmIIo.write(TXbuf);

		synchronized (mmFrames) {
			mmFrames.wait(3000);
			if (mmFrames.size() > 0) {
				return mmFrames.getLast().getPayloadBuffer()[0];
			}
		}

		return (byte) EFrameStatus.ErrorGeneral.getValue();
	}

	/***** IR API *******************/

	/**
	 * @param in iIo
	 *            the interface of IO 
	 *                
	 * @return the firmware version of RT300.
	 *         
	 */
	public String init(IIo iIo) {
		mmIIo = iIo;
		mmIIo.setOnReadFunc(this);
		mmParseState = EParseState.cmd;

		byte[] versionTemp = IrGetVersion();
		String result=null;
	
		if (!D) {
			if (versionTemp == null) {
			
				mmIIo.setOnReadFunc(null);
				mmIIo = null;
			}else{
				result=String.format("%02x%02x",versionTemp[1],versionTemp[2]);	
			}
		}

		return result;
	}

	/**
	 * get RT300 version info
	 * 
	 * @param version
	 * @return true-success false-failed
	 */
	public byte[] IrGetVersion() {

		if (D)
			Log.d(TAG, "IrGetVersion");
		Frame frame = new Frame(0);
		frame.setCmdID((byte) 0x09);
		byte[] version = null;

		try {
		     boolean result = transmit_data(frame.getPacketBuffer());

			if (result) {
				if (D)
					Log.d(TAG, "IRGetVersion, mmFrames.removeFirst()");
				Frame rsultframe = mmFrames.removeFirst();

				if (D)
					Log.d(TAG, "rsultframe.getPayloadBuffer();");
				version = rsultframe.getPayloadBuffer();
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "IRGetVersion, exception");
			e.printStackTrace();
		}

		return version;
	}

	/*******************************
	 * stop IR transmission
	 *******************************/
	public void IrTransmitStop() {
		byte buffer[] = new byte[1];
		buffer[0] = 0x00;
		mmIIo.write(buffer);
	}

	/**
	 * TRANSMIT PREPROGRAMMED IR CODE
	 * 
	 * @param type
	 *            IR transmission type
	 * @param devId
	 *            device ID
	 * @param codeNum
	 *            code Number or Code location Number
	 * @param keyId
	 *            Key ID
	 * @return
	 */
	public boolean transmitPreprogramedCode(byte type, byte devId, int codeNum,
			byte keyId) {
		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		Frame frame = new Frame(5);
		frame.setCmdID((byte) 0x01);
		boolean result = false;

		try {
			frame.addPayload(type);
			frame.addPayload(devId);
			frame.addPayload((byte) (codeNum >> 8));
			frame.addPayload((byte) (codeNum & 0xFF));
			frame.addPayload(keyId);

			result = transmit_data(frame.getPacketBuffer());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * TRANSMIT PREPROGRAMMED IR CODE
	 * 
	 * @param type
	 *            IR transmission type
	 * @param devId
	 *            device ID
	 * @param codeNum
	 *            code Number or Code location Number
	 * @param keyId
	 *            Key ID
	 * @return
	 */
	public boolean transmitIrData(byte type, byte[] data) {
		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		
		if(data==null){
			return false;
		}
		
		Frame frame = new Frame(81);
		frame.setCmdID((byte) 0x20);
		boolean result = false;

		try {
            frame.addPayload(type);
			frame.addPayload(data);

			Log.d("DeviceKeyActivity", ""+"changed");
			result = transmit_data(frame.getPacketBuffer());
			Log.d("DeviceKeyActivity", "result"+result);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * GET KEY FLAG
	 * 
	 * @param devId
	 *            device ID
	 * @param codeNum
	 *            code Number or Code location Number
	 * @return 1 byte status 8 bytes flag.
	 */
	public byte[] getKeyFlag(byte devId, int codeNum) {
		if (D)
			Log.d(TAG, "getKeyFlag");
		Frame frame = new Frame(3);
		frame.setCmdID((byte) 0x03);
		byte flags[] = null;

		try {
			boolean result = false;
			frame.addPayload(devId);
			frame.addPayload((byte) (codeNum >> 8));
			frame.addPayload((byte) (codeNum & 0xFF));

			result = transmit_data(frame.getPacketBuffer());

			if (result) {
				flags = mmFrames.getLast().getPayloadBuffer();
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "getKeyFlag, exception");
			e.printStackTrace();
		}

		return flags;
	}

	/**
	 * STORE SUPPLEMENTRARY LIBRARY TO E2PROM
	 * 
	 * @param location
	 *            the location of library.
	 * 
	 * @param data
	 *            the data to be write.
	 * 
	 * @return
	 */
	public boolean StoreLibrary2E2prom(byte location, byte[] data) {
		if (D)
			Log.d(TAG, "StoreLibrary2E2prom");

		boolean result = false;

		if (data.length != 592) {
			return result;
		}

		try {
			// transform the first 4 packages.
			int curStart = 0;
			int curLength = 121;
			byte status = 0;

			Frame frame;
			int i = 0;
			for (i = 0; i < 4; i++) {
				frame = new Frame(123);
				frame.setCmdID((byte) 0x07);
				frame.addPayload(location);
				frame.addPayload((byte) i);
				frame.addPayload(data, curStart, curLength);

				status = transmit_data_ex(frame.getPacketBuffer());

				if (status != 0x31) {
					break;
				}

				if (D)
					Log.d(TAG, " " + status);

				curStart += 121;
			}

			if (status != 0x31) {
				return result;
			}

			frame = new Frame(110);
			frame.setCmdID((byte) 0x07);
			frame.addPayload(location);
			frame.addPayload((byte) 4);
			frame.addPayload(data, 484, 108);

			if (D)
				Log.d(TAG, " " + status);

			status = transmit_data_ex(frame.getPacketBuffer());

			result = (status == 0x30);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * TRANSMIT PREPROGRAMMED IR CODE
	 * 
	 * @param loc
	 *        IR Code Storage Location(0-100)
	 * @return
	 */
	public boolean learnIrCode(byte loc) {
		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		Frame frame = new Frame(1);
		frame.setCmdID((byte) 0x04);
		boolean result = false;

		try {
			frame.addPayload(loc);

			result = transmit_data(frame.getPacketBuffer());
			
			if(result)
			{
				result=false;
				synchronized (mmFrames) {
					mmFrames.wait(20000);
					if (mmFrames.size() > 1) {
						if (mmFrames.getLast().getPayloadBuffer()[0] == EFrameStatus.Succeed
								.getValue()) {
							result= true;
						}
					}
				}
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * TRANSMIT PREPROGRAMMED IR CODE
	 * 
	 * @param loc
	 *        IR Code Storage Location(0-100)
	 * @return
	 *     81 byte data.
	 */
	public byte[] readLearnData(byte loc) {
		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		Frame frame = new Frame(1);
		frame.setCmdID((byte) 0x12);
		boolean result = false;
		byte[] resultFrame=null;

		try {
			frame.addPayload(loc);

			result = transmit_data(frame.getPacketBuffer());
			
			
			if(result){
			Frame resultframe = mmFrames.removeFirst();

			if (D)
				Log.d(TAG, "rsultframe.getPayloadBuffer();");
			resultFrame = resultframe.getPayloadBuffer();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return resultFrame;
	}
	
	/**
	 * TRANSMIT LEARNED IR CODE
	 * 
     * @param type
	 *            IR transmission type
	 * @param loc
	 *            Learned IR Code Storage Location     
	 * @return
	 *     
	 */
	public boolean transmitLearnData(byte type,byte loc) {
		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		Frame frame = new Frame(2);
		frame.setCmdID((byte) 0x02);
		boolean result = false;

		try {
			frame.addPayload(type);
			frame.addPayload(loc);

			result = transmit_data(frame.getPacketBuffer());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * TRANSMIT PREPROGRAMMED IR CODE
	 * 
	 * @param type
	 *        IR Code Storage Location(0-100)
	 * @param data
	 *        IR learn data.       
	 * @return
	 *     
	 */
	public boolean storeLearnData(byte loc,byte[] data) {
		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		
		if(data==null) return false;
		
		Frame frame = new Frame(82);
		frame.setCmdID((byte) 0x13);
		boolean result = false;

		try {
			frame.addPayload(loc);
			frame.addPayload(data);

			result = transmit_data(frame.getPacketBuffer());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return result;
	}
	
	
	/***** end of ir api *******************/

	/**
	 * parse of RT300 protocol ack packet
	 * 
	 * @param buffer
	 *            data to be Parse
	 */
	private void Parse(byte buffer) {

		if (D)
			Log.d(TAG,
					"state=" + mmParseState
							+ String.format("Buffer=%H ", buffer));

		if (mmParseState == EParseState.cmd) {
			mmTempCmdId = buffer;
			mmParseState = EParseState.length;
		} else if (mmParseState == EParseState.length) {
			mmFrame = new Frame(buffer - 2);
			mmFrame.setCmdID(mmTempCmdId);

			if (buffer != 2) {
				mmParseState = EParseState.data;
			} else {
				mmParseState = EParseState.checkSum;
			}
		} else if (mmParseState == EParseState.data) {

			mmFrame.addPayload(buffer);
			if (mmFrame.isPayloadFull()) {
				mmParseState = EParseState.checkSum;
			}
		} else if (mmParseState == EParseState.checkSum) {

			if (buffer == mmFrame.calcAckChecksum()) {
				// if (mmFrame.getPayloadBuffer()[0] == EFrameStatus.Succeed
				// .getValue()) {
				synchronized (mmFrames) {
					mmFrames.add(mmFrame);
					mmFrames.notify();
				}
				// }
			}
			mmParseState = EParseState.cmd;

		}
	}

	@Override
	public void OnRead(byte[] buffer, int len) {
		// TODO Auto-generated method stub
		for (int i = 0; i < len; i++) {
			Parse(buffer[i]);
		}
	}

	/**
	 * Frame of the RT300 protocol
	 * 
	 * @author walker
	 * 
	 */
	class Frame {
		/**
		 * data buffer
		 */
		private byte[] mmPayloadBuffer;
		/**
		 * the idx of current data buffer
		 */
		private int mmPayloadIdx;

		/**
		 * max length of data buffer
		 */
		private int MaxBufLen = 150;

		/*
		 * command id of frame
		 */
		private byte mmCmdId;

		/**
		 * set the frame cmd id;
		 * 
		 * @param cmdId
		 */
		public void setCmdID(byte cmdId) {
			mmCmdId = cmdId;
		}

		/**
		 * set the frame cmd id;
		 * 
		 * @param cmdId
		 */
		public byte getCmdID() {
			return mmCmdId;
		}

		public Frame(int len) {
			mmPayloadBuffer = new byte[len];
			mmPayloadIdx = 0;
		}

		/**
		 * get the frame data
		 * 
		 * @return
		 */
		public byte[] getPayloadBuffer() {
			return mmPayloadBuffer;
		}

		/**
		 * get frame is complete.
		 */
		public boolean isPayloadFull() {
			return mmPayloadIdx == mmPayloadBuffer.length;
		}

		/**
		 * add data to frame
		 * 
		 * @param buffer
		 *            the data to be added.
		 */
		public void addPayload(byte buffer) {
			mmPayloadBuffer[mmPayloadIdx++] = buffer;
		}

		/**
		 * add data to frame
		 * 
		 * @param buffer
		 *            the data to be added.
		 */
		public void addPayload(byte[] buffer) {
			System.arraycopy(buffer, 0, mmPayloadBuffer, mmPayloadIdx,
					buffer.length);
			mmPayloadIdx += buffer.length;
		}

		/**
		 * add data to frame
		 * 
		 * @param buffer
		 *            the data to be added.
		 */
		public void addPayload(byte[] buffer, int start, int length) {
			System.arraycopy(buffer, start, mmPayloadBuffer, mmPayloadIdx,
					length);
			mmPayloadIdx += length;
		}

		public void clearPayload() {
			mmPayloadIdx = 0;
		}

		/**
		 * calculate ack packet checksum
		 * 
		 * @param pData
		 *            ： the data for calculate checksum
		 * @return: the length of pData
		 */
		public byte calcAckChecksum() {
			byte result = 0;

			result += mmCmdId;
			result += mmPayloadBuffer.length + 2;

			for (int i = 0; i < mmPayloadBuffer.length; i++) {
				result += mmPayloadBuffer[i];
			}

			return result;
		}

		/**
		 * get ack packet buffer
		 * 
		 * @return
		 */
		public byte[] getAckPacketBuffer() {
			byte[] result = new byte[mmPayloadBuffer.length + 3];

			result[0] = mmCmdId;
			result[1] = (byte) (mmPayloadBuffer.length + 2);
			System.arraycopy(mmPayloadBuffer, 0, result, 2,
					mmPayloadBuffer.length);
			result[result.length - 1] = calcAckChecksum();

			return result;
		}

		/**
		 * calculate ack packet checksum
		 * 
		 * @param pData
		 *            ： the data for calculate checksum
		 * @return: the length of pData
		 */
		public byte calcChecksum() {
			byte result = 0;

//			result += 0x45;
//			result += 0x5a;
			
			result += 0x45;
			result += 0x34;
			
			result += mmCmdId;
			result += mmPayloadBuffer.length + 4;

			for (int i = 0; i < mmPayloadBuffer.length; i++) {
				result += mmPayloadBuffer[i];
			}

			return result;
		}

		/**
		 * get ack packet buffer
		 * 
		 * @return
		 */
		public byte[] getPacketBuffer() {
			byte[] result = new byte[mmPayloadBuffer.length + 5];
//			result[0] = 0x45;
//			result[1] = 0x5a;
			
			result[0] = 0x45;
			result[1] = 0x34;
			
			result[2] = mmCmdId;
			result[3] = (byte) (mmPayloadBuffer.length + 4);
			System.arraycopy(mmPayloadBuffer, 0, result, 4,
					mmPayloadBuffer.length);
			result[result.length - 1] = calcChecksum();

			return result;
		}

	};

	/**
	 * Parse State
	 * 
	 * @author walker
	 * 
	 */
	enum EParseState {
		cmd, // cmd Id
		length, // data length
		data, // data
		checkSum, // checksum
	};

	/**
	 * the status of current frame.
	 * 
	 * @author walker
	 * 
	 */
	enum EFrameStatus {
		Succeed(0x30), ErrorGeneral(0x40);
		private int value;

		EFrameStatus(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	};
}
