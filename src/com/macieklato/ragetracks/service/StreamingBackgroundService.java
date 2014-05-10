package com.macieklato.ragetracks.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Response.Listener;
import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.controller.ApplicationController;
import com.macieklato.ragetracks.controller.MainActivity;
import com.macieklato.ragetracks.controller.SongController;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.receiver.RemoteControlBroadcastReceiver;

public class StreamingBackgroundService extends Service implements
		OnAudioFocusChangeListener {
	// Tag
	public final String TAG = "StreamingBackgroundService";

	// Extras
	public final static String EXTRA_SONG_ID = "com.macieklato.ragetracks.extra.SONG_ID";
	public final static long DEFAULT_SONG_ID = -1;
	public final static String EXTRA_ABSOLUTE_POSITION = "com.macieklato.ragetracks.extra.ABS_POSITION";
	public final static String EXTRA_RELATIVE_POSITION = "com.macieklato.ragetracks.extra.REL_POSITION";
	public final static int DEFAULT_POSITION = -1;
	public final static String EXTRA_DURATION = "com.macieklato.ragetracks.extra.DURATION";

	// Actions
	public final static String EXTRA_ACTION = "com.macieklato.ragetracks.extra.ACTION";
	public final static int DEFAULT_ACTION = -1;
	public final static int ACTION_PLAY = 0;
	public final static int ACTION_LOAD = 1;
	public final static int ACTION_SEEK = 2;
	public final static int ACTION_PAUSE = 3;
	public final static int ACTION_STOP = 4;
	public final static int ACTION_TOGGLE_PLAYBACK = 5;
	public final static int ACTION_NEXT = 6;
	public final static int ACTION_PREVIOUS = 7;
	public final static int ACTION_KILL = 8;

	// Updates
	public final static String ACTION_UPDATE = "com.macieklato.ragetracks.action.UPDATE";
	public final static String EXTRA_UPDATE = "com.macieklato.ragetracks.extra.UPDATE";
	public final static int UPDATE_PLAY = 0;
	public final static int UPDATE_PAUSE = 1;
	public final static int UPDATE_STOP = 2;
	public final static int UPDATE_LOADING = 3;
	public final static int UPDATE_POSITION = 4;
	public final static int UPDATE_ERROR = 5;
	public final static int UPDATE_DELAY = 1000;

	private MediaPlayer player;
	private AudioManager audioManager;
	private WifiManager wifiManager;
	private WifiManager.WifiLock wifiLock;
	private RemoteControlClient remoteControlClient;
	private ComponentName remoteComponentName;
	private Song song;
	private Handler h;
	private Runnable update;
	private boolean init;
	private Notification notification;
	private RemoteViews remoteViews;
	private Bitmap lockScreenAlbum;
	private Bitmap notificationAlbum;
	private boolean loaded;
	private boolean resumeOnFocus;

	private final int NOTIFICATION_ID = 101293;

	public void onCreate() {
		super.onCreate();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL,
				"ragetracks_wifi_lock");
		resumeOnFocus = false;
		initializeRemote();
		initializePlayer();
		initializeUpdate();
		initializeNotification();
	}

	private void initializeNotification() {
		notification = new Notification();
		notification.icon = R.drawable.rage;
		if (supportsCustomNotification()) {
			remoteViews = new RemoteViews(getPackageName(), R.layout.widget);

			Intent mainIntent = new Intent(getApplicationContext(),
					MainActivity.class);
			PendingIntent mainPendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, mainIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.notification,
					mainPendingIntent);

			Intent nextIntent = new Intent(getApplicationContext(),
					StreamingBackgroundService.class);
			nextIntent.putExtra(EXTRA_ACTION, ACTION_NEXT);
			PendingIntent nextPendingIntent = PendingIntent.getService(
					getApplicationContext(), 0, nextIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.remote_next_button,
					nextPendingIntent);

			Intent previousIntent = new Intent(getApplicationContext(),
					StreamingBackgroundService.class);
			previousIntent.putExtra(EXTRA_ACTION, ACTION_PREVIOUS);
			PendingIntent previousPendingIntent = PendingIntent.getService(
					getApplicationContext(), 1, previousIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.remote_previous_button,
					previousPendingIntent);

			Intent toggleIntent = new Intent(getApplicationContext(),
					StreamingBackgroundService.class);
			toggleIntent.putExtra(EXTRA_ACTION, ACTION_TOGGLE_PLAYBACK);
			PendingIntent togglePendingIntent = PendingIntent.getService(
					getApplicationContext(), 2, toggleIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.remote_play_pause_button,
					togglePendingIntent);

			Intent cancelIntent = new Intent(getApplicationContext(),
					StreamingBackgroundService.class);
			cancelIntent.putExtra(EXTRA_ACTION, ACTION_KILL);
			PendingIntent cancelPendingIntent = PendingIntent.getService(
					getApplicationContext(), 3, cancelIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.remote_cancel_button,
					cancelPendingIntent);
			notification.contentView = remoteViews;
		}
	}

	private void initializeUpdate() {
		h = new Handler();
		update = new Runnable() {
			@Override
			public void run() {
				sendPositionUpdate();
				startUpdateLooper();
			}
		};
	}

	private void startUpdateLooper() {
		h.postDelayed(update, UPDATE_DELAY);
	}

	private void stopUpdateLooper() {
		h.removeCallbacks(update);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void initializeRemote() {
		if (!supportsRemoteControlClient())
			return;
		remoteComponentName = new ComponentName(getApplicationContext()
				.getPackageName(),
				new RemoteControlBroadcastReceiver().ComponentName);
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(remoteComponentName);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0,
				mediaButtonIntent, 0);
		remoteControlClient = new RemoteControlClient(mediaPendingIntent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			remoteControlClient
					.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
							| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_STOP
							| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
							| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
							| RemoteControlClient.FLAG_KEY_MEDIA_POSITION_UPDATE);
		} else {
			remoteControlClient
					.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
							| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_STOP
							| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
							| RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void registerRemoteClient() {
		Log.d(TAG, "registerRemoteClient");
		if (!supportsRemoteControlClient())
			return;

		audioManager.registerMediaButtonEventReceiver(remoteComponentName);
		audioManager.registerRemoteControlClient(remoteControlClient);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void unregisterRemoteClient() {
		Log.d(TAG, "unregisterRemoteClient");
		if (!supportsRemoteControlClient())
			return;
		audioManager.unregisterMediaButtonEventReceiver(remoteComponentName);
		audioManager.unregisterRemoteControlClient(remoteControlClient);
	}

	private void updateMetadata(final Song song) {
		if (!supportsRemoteControlClient())
			return;
		if (remoteControlClient == null)
			return;

		if (song != null) {
			setMetadata(song, lockScreenAlbum);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setMetadata(Song song, Bitmap bmp) {
		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM,
				song.getTitle());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
				song.getArtist());
		metadataEditor.putString(
				MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
				song.getArtist());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
				song.getTitle());
		if (bmp == null) {
			bmp = BitmapFactory.decodeResource(getApplicationContext()
					.getResources(), R.drawable.default_cover);
		}
		metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, bmp);
		metadataEditor.apply();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent == null)
			return Service.START_STICKY;

		long songId = intent.getLongExtra(EXTRA_SONG_ID, DEFAULT_SONG_ID);
		int action = intent.getIntExtra(EXTRA_ACTION, DEFAULT_ACTION);
		switch (action) {
		case ACTION_PLAY:
			Log.d(TAG, "ACTION_PLAY");
			play();
			break;
		case ACTION_LOAD:
			Log.d(TAG, "ACTION_LOAD");
			load(SongController.getInstance().getSongById(songId));
			break;
		case ACTION_STOP:
			Log.d(TAG, "ACTION_STOP");
			stop();
			break;
		case ACTION_PAUSE:
			Log.d(TAG, "ACTION_PAUSE");
			pause();
			break;
		case ACTION_TOGGLE_PLAYBACK:
			Log.d(TAG, "ACTION_TOGGLE_PLAYBACK");
			if (player == null)
				return Service.START_STICKY;
			if (isPrepared() && player.isPlaying())
				pause();
			else
				play();
			break;
		case ACTION_NEXT:
			Log.d(TAG, "ACTION_NEXT");
			next();
			break;
		case ACTION_PREVIOUS:
			Log.d(TAG, "ACTION_PREVIOUS");
			previous();
			break;
		case ACTION_SEEK:
			Log.d(TAG, "ACTION_SEEK");
			int pos = intent.getIntExtra(EXTRA_ABSOLUTE_POSITION,
					DEFAULT_POSITION);
			if (pos != DEFAULT_POSITION) {
				seekTo(pos);
			} else {
				float relPos = intent.getFloatExtra(EXTRA_RELATIVE_POSITION,
						DEFAULT_POSITION);
				if (relPos != DEFAULT_POSITION) {
					seek(relPos);
				}
			}
			break;
		case ACTION_KILL:
			Log.d(TAG, "ACTION_KILL");
			stopSelf();
			break;
		}
		// Set sticky as we are a long running operation
		return Service.START_STICKY;
	}

	// / <summary>
	// / Initializes the player.
	// / </summary>
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void initializePlayer() {
		Log.d(TAG, "intializePlayer");

		init = false;
		loaded = false;
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);

		player.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				loaded = percent >= 100;
			}
		});
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (loaded) {
					next();
					loaded = false;
				}
			}
		});

		// When we have prepared the song start playback
		player.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer player) {
				init = true;
				play();
			}
		});

		player.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				if (supportsRemoteControlClient()) {
					if (remoteControlClient != null)
						remoteControlClient
								.setPlaybackState(RemoteControlClient.PLAYSTATE_ERROR);
				}
				// playback error
				Log.e("Media", "Error in playback resetting: " + what);
				stop();// this will clean up and reset properly.
				return false;
			}
		});

		song = null;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void play() {
		Log.d(TAG, "play");
		if (isPrepared() && player.isPlaying())
			return;
		if (song != null) {
			// We are simply paused so just start again
			player.start();
			sendUpdate(UPDATE_PLAY);

			// Update remote client now that we are playing
			if (supportsRemoteControlClient()) {
				registerRemoteClient();
				remoteControlClient
						.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			}
			if (supportsCustomNotification()) {
				remoteViews.setImageViewResource(R.id.remote_play_pause_button,
						R.drawable.pause);
				updateNotification();
			}
			startUpdateLooper();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void load(Song s) {
		Log.d(TAG, "load");
		if (s == null)
			return;
		loaded = false;
		if (song != null) {
			if (song.equals(s))
				return;
			player.stop();
			player.reset();
			init = false;
			sendUpdate(UPDATE_STOP);
		}
		song = s;
		try {
			player.setDataSource(s.getStreamUrl());
			int focusResult = audioManager.requestAudioFocus(this,
					AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				Log.e(TAG, "Could not get audio focus");
			}

			sendUpdate(UPDATE_LOADING);
			player.prepareAsync();
			aquireWifiLock();
			startForeground(notificationAlbum);
			if (supportsRemoteControlClient()) {
				registerRemoteClient();
				remoteControlClient
						.setPlaybackState(RemoteControlClient.PLAYSTATE_BUFFERING);
				updateMetadata(song);
			}
			updateArt();
		} catch (Exception e) {
			e.printStackTrace();
			sendUpdate(UPDATE_ERROR);
			next();
		}
	}

	private void updateArt() {
		if (song != null) {
			final Song original = song;
			ApplicationController.getInstance().getBitmap(
					song.getThumbnailUrl(), new Listener<Bitmap>() {
						@Override
						public void onResponse(Bitmap bmp) {
							if (bmp == null || bmp.isRecycled()
									|| !original.equals(song)) {
								return;
							}
							Bitmap temp1 = null;
							Bitmap temp2 = null;
							if (supportsRemoteControlClient()) {
								int width = 500;
								int height = 500;
								if (lockScreenAlbum == null) {
									lockScreenAlbum = Bitmap.createBitmap(
											width, height, bmp.getConfig());
								}

								temp1 = Bitmap.createScaledBitmap(bmp, width,
										height, true);
								int[] pixels = new int[width * height];
								temp1.getPixels(pixels, 0, width, 0, 0, width,
										height);
								lockScreenAlbum.setPixels(pixels, 0, width, 0,
										0, width, height);
								updateMetadata(song);
							}
							if (supportsCustomNotification()) {
								int width = (int) getResources().getDimension(
										R.dimen.large_icon_width);
								int height = (int) getResources().getDimension(
										R.dimen.large_icon_height);

								if (notificationAlbum == null) {
									notificationAlbum = Bitmap.createBitmap(
											width, height, bmp.getConfig());
								}

								temp2 = Bitmap.createScaledBitmap(bmp, width,
										height, true);
								int[] pixels = new int[width * height];
								temp2.getPixels(pixels, 0, width, 0, 0, width,
										height);
								notificationAlbum.setPixels(pixels, 0, width,
										0, 0, width, height);
								remoteViews.setImageViewBitmap(
										R.id.remote_picture, notificationAlbum);
								updateNotification();
							}
							if (temp1 != null) {
								temp1.recycle();
							}
							if (temp2 != null) {
								temp2.recycle();
							}
							bmp.recycle();
						}
					});
		}
	}

	private synchronized void updateNotification() {
		NotificationManager nm = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_ID, notification);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void startForeground(Bitmap bmp) {
		Log.d(TAG, "startForeground");

		if (notification == null || remoteViews == null) {
			initializeNotification();
		}

		if (!supportsCustomNotification()) {
			Intent resultIntent = new Intent(this, MainActivity.class);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(getApplicationContext(),
					song.getTitle(), song.getArtist(), resultPendingIntent);
		} else {
			remoteViews.setTextViewText(R.id.remote_artist, song.getArtist());
			remoteViews.setTextViewText(R.id.remote_title, song.getTitle());
			if (bmp == null) {
				remoteViews.setImageViewResource(R.id.remote_picture,
						R.drawable.default_cover);
			} else {
				remoteViews.setImageViewBitmap(R.id.remote_picture, bmp);
			}
		}

		startForeground(NOTIFICATION_ID, notification);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void pause() {
		Log.d(TAG, "pause");
		if (isPrepared() && player.isPlaying()) {
			player.pause();
			sendUpdate(UPDATE_PAUSE);
		}

		if (supportsRemoteControlClient()) {
			remoteControlClient
					.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
		}
		if (supportsCustomNotification()) {
			remoteViews.setImageViewResource(R.id.remote_play_pause_button,
					R.drawable.play);
			updateNotification();
		}
		stopForeground(!supportsCustomNotification());
		stopUpdateLooper();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void stop() {
		Log.d(TAG, "stop");
		if (isPrepared() && player.isPlaying()) {
			player.stop();
			sendUpdate(UPDATE_STOP);
			song = null;
			if (supportsRemoteControlClient()) {
				if (remoteControlClient != null)
					remoteControlClient
							.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
			}
			stopForeground(false);
		}
		stopUpdateLooper();
	}

	private void next() {
		Log.d(TAG, "next");
		load(SongController.getInstance().nextSong());
	}

	private void previous() {
		Log.d(TAG, "prvious");
		load(SongController.getInstance().previousSong());
	}

	private void seek(float percent) {
		Log.d(TAG, "seek");
		int pos = (int) (player.getDuration() * percent);
		seekTo(pos);
	}

	private void seekTo(int pos) {
		Log.d(TAG, "seekTo");
		int minutes = player.getDuration() / 1000 / 60;
		int seconds = player.getDuration() / 1000 - (60 * minutes);
		int seekMinutes = pos / 1000 / 60;
		int seekSeconds = pos / 1000 - (60 * seekMinutes);
		Log.d("media", "seek " + seekMinutes + ":" + seekSeconds + " / "
				+ minutes + ":" + seconds);
		player.seekTo(pos);
		sendPositionUpdate();
	}

	private void reset() {
		Log.d(TAG, "reset");
		song = null;
		player.reset();
		releaseWifiLock();
		unregisterRemoteClient();
		stopUpdateLooper();
		init = false;
	}

	private void aquireWifiLock() {
		Log.d(TAG, "aquireWifiLock");
		if (!wifiLock.isHeld()) {
			wifiLock.acquire();
		}
	}

	private void releaseWifiLock() {
		Log.d(TAG, "releaseWifiLock");
		if (wifiLock.isHeld()) {
			wifiLock.release();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		reset();
		player.release();
		ApplicationController.getInstance().destroy();
		stopForeground(true);
		if (notificationAlbum != null) {
			notificationAlbum.recycle();
		}
		if (lockScreenAlbum != null) {
			lockScreenAlbum.recycle();
		}
	}

	public void onAudioFocusChange(int focusChange) {
		Log.d(TAG, "onAudioFocusChange");
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			if (isPrepared()) {
				if(resumeOnFocus && !player.isPlaying()) {
					player.start();
					resumeOnFocus = false;
				}
				player.setVolume(1.0f, 1.0f);// Turn it up!
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			stop();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			if(isPrepared() && player.isPlaying()) {
				resumeOnFocus = true;
			}
			pause();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			if (isPrepared() && player.isPlaying())
				player.setVolume(.1f, .1f);// turn it down!
			break;
		}
	}

	public static boolean supportsRemoteControlClient() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public static boolean supportsCustomNotification() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private void sendUpdate(int update) {
		Log.d(TAG, "sendUpdate");
		if (song == null)
			return;

		switch (update) {
		case UPDATE_PLAY:
			song.setState(Song.PLAYING);
			SongController.getInstance().setActiveSong(song);
			break;
		case UPDATE_PAUSE:
			song.setState(Song.PAUSED);
			break;
		case UPDATE_STOP:
			song.setState(Song.IDLE);
			break;
		case UPDATE_LOADING:
			song.setState(Song.LOADING);
			break;
		}

		Intent intent = new Intent(ACTION_UPDATE);
		intent.putExtra(EXTRA_UPDATE, update);
		intent.putExtra(EXTRA_SONG_ID, song.getId());
		sendBroadcast(intent);
	}

	private void sendPositionUpdate() {
		Log.d(TAG, "sendPositionUpdate");
		if (song == null)
			return;
		Intent intent = new Intent(ACTION_UPDATE);
		intent.putExtra(EXTRA_UPDATE, UPDATE_POSITION);
		intent.putExtra(EXTRA_SONG_ID, song.getId());
		intent.putExtra(EXTRA_ABSOLUTE_POSITION, player.getCurrentPosition());
		intent.putExtra(EXTRA_DURATION, player.getDuration());
		sendBroadcast(intent);
	}

	private boolean isPrepared() {
		return song != null && init;
	}
}
