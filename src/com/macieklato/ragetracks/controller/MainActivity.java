package com.macieklato.ragetracks.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.R.id;
import com.macieklato.ragetracks.listener.OnPullListener;
import com.macieklato.ragetracks.listener.SongStateChangeListener;
import com.macieklato.ragetracks.model.Category;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.receiver.RemoteControlBroadcastReceiver;
import com.macieklato.ragetracks.util.JSONUtil;
import com.macieklato.ragetracks.util.Network;
import com.macieklato.ragetracks.widget.FacebookFragment;
import com.macieklato.ragetracks.widget.WaveformSeekBar;

public class MainActivity extends FragmentActivity {

	// constant variables
	public static final int COUNT = 10;

	// controllers
	private MyAdapter adapter;
	private OnPullListener pullListener;
	private SongController songController = SongController.getInstance();
	private GridView gridView;
	private FacebookFragment facebook;

	// instance vairables
	private int songIndex = -1;
	private int page = 1;
	private boolean autoPlay = false;
	private String searchText = "";
	private String category = "";
	private boolean loading = false;

	// remote variables
	private RemoteControlClient remoteControlClient;
	private ComponentName remoteComponentName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // set view
		gridView = (GridView) findViewById(R.id.gridview);
		setListeners(); // initialize state
		loadCategories();
		loadSongs();
		loadRemotes();

		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			facebook = new FacebookFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.login_container, facebook).commit();
		} else {
			// Or set the fragment from restored state info
			facebook = (FacebookFragment) getSupportFragmentManager()
					.findFragmentById(R.id.login_container);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {

		KeyEvent key = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		switch (key.getKeyCode()) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			songController.toggle();
			break;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			if (songController.getState() == SongController.PAUSED) {
				songController.toggle();
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			if (songController.getState() == SongController.PLAYING) {
				songController.toggle();
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_STOP:
			songController.stop();
			break;
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			onNextClicked(null);
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			onPreviousClicked(null);
			break;
		default:
			return;
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void loadRemotes() {

		if (supportsRemoteControlClient()) {
			remoteComponentName = new ComponentName(ApplicationController
					.getInstance().getPackageName(),
					new RemoteControlBroadcastReceiver().ComponentName);
		}
	}

	/**
	 * initializes any variables for the initial state
	 */
	private void setListeners() {
		// set the pull events for the menu hider
		pullListener = new OnPullListener(this.getApplicationContext()) {
			@Override
			public void onBottomToTop(float x, float y) {
				if (contains(findViewById(R.id.gridview), x, y)) {
					findViewById(R.id.top_menu).setVisibility(View.GONE);
				}
			}

			@Override
			public void onTopToBottom(float x, float y) {
				if (contains(findViewById(R.id.gridview), x, y)) {
					findViewById(R.id.top_menu).setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onLeftToRight(float x, float y) {
				if (!contains(findViewById(R.id.seek_bar), x, y)) {
					showMenu();
				}
			}

			@Override
			public void onRightToLeft(float x, float y) {
				if (!contains(findViewById(R.id.seek_bar), x, y)) {
					hideMenu();
				}
			}
		};

		final Context c = this.getApplicationContext();
		songController.addStateListener(new SongStateChangeListener() {

			@Override
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			public void onPause(Song s) {
				ImageView button = (ImageView) findViewById(R.id.play_pause_button);
				button.setImageResource(R.drawable.play);
				s.setState(Song.PAUSED);
				adapter.notifyDataSetChanged();
				if (supportsRemoteControlClient()) {
					remoteControlClient
							.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
				}
			}

			@Override
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			public void onLoading(Song s) {
				s.setState(Song.LOADING);
				adapter.notifyDataSetChanged();
				if (supportsRemoteControlClient()) {
					registerRemoteClient();
					remoteControlClient
							.setPlaybackState(RemoteControlClient.PLAYSTATE_BUFFERING);
					updateMetadata(s);
				}
			}

			@Override
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			public void onPlay(Song s) {
				ImageView button = (ImageView) findViewById(R.id.play_pause_button);
				button.setImageResource(R.drawable.pause);
				s.setState(Song.PLAYING);
				songIndex = COUNT * (s.getPage() - 1) + s.getIndex();
				WaveformSeekBar seekBar = (WaveformSeekBar) findViewById(R.id.seek_bar);
				seekBar.setWaveformUrl(s.getWaveformUrl(),
						ApplicationController.getInstance().getImageLoader());
				Log.d("waveform", s.getWaveformUrl());
				adapter.notifyDataSetChanged();

				// Update remote client now that we are playing
				if (supportsRemoteControlClient()) {
					registerRemoteClient();
					remoteControlClient
							.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
				}
			}

			@Override
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			public void onStop(Song s) {
				s.setState(Song.IDLE);
				adapter.notifyDataSetChanged();
				if (supportsRemoteControlClient()) {
					if (remoteControlClient != null)
						remoteControlClient
								.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
				}
			}

			@Override
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			public void onError(Song s) {
				if (supportsRemoteControlClient()) {
					if (remoteControlClient != null)
						remoteControlClient
								.setPlaybackState(RemoteControlClient.PLAYSTATE_ERROR);
				}
				Toast.makeText(c, "Song unavilable", Toast.LENGTH_SHORT).show();
				// adapter.removeSong(s);
			}

			@Override
			public void onSongUpdate(int position, int duration) {
				int minutes = duration / 1000 / 60;
				int seconds = duration / 1000 - (60 * minutes);
				int currentMinutes = position / 1000 / 60;
				int currentSeconds = position / 1000 - (60 * currentMinutes);
				WaveformSeekBar seek = (WaveformSeekBar) findViewById(R.id.seek_bar);
				int progress = (int) ((float) position * seek.getMax() / duration);
				seek.setProgress(progress);
				TextView currentTime = (TextView) findViewById(R.id.current_time);
				currentTime.setText(String.format("%d:%02d", currentMinutes,
						currentSeconds));
				TextView totalTime = (TextView) findViewById(R.id.total_time);
				totalTime.setText(String.format("%d:%02d", minutes, seconds));
			}

		});

		adapter = new MyAdapter(this.getApplicationContext());
		gridView.setAdapter(adapter); // add grid view adapter
		gridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				int lastVisible = firstVisibleItem + visibleItemCount;
				if (!loading && visibleItemCount > 0
						&& lastVisible >= totalItemCount - COUNT) {
					loadSongs();
				}
			}

		});

		final WaveformSeekBar seek = (WaveformSeekBar) findViewById(R.id.seek_bar);
		seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				songController.seek(seek.getProgress() * 1f / seek.getMax());
			}

		});

	}

	/**
	 * hides the left side menu and adjusts the main screen
	 */
	private void hideMenu() {

		// hide side menu
		View menu = findViewById(R.id.leftmenu);
		if (menu.getVisibility() == View.GONE)
			return;
		menu.setVisibility(View.GONE);

		// move main screen to the left
		View mainScreen = findViewById(R.id.main_screen);
		LayoutParams p = (FrameLayout.LayoutParams) mainScreen
				.getLayoutParams();
		p.rightMargin = 0;
	}

	/**
	 * makes the left side menu visible and adjusts the main screen
	 */
	private void showMenu() {
		// show side menu
		View menu = findViewById(R.id.leftmenu);
		if (menu.getVisibility() == View.VISIBLE)
			return;
		menu.setVisibility(View.VISIBLE);

		// move main screen to the right
		View mainScreen = findViewById(R.id.main_screen);
		LayoutParams p = (FrameLayout.LayoutParams) mainScreen
				.getLayoutParams();
		p.rightMargin = -menu.getWidth();
		if (p.rightMargin == 0) { // something went wrong (only happens the
									// first time), this is a bit of a hack
			p.rightMargin = (int) (-200
					* this.getResources().getDisplayMetrics().xdpi / 160f);
		}
	}

	/**
	 * callback for clicking the menu button
	 * 
	 * @param v
	 *            - the menu button view
	 */
	public void onMenuClicked(View v) {
		View v2 = findViewById(R.id.leftmenu);
		if (v2.getVisibility() == View.VISIBLE) {
			hideMenu();
		} else {
			showMenu();
		}
	}

	/**
	 * callback for clicking the search button
	 * 
	 * @param v
	 *            - the search button view
	 */
	public void onSearchClicked(View v) {
		findViewById(R.id.top_menu_1).setVisibility(View.GONE);
		findViewById(R.id.top_menu_2).setVisibility(View.VISIBLE);
	}

	/**
	 * callback for when a search is commited
	 * 
	 * @param v
	 *            - commit search image view
	 */
	public void onCommitSearchClicked(View v) {
		EditText userInput = (EditText) findViewById(R.id.search_text_edit);
		searchText = userInput.getText().toString();
		reset();
		closeKeyboard();
	}

	/**
	 * callback for when a search is canceled
	 * 
	 * @param v
	 *            - cancel search image view
	 */
	public void onCancelSearchClicked(View v) {
		if (searchText.length() > 0) {
			searchText = "";
			EditText userInput = (EditText) findViewById(R.id.search_text_edit);
			userInput.setText("");
			findViewById(R.id.top_menu_1).setVisibility(View.VISIBLE);
			findViewById(R.id.top_menu_2).setVisibility(View.GONE);
			reset();
		}
		closeKeyboard();
	}

	private void closeKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		View v = getCurrentFocus();
		if (v != null) {
			inputManager.hideSoftInputFromWindow(v.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * callback for clicking the bookmark button
	 * 
	 * @param v
	 *            - the bookmark view
	 */
	public void onBookmarkClicked(View v) {
		Toast.makeText(this.getApplicationContext(), "You clicked bookmark",
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * callback for clicking the previous button
	 * 
	 * @param v
	 *            - the previous button view
	 */
	public void onPreviousClicked(View v) {
		songIndex = Math.max(0, songIndex - 1);
		songController.toggle((Song) adapter.getItem(songIndex));
	}

	/**
	 * callback for clicking the play or pause button (same button, different
	 * states)
	 * 
	 * @param v
	 *            - the play/pause view
	 */
	public void onPlayPauseClicked(View v) {
		songController.toggle();
	}

	/**
	 * callback for clicking the next button
	 * 
	 * @param v
	 *            - the next button view
	 */
	public void onNextClicked(View v) {
		if (songIndex >= adapter.getCount() - 1) {
			loadSongs();
			autoPlay = true;
		} else {
			songIndex++;
			songController.toggle((Song) adapter.getItem(songIndex));
			Log.d("next", "play:" + songIndex);
		}
	}

	/**
	 * callback for clicking the share button
	 * 
	 * @param v
	 *            - the share button view
	 */
	public void onShareClicked(View v) {
		Song s = songController.getSong();
		if (s != null) {
			facebook.share(s);
		}
	}

	/**
	 * callback for clicking drawer bottom menu button
	 * 
	 * @param v
	 *            - drawer icon view
	 */
	public void onDrawerClicked(View v) {
		View seek = findViewById(R.id.seek_bar_container);
		if (seek.getVisibility() == View.VISIBLE) {
			seek.setVisibility(View.GONE);
		} else {
			seek.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * callback for clicking bookmark header in left-side menu
	 * 
	 * @param v
	 *            - bookmark header view
	 */
	public void onBookmarksListClicked(View v) {
		if (findViewById(id.bookmark_scroller).getVisibility() == View.VISIBLE) {
			closeBookmarks();
		} else {
			openBookmarks();
		}
	}

	private void closeBookmarks() {
		findViewById(R.id.bookmark_scroller).setVisibility(View.GONE);
		findViewById(R.id.bookmark_menu_text).setBackgroundColor(0xff000000);
	}

	private void openBookmarks() {
		findViewById(R.id.bookmark_scroller).setVisibility(View.VISIBLE);
		findViewById(R.id.bookmark_menu_text).setBackgroundColor(0xff222222);
		closeGenres();
	}

	/**
	 * callback for clicking genre header in left-side menu
	 * 
	 * @param v
	 *            - genre header view
	 */
	public void onGenresListClicked(View v) {
		if (findViewById(id.genre_scroller).getVisibility() == View.VISIBLE) {
			closeGenres();
		} else {
			openGenres();
		}
	}

	private void closeGenres() {
		findViewById(R.id.genre_scroller).setVisibility(View.GONE);
		findViewById(R.id.genre_menu_text).setBackgroundColor(0xff000000);
	}

	private void openGenres() {
		findViewById(R.id.genre_scroller).setVisibility(View.VISIBLE);
		findViewById(R.id.genre_menu_text).setBackgroundColor(0xff222222);
		closeBookmarks();
	}

	/**
	 * loads count number of posts (new songs)
	 * 
	 * @param count
	 */
	public void loadSongs() {
		if (loading)
			return;
		setLoading(true);
		Listener<JSONObject> onResponse = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject obj) {
				try {
					JSONArray arr = obj.getJSONArray(Network.POSTS);
					ArrayList<Song> songs = JSONUtil.parsePosts(arr, page);
					ArrayList<String> tracks = new ArrayList<String>();
					for (Song s : songs) {
						adapter.addSong(s);
						tracks.add(s.getTrack());
					}
					getWaveformUrls(songs, tracks);
					if (songs.size() > 0)
						page++;
					if (autoPlay) {
						onNextClicked(null);
						autoPlay = false;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				setLoading(false);
			}
		};

		ErrorListener onError = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(),
						"Poor network connection, try turning on wifi",
						Toast.LENGTH_SHORT).show();
				setLoading(false);
			}

		};

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair(Network.JSON, "1"));
		data.add(new BasicNameValuePair(Network.COUNT, "" + COUNT));
		data.add(new BasicNameValuePair(Network.INCLUDE, Network.INCLUDE_ALL));
		data.add(new BasicNameValuePair(Network.PAGE, "" + page));
		if (searchText.length() > 0)
			data.add(new BasicNameValuePair(Network.SEARCH, "" + searchText));

		String url = Network.HOST;
		if (category.length() > 0)
			url += Network.CATEGORY + category + "/";
		url += "?";
		url += URLEncodedUtils.format(data, "utf-8");

		Log.d("load", url);

		JsonObjectRequest req = new JsonObjectRequest(url, null, onResponse,
				onError);
		ApplicationController.getInstance().getRequestQueue().add(req);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public boolean dispatchTouchEvent(MotionEvent e) {
		if (adapter.getCount() <= 0) {
			loadSongs();
		}
		if (pullListener.onTouch(null, e))
			return true;
		return super.dispatchTouchEvent(e);

	}

	private static boolean contains(View v, float x, float y) {
		int l[] = new int[2];
		v.getLocationOnScreen(l);
		if (x < l[0] || x > l[0] + v.getWidth() || y < l[1]
				|| y > l[1] + v.getHeight()) {
			return false;
		}
		return true;
	}

	private void getWaveformUrls(final ArrayList<Song> songs,
			final ArrayList<String> tracks) {

		Listener<JSONArray> onResponse = new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray arg0) {
				Map<String, String> map = JSONUtil.parseWaveformUrls(arg0);
				for (Song s : songs) {
					String url = map.get(s.getTrack());
					s.setWaveformUrl(url);
				}
			}
		};

		ErrorListener onError = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError e) {
				e.printStackTrace();
			}

		};

		String ids = tracks.toString();
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair(Network.TRACK_IDS, ids.substring(1,
				ids.length() - 1)));
		data.add(new BasicNameValuePair(Network.CLIENT_ID_ARG,
				Network.CLIENT_ID));
		String url = Network.SOUND_CLOUD_TRACKS;
		url += "?";
		url += URLEncodedUtils.format(data, "utf-8");

		JsonArrayRequest req = new JsonArrayRequest(url, onResponse, onError);
		ApplicationController.getInstance().getRequestQueue().add(req);
	}

	private void loadCategories() {
		Listener<JSONObject> onResponse = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject obj) {
				try {
					JSONArray arr = obj.getJSONArray(Network.CATEGORIES);
					ArrayList<Category> categories = JSONUtil
							.parseCategories(arr);
					LayoutInflater inflater = LayoutInflater
							.from(getApplicationContext());
					LinearLayout genreList = (LinearLayout) findViewById(R.id.genre_list);
					for (Category cat : categories) {
						final String slug = cat.getSlug();
						View v = inflater.inflate(R.layout.genre, null, false);
						TextView genre = (TextView) v.findViewById(R.id.genre);
						genre.setText(cat.getName());
						genre.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								category = slug;
								reset();
							}
						});
						genreList.addView(v);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		ErrorListener onError = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError e) {
				e.printStackTrace();
			}

		};

		String url = Network.HOST + Network.CATEGORY_IDX;

		JsonObjectRequest req = new JsonObjectRequest(url, null, onResponse,
				onError);
		ApplicationController.getInstance().getRequestQueue().add(req);
	}

	private void setLoading(boolean loading) {
		this.loading = loading;
		if (loading) {
			findViewById(R.id.mainLoadingIcon).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.mainLoadingIcon).setVisibility(View.GONE);
		}
	}

	private void reset() {
		songIndex = 0;
		page = 1;
		autoPlay = false;
		SongController.getInstance().reset();
		adapter.reset();
		loadSongs();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void registerRemoteClient() {
		if (!supportsRemoteControlClient())
			return;

		if (remoteControlClient == null) {
			songController.getAudioManager().registerMediaButtonEventReceiver(
					remoteComponentName);

			Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			mediaButtonIntent.setComponent(remoteComponentName);
			PendingIntent mediaPendingIntent = PendingIntent
					.getBroadcast(ApplicationController.getInstance()
							.getApplicationContext(), 0, mediaButtonIntent, 0);
			remoteControlClient = new RemoteControlClient(mediaPendingIntent);
			songController.getAudioManager().registerRemoteControlClient(
					remoteControlClient);
		}

		// add transport control flags we can to handle
		remoteControlClient
				.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
						| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
						| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
						| RemoteControlClient.FLAG_KEY_MEDIA_STOP
						| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
						| RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
	}

	// / Unregisters the remote client from the audio manger
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void unregisterRemoteClient() {
		if (!supportsRemoteControlClient())
			return;
		songController.getAudioManager().unregisterMediaButtonEventReceiver(
				remoteComponentName);
		songController.getAudioManager().unregisterRemoteControlClient(
				remoteControlClient);
		remoteControlClient = null;
	}

	// / Updates the metadata on the lock screen
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void updateMetadata(final Song song) {
		if (!supportsRemoteControlClient())
			return;
		if (remoteControlClient == null)
			return;
		setMetadata(song, null);
		ApplicationController.getInstance().getImageLoader()
				.get(song.getThumbnailUrl(), new ImageListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						arg0.printStackTrace();
					}

					@Override
					public void onResponse(ImageContainer arg0, boolean arg1) {
						if (song.equals(songController.getSong())) {
							setMetadata(song, arg0.getBitmap());
						}
					}
				});

	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setMetadata(Song song, Bitmap bmp) {
		if (!supportsRemoteControlClient())
			return;
		if (remoteControlClient == null)
			return;

		if (bmp == null) {
			bmp = BitmapFactory.decodeResource(ApplicationController
					.getInstance().getApplicationContext().getResources(),
					R.drawable.default_cover);
		}
		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
				song.getArtist());
		metadataEditor.putString(
				MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
				song.getArtist());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM,
				song.getTitle());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
				song.getTitle());
		metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, bmp);
		metadataEditor.apply();
	}

	private boolean supportsRemoteControlClient() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public void onDestroy() {
		super.onDestroy();
		songController.destroy();
		unregisterRemoteClient();
	}
}
