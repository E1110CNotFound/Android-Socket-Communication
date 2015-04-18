package com.guoyonghui.client.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.guoyonghui.client.R;
import com.guoyonghui.client.callback.Callback;
import com.guoyonghui.client.model.Message;
import com.guoyonghui.client.util.CommunicationThread;

public class ChatActivity extends Activity implements Callback {
	
	/***
	 * 本机地址配置信息的key
	 */
	public static final String PREF_LOCALHOST = "pref_localhost";
	
	/***
	 * 服务器地址配置信息的key
	 */
	public static final String PREF_SERVER_IP = "pref_server_ip";
	
	/***
	 * 发送端口配置信息的key
	 */
	public static final String PREF_SEND_PORT = "pref_send_port";
	
	/***
	 * 接收端口配置信息的key
	 */
	public static final String PREF_RECEIVE_PORT = "pref_receive_port";
	
	/***
	 * 本机地址extra的key
	 */
	public static final String EXTRA_LOCALHOST = "extra_localhost";
	
	/***
	 * 服务器地址extra的key
	 */
	public static final String EXTRA_SERVER_IP = "extra_server_ip";
	
	/***
	 * 发送端口extra的key
	 */
	public static final String EXTRA_SEND_PORT = "extra_send_port";
	
	/***
	 * 接收端口extra的key
	 */
	public static final String EXTRA_RECEIVE_PORT = "extra_receive_port";
	
	/***
	 * 设置端口的请求码
	 */
	private static final int REQUEST_CODE_SET_UP_PORT = 0;
	
	private ListView mMsgListView;
	
	private EditText mInputMsgEditText;
	
	private Button mSendMsgButton;
	
	private MsgListAdapter mMsgListAdapter;
	
	private ArrayList<Message> mMsgs;
	
	private CommunicationThread mCommunicationThread;
	
	private String mLocalhost;
	
	private String mServerIP;
	
	private int mSendPort;
	
	private int mReceivePort;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		mCommunicationThread = new CommunicationThread(new Handler(), this);
		mCommunicationThread.start();
		mCommunicationThread.getLooper();
		
		mMsgListView = (ListView)findViewById(R.id.msg_listview);
		mMsgs = new ArrayList<Message>();
		mMsgListAdapter = new MsgListAdapter(mMsgs);
		mMsgListView.setAdapter(mMsgListAdapter);
		
		mInputMsgEditText = (EditText)findViewById(R.id.input_msg_edittext);
		
		mSendMsgButton = (Button)findViewById(R.id.send_msg_button);
		mSendMsgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mServerIP != null) {
					String msg = mInputMsgEditText.getText().toString();
					if(msg != null && !"".equals(msg)) {
						mCommunicationThread.startSendMsg(msg, mServerIP, mSendPort);
					}
				}
			}
		});
		
		mLocalhost = getLocalhost();
		appendMessage("localhost: " + mLocalhost, Message.MSG_FROM_SERVER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_set_up_port) {
			Intent intent = new Intent(this, SetUpPortActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SET_UP_PORT);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data == null) {
			return;
		}
		if(requestCode == REQUEST_CODE_SET_UP_PORT) {
			mSendPort = data.getIntExtra(EXTRA_SEND_PORT, 0);
			mReceivePort = data.getIntExtra(EXTRA_RECEIVE_PORT, 0);
			mServerIP = data.getStringExtra(EXTRA_SERVER_IP);

			appendMessage("Set send port: " + mSendPort, Message.MSG_FROM_SERVER);
			appendMessage("Set receive port: " + mReceivePort, Message.MSG_FROM_SERVER);
			appendMessage("Set server IP: " + mServerIP, Message.MSG_FROM_SERVER);
			
			mCommunicationThread.startReceiveMsg(mReceivePort);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCommunicationThread.closeCommunicationThread();
	}

	@Override
	public void onServerSocketSetUp(boolean isServerSocketSetUp) {
		if(isServerSocketSetUp) {
			appendMessage("Success to set up a server socket on localhost(" + mLocalhost + ") and port(" + mReceivePort + ").", Message.MSG_FROM_SERVER);
		} else {
			appendMessage("Fail to set up a server socket on localhost(" + mLocalhost + ") and port(" + mReceivePort + ").", Message.MSG_FROM_SERVER);
		}
	}

	@Override
	public void onMsgReceived(String msg, String clientIP) {
		appendMessage("from " + clientIP + ":\n" + msg, Message.MSG_FROM_CLIENT);
	}

	@Override
	public void onMsgSended(boolean isMsgSended) {
		if(isMsgSended) {
			appendMessage("to " + mServerIP + ":\n" + mInputMsgEditText.getText().toString(), Message.MSG_FROM_SERVER);
			mInputMsgEditText.setText(null);
		} else {
			appendMessage("send message to " + mServerIP + " failed.", Message.MSG_FROM_SERVER);
			mInputMsgEditText.setText(null);
		}
	}
	
	private void appendMessage(String msg, int msgFrom) {
		Message message = new Message(msg, msgFrom);
		mMsgs.add(message);
		((MsgListAdapter)mMsgListView.getAdapter()).notifyDataSetChanged();
	}
	
	private String getLocalhost() {
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(!wm.isWifiEnabled()) {
			Toast.makeText(this, R.string.error_wifi_not_available, Toast.LENGTH_SHORT).show();
			return null;
		}
		WifiInfo wi = wm.getConnectionInfo();
		int localhost = wi.getIpAddress();
		String localhostAddr = (localhost & 0xFF) + "." + ((localhost >> 8) & 0xFF) + "." + ((localhost >> 16) & 0xFF) + "." + ((localhost >> 24) & 0xFF);
		
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
		.edit()
		.putString(PREF_LOCALHOST, localhostAddr)
		.commit();
		
		return localhostAddr;
	}
	
	private class MsgListAdapter extends ArrayAdapter<Message> {

		public MsgListAdapter(ArrayList<Message> msgs) {
			super(ChatActivity.this, 0, msgs);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Message msg = getItem(position);
		
			if(Message.MSG_FROM_SERVER == msg.getMsgFrom()) {
				convertView = ChatActivity.this.getLayoutInflater().inflate(R.layout.chat_item_server, parent, false);
				TextView serverMsgTextView = (TextView)convertView.findViewById(R.id.server_chat_item_textview);
				serverMsgTextView.setText(msg.getMsg());
			} else {
				convertView = ChatActivity.this.getLayoutInflater().inflate(R.layout.chat_item_client, parent, false);
				TextView clientMsgTextView = (TextView)convertView.findViewById(R.id.client_chat_item_textview);
				clientMsgTextView.setText(msg.getMsg());
			}
			
			return convertView;
		}
		
	}
}
