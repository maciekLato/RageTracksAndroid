package com.macieklato.ragetracks.widget;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class SongController implements OnClickListener{
	
	public static MediaPlayer player;
	private boolean init = false;
	private Song song;
	
	public SongController(Song song) {
		this.song = song;
	}

	@Override
	public void onClick(View arg0) {
		if(!init || player == null || !player.isPlaying()) play();
		else pause();
	}
	
	private void init() {
		if(player != null) {
			player.pause();
			player.stop();
			player.release();
		}
		Log.d("player", "initializing player:"+song.getStreamUrl());
		player = new MediaPlayer();
		init = true;
		player.setOnPreparedListener(new OnPreparedListener(){
			public void onPrepared(MediaPlayer player) {
				Log.d("player", "prepared:"+song.getStreamUrl());
				play();
			}
		});
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try{
			player.setDataSource(song.getStreamUrl());
		} catch(Exception e) {
			e.printStackTrace();
		}
		player.prepareAsync();
	}
	
	public void play() {
		if(!init || player == null) {
			init();
		} else player.start();
	}
	
	public void pause() {
		player.pause();
	}
	
	public void seek(int msec) {
		if(player != null) {
			player.seekTo(msec);
		}
	}

}
