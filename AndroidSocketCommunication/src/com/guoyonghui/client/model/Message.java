package com.guoyonghui.client.model;

public class Message {

	public static final int MSG_FROM_SERVER = 0;

	public static final int MSG_FROM_CLIENT = 1;
	
	private String mMsg;
	
	private int mMsgFrom;

	public Message(String msg, int msgFrom) {
		mMsg = msg;
		mMsgFrom = msgFrom;
	}

	public String getMsg() {
		return mMsg;
	}

	public void setMsg(String msg) {
		mMsg = msg;
	}

	public int getMsgFrom() {
		return mMsgFrom;
	}

	public void setMsgFrom(int msgFrom) {
		mMsgFrom = msgFrom;
	}

}
