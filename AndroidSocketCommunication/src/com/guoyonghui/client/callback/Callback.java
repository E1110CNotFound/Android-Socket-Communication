package com.guoyonghui.client.callback;

public interface Callback {

	/***
	 * ����ServerSocket�Ļص��ӿ�
	 * @param isServerSocketSetUp ServerSocket�Ƿ����ɹ�
	 */
	public void onServerSocketSetUp(boolean isServerSocketSetUp);
	
	/***
	 * �յ��ͻ�����Ϣ�Ļص��ӿ�
	 * @param msg �ͻ��˷��͹�������Ϣ
	 * @param clientIP �ͻ���IP
	 */
	public void onMsgReceived(String msg, String clientIP);
	
	/***
	 * ��ͻ��˷�����Ϣ�Ļص��ӿ�
	 * @param isMsgSended ��ͻ��˷�����Ϣ�Ƿ�ɹ�
	 */
	public void onMsgSended(boolean isMsgSended);
	
}
