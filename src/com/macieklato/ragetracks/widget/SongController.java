package com.macieklato.ragetracks.widget;

import com.macieklato.ragetracks.R;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class SongController implements OnClickListener{
	
	public static final int PLAYING = 0;
	public static final int PAUSED = 1;
	public static final int LOADING = 2;
	public static final int UNINITIALIZED = 3;
	
	public static MediaPlayer player;
	private static View previous;
	private static int state = UNINITIALIZED;
	
	private static SongStateListener listener;

	private Song song;
	
	public SongController(Song song) {
		this.song = song;
	}

	@Override
	public void onClick(View arg0) {
		if(state == LOADING) return;
		if(previous == null || arg0.equals(previous)) {
			switch(state) {
			case UNINITIALIZED:
				init(arg0);
				break;
			case PAUSED:
				play(arg0);
				break;
			case PLAYING:
				pause(arg0);
				break;
			case LOADING:
				break;
			}
		} 
		else {
			stop(previous);
			load(arg0);
		}
		previous = arg0;
	}
	
	private void init(View v) {
		player = new MediaPlayer();
		load(v);
	}
	
	private void load(final View v) {
		player.setOnPreparedListener(new OnPreparedListener(){
			public void onPrepared(MediaPlayer player) {
				play(v);
			}
		});
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try{
			player.setDataSource(song.getStreamUrl());
		} catch(Exception e) {
			e.printStackTrace();
		}
		player.prepareAsync();
		state = LOADING;
	}
	
	public static boolean play() {
		if(previous != null) {
			play(previous);
			return true;
		}
		return false;
	}
	
	private static void play(View v) {
		player.start();
		ImageView play = (ImageView)v.getTag(R.id.play);
        play.setVisibility(View.GONE);
        ImageView pause = (ImageView)v.getTag(R.id.pause);
        pause.setVisibility(View.VISIBLE);
        state = PLAYING;
        if(listener != null) listener.onPlay();
	}
	
	public static boolean pause() {
		if(previous != null) {
			pause(previous);
			return true;
		}
		return false;
		
	}
	
	private static void pause(View v) {
		player.pause();
		ImageView play = (ImageView)v.getTag(R.id.play);
        play.setVisibility(View.VISIBLE);
        ImageView pause = (ImageView)v.getTag(R.id.pause);
        pause.setVisibility(View.GONE);
        state = PAUSED;
        if(listener != null) listener.onPause();
	}
	
	public static boolean stop() {
		if(previous != null) {
			stop(previous);
			return true;
		}
		return false;
	}
	
	private static void stop(View v) {
		player.pause();
		player.stop();
		player.reset();
        ImageView play = (ImageView)v.getTag(R.id.play);
        play.setVisibility(View.GONE);
        ImageView pause = (ImageView)v.getTag(R.id.pause);
        pause.setVisibility(View.GONE);
        state = UNINITIALIZED;
	}
	
	public static boolean seek(int msec) {
		if(player != null) {
			player.seekTo(msec);
			return true;
		}
		return false;
	}
	
	public void kill() {
		if(player != null) {
			player.pause();
			player.stop();
			player.reset();
			player.release();
		}
		state = UNINITIALIZED;
	}
	
	public static void setOnStateChangedListener(SongStateListener listener) {
		SongController.listener = listener;
	}
	
	public static boolean isPlaying() {
		return state == PLAYING;
	}
	
	public static boolean isPaused() {
		return state == PAUSED;
	}
	

}
