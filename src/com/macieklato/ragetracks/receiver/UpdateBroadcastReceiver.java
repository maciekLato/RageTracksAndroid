package com.macieklato.ragetracks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.macieklato.ragetracks.controller.ApplicationController;
import com.macieklato.ragetracks.controller.MainActivity;
import com.macieklato.ragetracks.service.StreamingBackgroundService;

public class UpdateBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "UpdateBroadcastReceiver";

	public String ComponentName = UpdateBroadcastReceiver.class.getName();

	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");

		if (intent.getAction() != StreamingBackgroundService.ACTION_UPDATE
				&& intent.getAction() != ApplicationController.ACTION_UPDATE)
			return;

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(context, MainActivity.class);
		context.startActivity(intent);
	}
}
