package com.macieklato.ragetracks.model;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

public class SongController {

	public static final int PLAYING = 0;
	public static final int PAUSED = 1;
	public static final int LOADING = 2;

	//static variables
	private static SongController singleton = new SongController();

	//instance variables
	private MediaPlayer media;
	private int state = -1;
	private Song song;
	
	//private methods
	private SongController(){
		media = new MediaPlayer();
	}
	
	private void play() {
		media.start();
		state = PLAYING;
	}
	
	private void pause() {
		media.pause();
		state = PAUSED;
	}
	
	private void loadSong(Song s) {
		state = LOADING;
		song = s;
		media.setOnPreparedListener(new OnPreparedListener(){
			public void onPrepared(MediaPlayer player) {
				play();
			}
		});
		media.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try{
			media.setDataSource(song.getStreamUrl());
		} catch(Exception e) {
			e.printStackTrace();
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
				media.reset();
				loadSong(s);
			}
		}
	}
	
	public void seek(int msec) {
		if(state == PLAYING || state == PAUSED) {
			media.seekTo(msec);
		}
	}
	
	public void destroy() {
		media.pause();
		media.stop();
		media.release();
	}

}