/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 * 
 * Author: Walker
 */
package com.remotec.universalremote.irapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.remotec.universalremote.activity.DeviceActivity;
import com.remotec.universalremote.data.RemoteUi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android_serialport_api.SerialPort;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class SerialPortConnectionManager extends IIo implements IConnectionManager {
	// Debugging
	private static final String TAG = "SerialPortConnectionManager";
	private static final boolean D = false;

	private Handler mHandler;

	private ConnectedThread mConnectedThread;
	private int mState;
	private SerialPort mmSerialPort;

	private static final int REQUEST_ENABLE_BT = 3;

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	// Check if BT Adapter is Available;
	public boolean isAdapterAvailable() {
		return true;
	}

	// Check if BT Adapter is Enable;
	public boolean isAdapterEnabled() {
		return true;
	}

	// pup up dialog for use to make the adapter enable.
	public void makeAdapterEnabled(Activity activity) {
		
	}

	/**
	 * Constructor. Prepares a new BluetoothRemote session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public SerialPortConnectionManager(Handler handler) {
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(DeviceActivity.CONNECTTION_STATE_CHANGE, state,
				-1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}


	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            address The BluetoothDevice to connect
	 */
	public synchronized void connect(String deviceAddr) {
		if (D)
			Log.d(TAG, "connect to: " + deviceAddr);


		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		try {
			mmSerialPort=null;
			mmSerialPort=new SerialPort(new File(deviceAddr),115200,0);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mmSerialPort!=null){
		  connected(mmSerialPort,deviceAddr);
		}else{
		  connectionFailed();
		}
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(SerialPort serialPort,String address) {
		if (D)
			Log.d(TAG, "connected");

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(serialPort);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler
				.obtainMessage(DeviceActivity.MESSAGE_DEVICE_ADDRESS);
		Bundle bundle = new Bundle();
		bundle.putString(DeviceActivity.DEVICE_ADDRESS, address);
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// Send the name of the connected device back to the UI Activity
		msg = mHandler.obtainMessage(DeviceActivity.MESSAGE_DEVICE_NAME);
		bundle = new Bundle();
		bundle.putString(DeviceActivity.DEVICE_NAME, "Remotec IR Extender");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		// if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread =
		// null;}
		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);

	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		if (D)
			Log.d(TAG, "connectionFailed");

		setState(STATE_NONE);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(DeviceActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(DeviceActivity.TOAST, "Unable to connect device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		if (D)
			Log.d(TAG, "connectionLost");
		setState(STATE_NONE);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(DeviceActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(DeviceActivity.TOAST, "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}


	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final SerialPort mmSerialPort;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		// private final LinkedList<byte[]> mmInputs;

		public ConnectedThread(SerialPort serialPort) {
			Log.d(TAG, "create ConnectedThread");
			mmSerialPort = serialPort;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			tmpIn = mmSerialPort.getInputStream();
			tmpOut = mmSerialPort.getOutputStream();

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					if (D) {

						Log.d(TAG, String.format("read bytes=%d", bytes));

						for (int i = 0; i < bytes; i++) {
							Log.d(TAG, String.format("bytes " + i + " =%x",
									buffer[i]));
						}
					}

					if (mmIOnRead != null) {
						mmIOnRead.OnRead(buffer, bytes);
					}

				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);

				if (D) {

					Log.d(TAG, String.format("write bytes =%d", buffer.length));
					for (int i = 0; i < buffer.length; i++) {
						Log.d(TAG,
								String.format("bytes " + i + " =%x", buffer[i]));
					}
				}

			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				Log.d(TAG, "cancel");
				mmInStream.close();
				mmOutStream.close();
				mmSerialPort.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}
}
