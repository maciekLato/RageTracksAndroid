package com.macieklato.ragetracks.controller;

import java.util.ArrayList;

import com.macieklato.ragetracks.listener.SongStateChangeListener;
import com.macieklato.ragetracks.model.Song;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

public class SongController implements OnAudioFocusChangeListener {

	public static final int UNINITIALIZED = -1;
	public static final int PLAYING = 0;
	public static final int PAUSED = 1;
	public static final int LOADING = 2;
	public static final int UPDATE_DELAY = 1000;

	// static variables
	private static SongController singleton = null;

	// instance variables
	private MediaPlayer player;
	private int state = UNINITIALIZED;
	private Song song;
	private ArrayList<SongStateChangeListener> listeners;
	private Handler h;
	private Runnable updateRunnable;
	private WifiManager wifiManager;
	private WifiManager.WifiLock wifiLock;
	private AudioManager audioManager;

	// private methods
	private SongController() {
		intializePlayer();
		
		audioManager = (AudioManager) ApplicationController.getInstance()
				.getSystemService(Context.AUDIO_SERVICE);
		wifiManager = (WifiManager) ApplicationController.getInstance()
				.getSystemService(Context.WIFI_SERVICE);

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
		if (song != null && player.isPlaying()) {
			for (SongStateChangeListener listener : listeners) {
				listener.onSongUpdate(player.getCurrentPosition(),
						player.getDuration());
			}
		}
	}

	private void play() {
		int focusResult = audioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
		}
		player.start();
		state = PLAYING;
		for (SongStateChangeListener listener : listeners) {
			if (song != null)
				listener.onPlay(song);
		}
	}

	public void stop() {
		if(player != null) {
			if (state != LOADING && player.isPlaying()) {
				player.stop();
			}
			player.reset();
			if (song != null) {
				for (SongStateChangeListener listener : listeners) {
					if (song != null)
						listener.onStop(song);
				}
			}
		}
		state = UNINITIALIZED;
	}

	private void pause() {
		if (player != null)
			player.pause();
		state = PAUSED;
		for (SongStateChangeListener listener : listeners) {
			if (song != null)
				listener.onPause(song);
		}
	}

	private void aquireWifiLock() {
		if (wifiLock == null) {
			wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL,
					"ragetracks_wifi_lock");
		}
		wifiLock.acquire();
	}

	private void releaseWifiLock() {
		if (wifiLock == null)
			return;

		wifiLock.release();
		wifiLock = null;
	}

	private void loadSong(Song s) {
		stop();
		song = s;
		try {
			player.setDataSource(song.getStreamUrl());
			state = LOADING;
		} catch (Exception e) {
			e.printStackTrace();
			state = UNINITIALIZED;
			song = null;
		}
		for (SongStateChangeListener listener : listeners) {
			listener.onLoading(song);
		}
		player.prepareAsync();
		aquireWifiLock();
	}

	// public methods
	public static SongController getInstance() {
		if (singleton == null) {
			singleton = new SongController();
		}
		return singleton;
	}

	public void toggle(Song s) {
		if (state != LOADING) {
			if (s == song) {
				toggle();
			} else {
				loadSong(s);
			}
		}
	}

	public void toggle() {
		if (state == PLAYING)
			pause();
		else if (state == PAUSED)
			play();
	}

	public void seek(float percent) {
		seekTo( (int) (player.getDuration() * percent) );
	}
	
	public void seekTo(int seek) {
		if (state == PLAYING || state == PAUSED) {
			int minutes = player.getDuration() / 1000 / 60;
			int seconds = player.getDuration() / 1000 - (60 * minutes);
			int seekMinutes = seek / 1000 / 60;
			int seekSeconds = seek / 1000 - (60 * seekMinutes);
			Log.d("media", "seek " + seekMinutes + ":" + seekSeconds + " / "
					+ minutes + ":" + seconds);
			player.seekTo(seek);
		}
	}
	
	public int getPosition() {
		return player.getCurrentPosition();
	}

	public void destroy() {
		h.removeCallbacks(updateRunnable);
		if (player != null) {
			player.release();
			player = null;
		}
		releaseWifiLock();
		singleton = null;
	}

	public Song getSong() {
		return song;
	}

	public void addStateListener(SongStateChangeListener listener) {
		listeners.add(listener);
	}

	public int getState() {
		return state;
	}

	public void reset() {
		stop();
		//song = null;
		releaseWifiLock();
	}

	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			if (player == null)
				intializePlayer();

			if (!player.isPlaying()) {
				play();
			}

			player.setVolume(1.0f, 1.0f);// Turn it up!
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			reset();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			pause();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			if (player.isPlaying())
				player.setVolume(.1f, .1f);// turn it down!
			break;
		}
	}

	private void intializePlayer() {
		player = new MediaPlayer();
		player.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer player) {
				play();
			}
		});
		player.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer player, int what, int extra) {
				for (SongStateChangeListener listener : listeners) {
					listener.onError(song);
				}
				stop();
				return false;
			}
		});
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setWakeMode(ApplicationController.getInstance()
				.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
	}
	
	public AudioManager getAudioManager() {
		return audioManager;
	}

}