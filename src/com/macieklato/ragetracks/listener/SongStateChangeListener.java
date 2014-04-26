package com.macieklato.ragetracks.listener;

import com.macieklato.ragetracks.model.Song;

public interface SongStateChangeListener {
	public void onPlay(Song s);

	public void onLoading(Song s);

	public void onPause(Song s);

	public void onStop(Song s);

	public void onError(Song s);

	public void onSongUpdate(int position, int duration);
}
