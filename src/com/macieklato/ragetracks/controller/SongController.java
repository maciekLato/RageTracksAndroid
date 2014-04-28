package com.macieklato.ragetracks.controller;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.service.StreamingBackgroundService;

public class SongController {

	public static final String TAG = "SongController";

	private static SongController instance = null;
	private List<Song> songs;
	private int songIndex;
	private boolean autoPlay;

	private SongController() {
		Log.d(TAG, "constructor");

		songs = new ArrayList<Song>();
		songIndex = -1;
		autoPlay = false;
	}

	public static synchronized SongController getInstance() {
		if (instance == null) {
			instance = new SongController();
		}
		return instance;
	}

	public Song getSongById(long id) {
		Log.d(TAG, "getSongById");

		for (Song s : songs) {
			if (s.getId() == id)
				return s;
		}
		return null;
	}

	public Song getSong() {
		Log.d(TAG, "getSong");

		if (songIndex < 0 || songIndex >= songs.size())
			return null;
		return songs.get(songIndex);
	}

	public Song getSongAt(int index) {
		Log.d(TAG, "getSongAt");

		if (index < 0 || index >= songs.size())
			return null;
		return songs.get(index);
	}

	public Song nextSong() {
		Log.d(TAG, "getNextSong");

		Song s = getSongAt(songIndex + 1);
		if (s == null) {
			autoPlay = true;
			ApplicationController.getInstance().loadSongs();
		} else {
			songIndex++;
		}
		return s;
	}

	public Song previousSong() {
		Log.d(TAG, "getPreviousSong");

		songIndex = Math.max(0, songIndex - 1);
		return getSong();
	}

	public void addSong(Song song) {
		Log.d(TAG, "addSong");

		this.songs.add(song);
		if (autoPlay) {
			autoPlay = false;
			ApplicationController.getInstance().sendCommand(
					StreamingBackgroundService.ACTION_NEXT);
		}
	}

	public void addSongs(List<Song> songs) {
		Log.d(TAG, "addSongs");

		this.songs.addAll(songs);
		if (autoPlay) {
			autoPlay = false;
			ApplicationController.getInstance().sendCommand(
					StreamingBackgroundService.ACTION_NEXT);
		}
	}

	public int getNumSongs() {
		return songs.size();
	}

	public void reset() {
		songIndex = 0;
		autoPlay = false;
		songs.clear();
	}

	public void setActiveSong(Song s) {		
		songIndex = songs.indexOf(s);
	}

	public void destroy() {
		instance = null;
	}
}
