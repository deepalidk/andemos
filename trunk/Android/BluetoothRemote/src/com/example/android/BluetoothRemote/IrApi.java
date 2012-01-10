package com.example.android.BluetoothRemote;

import java.util.LinkedList;

import android.text.format.Time;
import android.util.Log;

import com.example.android.BluetoothRemote.BTIO.IIo;
import com.example.android.BluetoothRemote.BTIO.IOnRead;

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
	private int mmRetransimitTime = 100;

	/**
	 * time to Retransimit(ms)
	 */
	private int mmRetransimitCount = 2;

	public IrApi() {
		mmIIo = null;
		mmParseState = EParseState.cmd;
		mmFrames = new LinkedList<Frame>();
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

		if(D)
			Log.d("TimeElapsed","0");
		if(D)
			Log.d("TimeElapsed","1");
		if(mmIIo==null)return false;
		
		if (D)
			Log.d(TAG, "transmit_data");
		int reTransmit = mmRetransimitCount;

		if (D)
			Log.d(TAG, "transmit_data:clear mmFrames");
		mmFrames.clear();

		
		Time t1=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
		Time t2=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
		Time t3;
		t1.setToNow(); // 取得系统时间。
		
         


		do {	
			mmIIo.write(TXbuf);

			if(D)
				Log.d("TimeElapsed","2");
			if (D)
				Log.d(TAG, "transmit_data:wait");
			synchronized (mmFrames) {
				mmFrames.wait(mmRetransimitTime);
				if (mmFrames.size() > 0) {
					t2.setToNow();
					long t=t2.toMillis(true)-t1.toMillis(true);
					
					String msg=String.format("%d", t);
					
					if(D)
						Log.d("TimeElapsed",msg);
					return true;
				}
			}
		} while (reTransmit-- > 0);

		return false;
	}

	/***** IR API *******************/

	/**
	 * @param iIo
	 *            the interface of IO
	 * @return true - succeeded. false- failed
	 */
	public boolean init(IIo iIo) {
		mmIIo = iIo;
		mmIIo.setOnReadFunc(this);
		mmParseState = EParseState.cmd;

		byte[] version = new byte[2];
		boolean result = IrGetVersion(version);

		if (!D) {
			if (result == false) {
				mmIIo.setOnReadFunc(null);
				mmIIo = null;
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
	public boolean IrGetVersion(byte[] version) {

		if (D)
			Log.d(TAG, "IrGetVersion");
		Frame frame = new Frame(0);
		frame.setCmdID((byte) 0x09);
		boolean result = false;

		try {

			result = transmit_data(frame.getPacketBuffer());

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

		return result;
	}

	/*******************************
	 * stop IR transmission
	 *******************************/
	public void IrTransmitStop() {
		byte buffer[] = new byte[1];
		buffer[1] = 0x00;
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
			frame.addPayload((byte) (codeNum >>> 8));
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
				if (mmFrame.getPayloadBuffer()[0] == EFrameStatus.Succeed
						.getValue()) {
					synchronized (mmFrames) {
						mmFrames.add(mmFrame);
						mmFrames.notify();
					}
				}
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

			result += 0x45;
			result += 0x5A;
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
			result[0] = 0x45;
			result[1] = 0x5A;
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
