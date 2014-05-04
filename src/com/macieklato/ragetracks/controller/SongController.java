package com.macieklato.ragetracks.controller;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.service.StreamingBackgroundService;

/**
 * Controls the list of songs, and makes requests when needed
 * 
 * @author justin
 * 
 */
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

	/**
	 * @return SongController, will create if null
	 */
	public static synchronized SongController getInstance() {
		if (instance == null) {
			instance = new SongController();
		}
		return instance;
	}

	/**
	 * @param id
	 *            - songid
	 * @return Song with the given id, or null
	 */
	public Song getSongById(long id) {
		Log.d(TAG, "getSongById");

		for (Song s : songs) {
			if (s.getId() == id)
				return s;
		}
		return null;
	}

	/**
	 * @return current Song, or null
	 */
	public Song getSong() {
		Log.d(TAG, "getSong");

		if (songIndex < 0 || songIndex >= songs.size())
			return null;
		return songs.get(songIndex);
	}

	/**
	 * @param index
	 * @return Song at index i, or null
	 */
	public Song getSongAt(int index) {
		Log.d(TAG, "getSongAt");

		if (index < 0 || index >= songs.size())
			return null;
		return songs.get(index);
	}

	/**
	 * Retrieves the next song in the list, or returns null and loads more songs
	 * 
	 * @return next Song in list or null
	 */
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

	/**
	 * Retrieves the previous song in the list, or the current song if no
	 * previous song
	 * 
	 * @return previous Song in list or null
	 */
	public Song previousSong() {
		Log.d(TAG, "getPreviousSong");

		songIndex = Math.max(0, songIndex - 1);
		return getSong();
	}

	/**
	 * adds a Song to the list
	 * 
	 * @param song
	 *            - Song
	 */
	public void addSong(Song song) {
		Log.d(TAG, "addSong");

		this.songs.add(song);
		if (autoPlay) {
			autoPlay = false;
			ApplicationController.getInstance().sendCommand(
					StreamingBackgroundService.ACTION_NEXT);
		}
	}

	/**
	 * adds a list of songs to the list
	 * 
	 * @param songs
	 *            - list of Songs
	 */
	public void addSongs(List<Song> songs) {
		Log.d(TAG, "addSongs");

		this.songs.addAll(songs);
		if (autoPlay) {
			autoPlay = false;
			ApplicationController.getInstance().sendCommand(
					StreamingBackgroundService.ACTION_NEXT);
		}
	}

	/**
	 * @return length of song list
	 */
	public int getNumSongs() {
		return songs.size();
	}

	/**
	 * clears song list and resets current song index to 0
	 */
	public void reset() {
		songIndex = 0;
		autoPlay = false;
		songs.clear();
	}

	/**
	 * skips to the given song, adds to list if necessary
	 * 
	 * @param s
	 *            - Song
	 */
	public void setActiveSong(Song s) {
		songIndex = songs.indexOf(s);
		if (s != null && songIndex < 0) {
			songIndex = songs.size();
			songs.add(s);
		}
	}

	/**
	 * removes singleton and list
	 */
	public void destroy() {
		instance = null;
	}

	/**
	 * @return if currently set to auto-play
	 */
	public boolean getAutoPlay() {
		return autoPlay;
	}
}
