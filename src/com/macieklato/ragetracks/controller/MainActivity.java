package com.macieklato.ragetracks.controller;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.R.id;
import com.macieklato.ragetracks.listener.OnPullListener;
import com.macieklato.ragetracks.model.Category;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.receiver.UpdateBroadcastReceiver;
import com.macieklato.ragetracks.service.StreamingBackgroundService;
import com.macieklato.ragetracks.util.JSONUtil;
import com.macieklato.ragetracks.util.Network;
import com.macieklato.ragetracks.widget.FacebookFragment;
import com.macieklato.ragetracks.widget.WaveformSeekBar;

public class MainActivity extends FragmentActivity {

	public String TAG = "MainActivity";
	public int DURATION = 100;

	// views
	private MyAdapter adapter;
	private GridView gridView;
	private FacebookFragment facebook;

	// listeners
	private OnPullListener pullListener;
	private UpdateBroadcastReceiver updateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		setContentView(R.layout.activity_main); // set view
		gridView = (GridView) findViewById(R.id.gridview);
		setListeners(); // initialize state
		loadCategories();
		if (SongController.getInstance().getNumSongs() <= 0)
			ApplicationController.getInstance().loadSongs();

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
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
		adapter.notifyDataSetChanged();
	}

	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");

		if (intent.getAction() == ApplicationController.ACTION_UPDATE) {
			int code = intent.getIntExtra(ApplicationController.EXTRA_UPDATE,
					-1);
			switch (code) {
			case ApplicationController.UPDATE_LOADING_SONGS:
				onSongsLoading();
				break;
			case ApplicationController.UPDATE_FINISH_LOADING_SONGS:
				onSongsLoaded();
				break;
			case ApplicationController.UPDATE_ERROR_LOADING_SONGS:
				onSongsErrorLoading();
				break;
			case ApplicationController.UPDATE_LOADING_WAVEFORMS:
				onWaveformsLoading();
				break;
			case ApplicationController.UPDATE_FINISH_LOADING_WAVEFORMS:
				onWaveformsLoaded();
				break;
			case ApplicationController.UPDATE_ERROR_LOADING_WAVEFORMS:
				onWaveformsErrorLoading();
				break;
			case ApplicationController.UPDATE_FINISH:
				finish();
				break;
			}
		}

		long id = intent.getLongExtra(StreamingBackgroundService.EXTRA_SONG_ID,
				-1);
		Song s = SongController.getInstance().getSongById(id);
		switch (intent.getIntExtra(StreamingBackgroundService.EXTRA_UPDATE, -1)) {
		case StreamingBackgroundService.UPDATE_PLAY:
			onSongPlay(s);
			break;
		case StreamingBackgroundService.UPDATE_PAUSE:
			onSongPause(s);
			break;
		case StreamingBackgroundService.UPDATE_STOP:
			onSongStop(s);
			break;
		case StreamingBackgroundService.UPDATE_LOADING:
			onSongLoading(s);
			break;
		case StreamingBackgroundService.UPDATE_POSITION:
			int position = intent.getIntExtra(
					StreamingBackgroundService.EXTRA_ABSOLUTE_POSITION, -1);
			int duration = intent.getIntExtra(
					StreamingBackgroundService.EXTRA_DURATION, -1);
			onPosition(position, duration);
			break;
		case StreamingBackgroundService.UPDATE_ERROR:
			onSongError(s);
			break;
		default:
			break;
		}
	}

	private void onSongPlay(Song s) {
		Log.d(TAG, "onSongPlay");
		if (s == null)
			return;
		ImageView button = (ImageView) findViewById(R.id.play_pause_button);
		button.setImageResource(R.drawable.pause);
		WaveformSeekBar seekBar = (WaveformSeekBar) findViewById(R.id.seek_bar);
		seekBar.setWaveformUrl(s.getWaveformUrl(), ApplicationController
				.getInstance().getImageLoader());
		Log.d("waveform", s.getWaveformUrl());
		adapter.notifyDataSetChanged();
	}

	private void onSongPause(Song s) {
		Log.d(TAG, "onSongPause");
		if (s == null)
			return;
		ImageView button = (ImageView) findViewById(R.id.play_pause_button);
		button.setImageResource(R.drawable.play);
		adapter.notifyDataSetChanged();
	}

	private void onSongStop(Song s) {
		Log.d(TAG, "onSongStop");
		if (s == null)
			return;
		adapter.notifyDataSetChanged();
	}

	private void onSongLoading(Song s) {
		Log.d(TAG, "onSongLoading");
		if (s == null)
			return;
		adapter.notifyDataSetChanged();
	}

	private void onSongError(Song s) {
		Log.d(TAG, "onSongError");
		if (s == null)
			return;
		Toast.makeText(getApplicationContext(), "Song unavilable",
				Toast.LENGTH_SHORT).show();
	}

	private void onPosition(int position, int duration) {
		Log.d(TAG, "onPosition");

		int minutes = duration / 1000 / 60;
		int seconds = duration / 1000 % 60;
		int currentMinutes = position / 1000 / 60;
		int currentSeconds = position / 1000 % 60;
		WaveformSeekBar seek = (WaveformSeekBar) findViewById(R.id.seek_bar);
		int progress = (int) ((float) position * seek.getMax() / duration);
		seek.setProgress(progress);
		TextView currentTime = (TextView) findViewById(R.id.current_time);
		currentTime.setText(String.format("%d:%02d", currentMinutes,
				currentSeconds));
		TextView totalTime = (TextView) findViewById(R.id.total_time);
		totalTime.setText(String.format("%d:%02d", minutes, seconds));
	}

	private void onSongsLoading() {
		Log.d(TAG, "onSongsLoading");
		setLoading(true);
	}

	private void onSongsLoaded() {
		Log.d(TAG, "onSongsLoaded");
		setLoading(false);
		adapter.notifyDataSetChanged();
	}

	private void onSongsErrorLoading() {
		Log.d(TAG, "onSongsErrorLoading");
		Toast.makeText(getApplicationContext(),
				"Poor network connection, try turning on wifi",
				Toast.LENGTH_SHORT).show();
	}

	private void onWaveformsLoading() {
		Log.d(TAG, "onWaveformsLoading");
	}

	private void onWaveformsLoaded() {
		Log.d(TAG, "onWaveformsLoaded");
	}

	private void onWaveformsErrorLoading() {
		Log.d(TAG, "onWaveformsErrorLoading");
		Toast.makeText(getApplicationContext(),
				"Poor network connection, try turning on wifi",
				Toast.LENGTH_SHORT).show();
	}

	private synchronized void unregisterReceiver() {
		if (updateReceiver != null) {
			this.unregisterReceiver(updateReceiver);
			updateReceiver = null;
		}
	}

	private synchronized void registerReceiver() {
		updateReceiver = new UpdateBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApplicationController.ACTION_UPDATE);
		filter.addAction(StreamingBackgroundService.ACTION_UPDATE);
		registerReceiver(updateReceiver, filter);
	}

	/**
	 * initializes any variables for the initial state
	 */
	private void setListeners() {
		Log.d(TAG, "setListeners");

		// set the pull events for the menu hider
		pullListener = new OnPullListener(getApplicationContext()) {
			@Override
			public void onBottomToTop(float x, float y) {
				Log.d(TAG, "onBottomToTop");

				if (contains(findViewById(R.id.gridview), x, y)) {
					findViewById(R.id.top_menu).setVisibility(View.GONE);
				}
			}

			@Override
			public void onTopToBottom(float x, float y) {
				Log.d(TAG, "onTopToBottom");

				if (contains(findViewById(R.id.gridview), x, y)) {
					findViewById(R.id.top_menu).setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onLeftToRight(float x, float y) {
				Log.d(TAG, "onLeftToRight");

				if (!contains(findViewById(R.id.seek_bar), x, y)) {
					showMenu();
				}
			}

			@Override
			public void onRightToLeft(float x, float y) {
				Log.d(TAG, "onRightToLeft");

				if (!contains(findViewById(R.id.seek_bar), x, y)) {
					hideMenu();
				}
			}
		};

		adapter = new MyAdapter(this.getApplicationContext());
		gridView.setAdapter(adapter); // add grid view adapter
		gridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.d(TAG, "onScroll");

				int lastVisible = firstVisibleItem + visibleItemCount;
				if (visibleItemCount > 0
						&& lastVisible >= totalItemCount
								- ApplicationController.COUNT) {
					ApplicationController.getInstance().loadSongs();
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
				Log.d(TAG, "onStopTrackingTouch");

				float percent = seek.getProgress() * 1f / seek.getMax();
				ApplicationController.getInstance().sendSeekCommand(percent);
			}

		});

	}

	/**
	 * hides the left side menu and adjusts the main screen
	 */
	private void hideMenu() {
		Log.d(TAG, "hideMenu");

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
		Log.d(TAG, "showMenu");

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
		Log.d(TAG, "onMenuClicked");

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
		Log.d(TAG, "onSearchClicked");

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
		Log.d(TAG, "onCommitSearchClicked");

		EditText userInput = (EditText) findViewById(R.id.search_text_edit);
		ApplicationController.getInstance().setSearch(
				userInput.getText().toString());
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
		Log.d(TAG, "onCancelSearchClicked");

		ApplicationController ac = ApplicationController.getInstance();
		if (ac.getSearch().length() > 0) {
			ac.setSearch("");
			reset();
		}
		EditText userInput = (EditText) findViewById(R.id.search_text_edit);
		userInput.setText("");
		findViewById(R.id.top_menu_1).setVisibility(View.VISIBLE);
		findViewById(R.id.top_menu_2).setVisibility(View.GONE);
		closeKeyboard();
	}

	private void closeKeyboard() {
		Log.d(TAG, "closeKeyboard");

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
		Log.d(TAG, "onBookmarkClicked");

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
		Log.d(TAG, "onPreviousClicked");

		ApplicationController.getInstance().sendCommand(
				StreamingBackgroundService.ACTION_PREVIOUS);
	}

	/**
	 * callback for clicking the play or pause button (same button, different
	 * states)
	 * 
	 * @param v
	 *            - the play/pause view
	 */
	public void onPlayPauseClicked(View v) {
		Log.d(TAG, "onPlayPauseClicked");

		ApplicationController.getInstance().sendCommand(
				StreamingBackgroundService.ACTION_TOGGLE_PLAYBACK);
	}

	/**
	 * callback for clicking the next button
	 * 
	 * @param v
	 *            - the next button view
	 */
	public void onNextClicked(View v) {
		Log.d(TAG, "onNextClicked");

		ApplicationController.getInstance().sendCommand(
				StreamingBackgroundService.ACTION_NEXT);
	}

	/**
	 * callback for clicking the share button
	 * 
	 * @param v
	 *            - the share button view
	 */
	public void onShareClicked(View v) {
		Log.d(TAG, "onShareClicked");

		Song s = SongController.getInstance().getSong();
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
		Log.d(TAG, "onDrawerClicked");

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
		Log.d(TAG, "onBookmarksListClicked");

		if (findViewById(id.bookmark_scroller).getVisibility() == View.VISIBLE) {
			closeBookmarks();
		} else {
			openBookmarks();
		}
	}

	private void closeBookmarks() {
		Log.d(TAG, "closeBookmarks");

		findViewById(R.id.bookmark_scroller).setVisibility(View.GONE);
		findViewById(R.id.bookmark_menu_text).setBackgroundColor(0xff000000);
	}

	private void openBookmarks() {
		Log.d(TAG, "openBookmarks");

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
		Log.d(TAG, "onGenresListClicked");

		if (findViewById(id.genre_scroller).getVisibility() == View.VISIBLE) {
			closeGenres();
		} else {
			openGenres();
		}
	}

	private void closeGenres() {
		Log.d(TAG, "closeGenres");

		findViewById(R.id.genre_scroller).setVisibility(View.GONE);
		findViewById(R.id.genre_menu_text).setBackgroundColor(0xff000000);
	}

	private void openGenres() {
		Log.d(TAG, "openGenres");

		findViewById(R.id.genre_scroller).setVisibility(View.VISIBLE);
		findViewById(R.id.genre_menu_text).setBackgroundColor(0xff222222);
		closeBookmarks();
	}

	private void loadCategories() {
		Log.d(TAG, "loadCategories");

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
								ApplicationController.getInstance()
										.setCategroy(slug);
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

	public boolean dispatchTouchEvent(MotionEvent e) {
		if (adapter.getCount() <= 0) {
			ApplicationController.getInstance().loadSongs();
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

	private void setLoading(boolean loading) {
		Log.d(TAG, "setLoading:" + loading);

		if (loading) {
			findViewById(R.id.mainLoadingIcon).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.mainLoadingIcon).setVisibility(View.GONE);
		}
	}

	private void reset() {
		Log.d(TAG, "reset");

		ApplicationController ap = ApplicationController.getInstance();
		ap.reset();
		ap.loadSongs();
		adapter.notifyDataSetChanged();
	}

}
