package com.guoyonghui.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.guoyonghui.client.callback.Callback;

public class CommunicationThread extends HandlerThread {

	private static final String TAG = "CommunicationThread";

	private static final int MSG_SEND_MESSAGE = 0;

	private static final int MSG_RECEIVE_MESSAGE = 1;

	private static final int MSG_CLOSE_COMMUNICATION_THREAD = 2;

	private Handler mHandler;

	private Handler mResponseHandler;

	private Callback mCallback;

	private ReceiveServerThread mReceiveServerThread;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == MSG_RECEIVE_MESSAGE) {
					int port = (int)msg.obj;

					if(mReceiveServerThread != null && mReceiveServerThread.isAlive()) {
						mReceiveServerThread.selfDestroy();
					}

					mReceiveServerThread = new ReceiveServerThread(port);
					mReceiveServerThread.start();
				} else if(msg.what == MSG_SEND_MESSAGE) {
					ArrayList<Object> params = (ArrayList<Object>)msg.obj;
					String message = (String)params.get(0);
					String clientIP = (String)params.get(1);
					int clientPort = (int)params.get(2);

					SendMsgThread sendMsgThread = new SendMsgThread(message, clientIP, clientPort);
					sendMsgThread.start();
				} else if(msg.what == MSG_CLOSE_COMMUNICATION_THREAD) {
					quit();
				}
			}

		};
	}

	@Override
	public boolean quit() {
		if(mReceiveServerThread != null && mReceiveServerThread.isAlive()) {
			mReceiveServerThread.selfDestroy();
		}
		return super.quit();
	}

	public CommunicationThread(Handler responseHandler, Callback callback) {
		super(TAG);

		mResponseHandler = responseHandler;
		mCallback = callback;
	}

	public void startReceiveMsg(int port) {
		mHandler.obtainMessage(MSG_RECEIVE_MESSAGE, port).sendToTarget();
	}

	public void startSendMsg(String msg, String clientIP, int clientPort) {
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(msg);
		params.add(clientIP);
		params.add(clientPort);

		mHandler.obtainMessage(MSG_SEND_MESSAGE, params).sendToTarget();
	}

	public void closeCommunicationThread() {
		mHandler.obtainMessage(MSG_CLOSE_COMMUNICATION_THREAD).sendToTarget();
	}

	private void handleServerSocketSetUpResult(final boolean isServerSocketSetUp) {
		mResponseHandler.post(new Runnable() {

			@Override
			public void run() {
				mCallback.onServerSocketSetUp(isServerSocketSetUp);
			}
		});
	}

	private void handleMsgReceivedResult(final String msg, final String clientIP) {
		mResponseHandler.post(new Runnable() {

			@Override
			public void run() {
				mCallback.onMsgReceived(msg, clientIP);
			}
		});
	}

	private void handleMsgSendedResult(final boolean isMsgSended) {
		mResponseHandler.post(new Runnable() {

			@Override
			public void run() {
				mCallback.onMsgSended(isMsgSended);
			}
		});
	}

	private class ReceiveServerThread extends Thread {

		private boolean flag = true;

		private int port;

		private ServerSocket serverSocket;

		public ReceiveServerThread(int port) {
			this.port = port;

			try {
				serverSocket = new ServerSocket(port);
				handleServerSocketSetUpResult(true);
			} catch (IOException e) {
				e.printStackTrace();
				handleServerSocketSetUpResult(false);
			}
		}

		@Override
		public void run() {
			while(flag) {
				Socket socket = null;

				try {
					socket = serverSocket.accept();
					new handleClientSocketThread(socket).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void selfDestroy() {
			flag = false;

			try {
				new Socket("localhost", port);
				if(serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			interrupt();
		}

	}

	private class SendMsgThread extends Thread {

		private String msg;

		private String clientIP;

		private int clientPort;

		public SendMsgThread(String msg, String clientIP, int clientPort) {
			this.msg = msg;
			this.clientIP = clientIP;
			this.clientPort = clientPort;
		}

		@Override
		public void run() {
			Socket socket = null;

			try {
				socket = new Socket(clientIP, clientPort);
				socket.setSoTimeout(5 * 1000);

				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				out.println(msg);
				out.flush();

				out.close();
				socket.close();

				handleMsgSendedResult(true);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private class handleClientSocketThread extends Thread {

		private Socket socket;

		public handleClientSocketThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				String msgString = reader.readLine();
				if(msgString != null) {
					handleMsgReceivedResult(msgString, socket.getInetAddress().getHostAddress());
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(socket != null) {
						socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
