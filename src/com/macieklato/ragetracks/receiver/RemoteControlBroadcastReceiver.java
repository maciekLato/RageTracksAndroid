package com.macieklato.ragetracks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.macieklato.ragetracks.controller.ApplicationController;
import com.macieklato.ragetracks.service.StreamingBackgroundService;

public class RemoteControlBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "RemoteControlBroadcastReceiver";

	public String ComponentName = RemoteControlBroadcastReceiver.class
			.getName();

	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");

		if (!intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON))
			return;

		// The event will fire twice, up and down.
		// we only want to handle the down event though.
		KeyEvent key = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (key.getAction() != KeyEvent.ACTION_DOWN)
			return;

		int action = StreamingBackgroundService.ACTION_PLAY;

		switch (key.getKeyCode()) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			action = StreamingBackgroundService.ACTION_TOGGLE_PLAYBACK;
			break;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			action = StreamingBackgroundService.ACTION_PLAY;
			break;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			action = StreamingBackgroundService.ACTION_PAUSE;
			break;
		case KeyEvent.KEYCODE_MEDIA_STOP:
			action = StreamingBackgroundService.ACTION_STOP;
			break;
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			action = StreamingBackgroundService.ACTION_NEXT;
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			action = StreamingBackgroundService.ACTION_PREVIOUS;
			break;
		default:
			return;
		}

		ApplicationController.getInstance().sendCommand(action);
	}
}
