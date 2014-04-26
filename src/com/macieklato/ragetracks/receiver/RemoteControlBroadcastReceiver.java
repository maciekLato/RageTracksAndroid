package com.macieklato.ragetracks.receiver;
import com.macieklato.ragetracks.controller.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class RemoteControlBroadcastReceiver extends BroadcastReceiver {

	public String ComponentName = RemoteControlBroadcastReceiver.class
			.getName();
	
	public String TAG = "RemoteControlReceiver";

	public void onReceive(Context context, Intent intent) {

		if (intent.getAction() != Intent.ACTION_MEDIA_BUTTON)
			return;

		// The event will fire twice, up and down.
		// we only want to handle the down event though.
		KeyEvent key = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (key.getAction() != KeyEvent.ACTION_DOWN)
			return;
		
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(context, MainActivity.class);
		context.startActivity(intent);
	}
}
