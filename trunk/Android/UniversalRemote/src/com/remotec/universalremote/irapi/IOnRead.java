package com.remotec.universalremote.irapi;

public interface IOnRead {
	public abstract void OnRead(byte[] buffer,int len);
}