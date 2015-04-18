package com.guoyonghui.client.callback;

public interface Callback {

	/***
	 * 创建ServerSocket的回调接口
	 * @param isServerSocketSetUp ServerSocket是否建立成功
	 */
	public void onServerSocketSetUp(boolean isServerSocketSetUp);
	
	/***
	 * 收到客户端信息的回调接口
	 * @param msg 客户端发送过来的信息
	 * @param clientIP 客户端IP
	 */
	public void onMsgReceived(String msg, String clientIP);
	
	/***
	 * 向客户端发送信息的回调接口
	 * @param isMsgSended 向客户端发送信息是否成功
	 */
	public void onMsgSended(boolean isMsgSended);
	
}
