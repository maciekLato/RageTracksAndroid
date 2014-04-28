package com.macieklato.ragetracks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.macieklato.ragetracks.controller.ApplicationController;
import com.macieklato.ragetracks.service.StreamingBackgroundService;

public class MusicBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "MusicBroadcastReceiver";

	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");

		if (!intent.getAction()
				.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
			return;

		// signal the service to stop!
		ApplicationController.getInstance().sendCommand(
				StreamingBackgroundService.ACTION_STOP);
	}
}