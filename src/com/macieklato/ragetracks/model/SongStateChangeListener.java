package com.macieklato.ragetracks.model;

public interface SongStateChangeListener {
	public void onPlay(Song s);
	public void onPause(Song s);
	public void onStop(Song s);
}
