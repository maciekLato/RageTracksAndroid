package com.macieklato.ragetracks.receiver;

import com.macieklato.ragetracks.controller.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;


public class MusicBroadcastReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        if (!intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            return;
        KeyEvent key = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, key);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(context, MainActivity.class);
		context.startActivity(intent);
    }
}