package com.remote.universalremote.irapi;

import android.util.Log;

import com.remote.universalremote.data.Device;
import com.remote.universalremote.data.Key;
import com.remote.universalremote.data.Key.Mode;

/*
 * AsyncTask for Learning.
 */
public class EmitTask extends android.os.AsyncTask<Integer, Integer, Integer> {

	// Debugging Tags
	private static final String TAG = "EmitTask";
	private static final boolean D = false;
	
	private static int count=0;
	private Device mDevice;
	private Key mKey;
	private byte mEmitType;

	public EmitTask(Device device, Key key, byte emitType) {
		super();
		mKey = key;
		mEmitType = emitType;
		mDevice=device;
	}

	@Override
	protected Integer doInBackground(Integer... params) {

		IrApi irController = IrApi.getHandle();

		if (irController != null) {

			synchronized (irController) {
				try {
				if (mDevice!=null&&mKey != null) {
					if (mKey.getMode() == Mode.BuildIn) {
						boolean result = irController.transmitPreprogramedCode(
								mEmitType, (byte) mDevice.getDeviceTypeId(),
								mDevice.getIrCode(), (byte) mKey.getKeyId());
					} else if (mKey.getMode() == Mode.Learn) {

						irController.transmitLearnData(mEmitType, mKey.getData());

					} else if (mKey.getMode() == Mode.UIRD) {

						boolean result = irController.transmitIrData(mEmitType,
								mKey.getData());
					}
				}else{
					irController.IrTransmitStop();
				}
				
				Thread.sleep(150);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return 0;
	}

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected void onProgressUpdate(Integer... progress) {

	}

	@Override
	protected void onPostExecute(Integer result) {

	}
}