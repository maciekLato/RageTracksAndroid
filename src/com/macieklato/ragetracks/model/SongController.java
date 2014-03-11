package com.macieklato.ragetracks.model;

import android.media.MediaPlayer;

public class SongController {

	public static final int PLAYING = 0;
	public static final int PAUSED = 0;
	public static final int LOADING = 0;
	public static final int UNINITIALIZED = 0;

	
	private MediaPlayer media;
	private int state = UNINITIALIZED;
	private Song song;
	
	private static SongController singleton = new SongController();
	
	private SongController(){ }
	
	public static SongController getInstance() {
	   return singleton;
	}
	
	public void toggle(Song s) {
		if(state == UNINITIALIZED) {
			init();
			loadSong(s);
		} else if(state != LOADING) {
			if(s == song) {
				if(state == PLAYING) media.pause();
				else media.start();
			} else {
				media.reset();
				loadSong(s);
			}
		}
	}
	
	private void init() {
	}
	
	private void loadSong(Song s) {
		state = LOADING;
	}
}