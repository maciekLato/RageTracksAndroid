package com.macieklato.ragetracks.model;

import java.util.ArrayList;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.util.Log;

public class SongController {

	public static final int UNINITIALIZED = -1;
	public static final int PLAYING = 0;
	public static final int PAUSED = 1;
	public static final int LOADING = 2;
	public static final int UPDATE_DELAY = 1000;

	//static variables
	private static SongController singleton = new SongController();

	//instance variables
	private MediaPlayer media;
	private int state = UNINITIALIZED;
	private Song song;
	private ArrayList<SongStateChangeListener> listeners;
	private Handler h;
	private Runnable updateRunnable;
	
	//private methods
	private SongController(){
		media = new MediaPlayer();
		media.setOnPreparedListener(new OnPreparedListener(){
			public void onPrepared(MediaPlayer player) {
				play();
			}
		});
		media.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer player, int what, int extra) {
				for(SongStateChangeListener listener: listeners) {
					listener.onError(song);
				}
				stop();
				return false;
			}
		});
		media.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		listeners = new ArrayList<SongStateChangeListener>();
		
		h = new Handler();
		updateRunnable = new Runnable() {
			@Override
			public void run() {
				update();
				h.postDelayed(updateRunnable, UPDATE_DELAY);
			}
		};
		h.post(updateRunnable);
	}
	
	private void update() {
		if(song != null && media.isPlaying()) {
			for(SongStateChangeListener listener: listeners) {
				listener.onSongUpdate(media.getCurrentPosition(), media.getDuration());
			}
		}
	}
	
	private void play() {
		media.start();
		state = PLAYING;
		for(SongStateChangeListener listener: listeners) {
			listener.onPlay(song);
		}
	}
	
	private void stop() {
		if(media.isPlaying()) {
			media.stop();
		}
		media.reset();
		if(song != null) {
			for(SongStateChangeListener listener: listeners) {
				listener.onStop(song);
			}
		}
		state = UNINITIALIZED;
	}
	
	private void pause() {
		media.pause();
		state = PAUSED;
		for(SongStateChangeListener listener: listeners) {
			listener.onPause(song);
		}
	}
	
	private void loadSong(Song s) {
		stop();
		state = LOADING;
		song = s;
		try{
			media.setDataSource(song.getStreamUrl());
		} catch(Exception e) {
			e.printStackTrace();
		}
		for(SongStateChangeListener listener: listeners) {
			listener.onLoading(song);
		}
		media.prepareAsync();
	}
	
	//public methods
	public static SongController getInstance() {
	   return singleton;
	}
	
	public void toggle(Song s) {
		if(state != LOADING) {
			if(s == song) {
				if(state == PLAYING) pause();
				else play();
			} else {
				loadSong(s);
			}
		}
	}
	
	public void toggle() {
		if(state == PLAYING) pause();
		else if (state == PAUSED) play();
	}
	
	public void seek(float percent) {
		if(state == PLAYING || state == PAUSED) {
			int seek = (int)(media.getDuration()*percent);
			int minutes = media.getDuration()/1000/60;
			int seconds = media.getDuration()/1000-(60*minutes);
			int seekMinutes = seek/1000/60;
			int seekSeconds = seek/1000-(60*seekMinutes);
			Log.d("media", "seek "+seekMinutes+":"+seekSeconds+" / "+minutes+":"+seconds);
			media.seekTo(seek);
		}
	}
	
	public void destroy() {
		if(media.isPlaying()) {
			media.pause();
		}
		media.stop();
		media.release();
		h.removeCallbacks(updateRunnable);
	}
	
	public Song getSong() {
		return song;
	}
	
	public void addStateListener(SongStateChangeListener listener) {
		listeners.add(listener);
	}
	

}