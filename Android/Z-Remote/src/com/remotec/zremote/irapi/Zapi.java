package com.remotec.zremote.irapi;

import java.util.LinkedList;

import android.text.format.Time;
import android.util.Log;

/**
 * @author walker
 * @date 2011.06.23
 */
public class Zapi implements IOnRead {

	// Debugging
	private static final String TAG = "Zapi";
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
	private int mmRetransimitTime = 200;
	
	/*
	 * time to execute a cmd(ms).
	 */
	private int mmCmdTimeOut=20000;

	/**
	 * time to Retransimit(ms)
	 */
	private int mmRetransimitCount = 0;

	public static Zapi getHandle() {
		return mmZapi;
	}

	private static Zapi mmZapi = new Zapi();

	private Zapi() {
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
					if (mmFrames.getLast().getFrameType() == 0x06) {
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

		byte[] versionTemp = GetVersion();
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
	public byte[] GetVersion() {

		if (D)
			Log.d(TAG, "IrGetVersion");
		Frame frame = new Frame(0);
		frame.setFrameType((byte) 0x09);
		byte[] version = null;

		version=new byte[3];
		version[1]=0;
		version[2]=1;
		return version;
/*		try {
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

		return version;*/
	}

	/*******************************
	 * Add Device.
	 * 
	 * @return node id is added. -1: null node is add.
	 *******************************/
	public int AddDevice() {

		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		Frame frame = new Frame(2);
		frame.setFrameType((byte) 0x80);
		int result = -1;

		try {
			frame.addPayload((byte) 0x80); //cmd frame
			frame.addPayload((byte) 0x00); //del cmd

		   boolean 	trasmitR = transmit_data(frame.getPacketBuffer());
			
			if(trasmitR)
			{
				synchronized (mmFrames) {
					mmFrames.wait(mmCmdTimeOut);
					if (mmFrames.size() > 1) {
						if (mmFrames.getLast().getPayloadBuffer()[1] == 0x00) {
							if(mmFrames.getLast().getPayloadBuffer()[2]==0x01){
							 result= mmFrames.getLast().getPayloadBuffer()[3];
							}
								
						}
					}else{
						CancelCommand();
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
	
	/*******************************
	 * Cotrol Device.
	 * 
	 * @return true-success. false-fail.
	 *******************************/
	public boolean ControlDevice(byte nodeId, byte endpoint, byte value){

		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		Frame frame = new Frame(7);
		frame.setFrameType((byte) 0x80);
		boolean result = false;

		try {
			frame.addPayload((byte) 0x80); //cmd frame
			frame.addPayload((byte) 0x03); //basic cmd
			frame.addPayload(nodeId);
			frame.addPayload(endpoint);
			frame.addPayload((byte)0x20);
			frame.addPayload((byte)0x01);
			frame.addPayload(value);

			result = transmit_data(frame.getPacketBuffer());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			if (D)
				Log.d(TAG, "transmitPreprogramedCode, exception");
			e.printStackTrace();
		}

		return result;
	
	}
	
	/*******************************
	 * Delete Device.
	 * 
	 * @return node id been delete. -1: null node is removed.
	 *******************************/
	public int DelDevice() {

		if (D)
			Log.d(TAG, "transmitPreprogramedCode");
		Frame frame = new Frame(2);
		frame.setFrameType((byte) 0x80);
		int result = -1;

		try {
			frame.addPayload((byte) 0x80); //cmd frame
			frame.addPayload((byte) 0x01); //del cmd

		   boolean 	trasmitR = transmit_data(frame.getPacketBuffer());
			
			if(trasmitR)
			{
				synchronized (mmFrames) {
					mmFrames.wait(mmCmdTimeOut);
					if (mmFrames.size() > 1) {
						if (mmFrames.getLast().getPayloadBuffer()[1] == 0x01) {
							if(mmFrames.getLast().getPayloadBuffer()[2]==0x01){
							 result= mmFrames.getLast().getPayloadBuffer()[3];
							}
								
						}
					}else{
						CancelCommand();
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
	
	/*******************************
	 * Cancel Last Command
	 *******************************/
	public void CancelCommand() {
		
		//release last command.
		mmFrames.notifyAll();
		
		byte buffer[] = new byte[5];
		buffer[0] = 0x01;
		buffer[1] = 0x03;
		buffer[2] = (byte) 0x80;
		buffer[3] = 0x05;
		buffer[4] = 0x79;
		
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
		frame.setFrameType((byte) 0x01);
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
		frame.setFrameType((byte) 0x20);
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
		frame.setFrameType((byte) 0x04);
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
		frame.setFrameType((byte) 0x12);
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
			
			if(buffer==0x06){ //ack
				mmFrame = new Frame(0);
				synchronized (mmFrames) {
					mmFrame.setFrameType(buffer);
					mmFrames.add(mmFrame);
					mmFrames.notify();
				}
				
				mmParseState = EParseState.cmd;
				
			}else if(buffer==0x01){ //response frame
				mmParseState = EParseState.length;
			}

		} else if (mmParseState == EParseState.length) {
			mmFrame = new Frame(buffer-1);
			mmFrame.setFrameType((byte)0x80);
		    mmParseState = EParseState.data;

		} else if (mmParseState == EParseState.data) {

			mmFrame.addPayload(buffer);
			if (mmFrame.isPayloadFull()) {
				mmParseState = EParseState.checkSum;
			}
		} else if (mmParseState == EParseState.checkSum) {

			if (buffer == mmFrame.calcChecksum()) {
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
		private byte mmFrameType;

		/**
		 * set the frame cmd id;
		 * 
		 * @param cmdId
		 */
		public void setFrameType(byte cmdId) {
			mmFrameType = cmdId;
		}

		/**
		 * set the frame cmd id;
		 * 
		 * @param cmdId
		 */
		public byte getFrameType() {
			return mmFrameType;
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
		public byte calcChecksum() {
			byte result = (byte)0xFF;

//			result ^= 0x01;
//			result += 0x5a;		
//			result += 0x4c;
//			result += 0x43;
			
//			result += mmFrameType;
			result ^= (mmPayloadBuffer.length+1);

			for (int i = 0; i < mmPayloadBuffer.length; i++) {
				result ^= mmPayloadBuffer[i];
			}

			return result;
		}

		/**
		 * get ack packet buffer
		 * 
		 * @return
		 */
		public byte[] getPacketBuffer() {
			byte[] result = new byte[mmPayloadBuffer.length + 3];
			result[0] = 0x01;
//			result[1] = 0x5a;
		
//			result[1] = mmFrameType;
			result[1] = (byte) (mmPayloadBuffer.length+1);
			System.arraycopy(mmPayloadBuffer, 0, result, 2,
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
		Succeed(0x01), ErrorGeneral(0x00);
		private int value;

		EFrameStatus(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	};
}
