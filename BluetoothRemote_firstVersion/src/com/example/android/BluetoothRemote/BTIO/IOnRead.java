package com.example.android.BluetoothRemote.BTIO;

public interface IOnRead {
	public abstract void OnRead(byte[] buffer,int len);
}