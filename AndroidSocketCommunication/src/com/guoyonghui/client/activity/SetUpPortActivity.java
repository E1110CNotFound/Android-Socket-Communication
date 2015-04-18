package com.guoyonghui.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.guoyonghui.client.R;

public class SetUpPortActivity extends Activity {
	
	private EditText mServerIPEditText;

	private EditText mSendPortEditText;

	private EditText mReceivePortEditText;

	private Button mSavePortButton;
	
	private void setPortResult(int sendPort, int receivePort, String serverIP) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		preferences.edit().putInt(ChatActivity.PREF_SEND_PORT, sendPort).commit();
		preferences.edit().putInt(ChatActivity.PREF_RECEIVE_PORT, receivePort).commit();
		preferences.edit().putString(ChatActivity.PREF_SERVER_IP, serverIP).commit();
		
		Intent intent = new Intent();
		intent.putExtra(ChatActivity.EXTRA_SEND_PORT, sendPort);
		intent.putExtra(ChatActivity.EXTRA_RECEIVE_PORT, receivePort);
		intent.putExtra(ChatActivity.EXTRA_SERVER_IP, serverIP);
		setResult(RESULT_OK, intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_up_port);
		
		mServerIPEditText = (EditText)findViewById(R.id.input_server_ip_edittext);
		if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(ChatActivity.PREF_SERVER_IP, null) != null) {
			mServerIPEditText.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(ChatActivity.PREF_SERVER_IP, null));
		}

		mSendPortEditText = (EditText)findViewById(R.id.input_send_port_edittext);
		if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(ChatActivity.PREF_SEND_PORT, 0) != 0) {
			mSendPortEditText.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(ChatActivity.PREF_SEND_PORT, 0) + "");
		}

		mReceivePortEditText = (EditText)findViewById(R.id.input_receive_port_edittext);
		if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(ChatActivity.PREF_RECEIVE_PORT, 0) != 0) {
			mReceivePortEditText.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(ChatActivity.PREF_RECEIVE_PORT, 0) + "");
		}

		mSavePortButton = (Button)findViewById(R.id.save_port_button);
		mSavePortButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String serverIP = mServerIPEditText.getText().toString();
					int sendPort = Integer.parseInt(mSendPortEditText.getText().toString());
					int receivePort = Integer.parseInt(mReceivePortEditText.getText().toString());
					
					setPortResult(sendPort, receivePort, serverIP);
					
					finish();
				} catch(NumberFormatException e) {
					e.printStackTrace();
					Toast.makeText(SetUpPortActivity.this, R.string.error_wrong_port, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

}
