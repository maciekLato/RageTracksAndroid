package com.macieklato.ragetracks.controller;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.R.id;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.model.SongController;
import com.macieklato.ragetracks.model.SongStateChangeListener;
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
	
	private RequestQueue queue;
	private ImageLoader mImageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // set view
		gridView = (GridView) findViewById(R.id.gridview);
		setListeners(); // initialize state
		loadSongs();

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
		
		queue = Volley.newRequestQueue(this.getApplicationContext());
		mImageLoader = new ImageLoader(queue, new ImageLoader.ImageCache() {
			private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(
					20);

			public void putBitmap(String url, Bitmap bitmap) {
				mCache.put(url, bitmap);
			}

			public Bitmap getBitmap(String url) {
				return mCache.get(url);
			}
		});
	}

	/**
	 * initializes any variables for the initial state
	 */
	private void setListeners() {
		// set the pull events for the menu hider
		pullListener = new OnPullListener(this.getApplicationContext()) {
			@Override
			public void onBottomToTop() {
				findViewById(R.id.top_menu).setVisibility(View.GONE);
				findViewById(R.id.bottommenu).setVisibility(View.GONE);
			}

			@Override
			public void onTopToBottom() {
				findViewById(R.id.top_menu).setVisibility(View.VISIBLE);
				findViewById(R.id.bottommenu).setVisibility(View.VISIBLE);
			}

			@Override
			public void onLeftToRight() {
				showMenu();
			}

			@Override
			public void onRightToLeft() {
				hideMenu();
			}
		};

		final Context c = this.getApplicationContext();
		songController.addStateListener(new SongStateChangeListener() {

			@Override
			public void onPause(Song s) {
				ImageView button = (ImageView) findViewById(R.id.play_pause_button);
				button.setImageResource(R.drawable.play);
				s.setState(Song.PAUSED);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onLoading(Song s) {
				s.setState(Song.LOADING);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onPlay(Song s) {
				ImageView button = (ImageView) findViewById(R.id.play_pause_button);
				button.setImageResource(R.drawable.pause);
				s.setState(Song.PLAYING);
				songIndex = COUNT * (s.getPage() - 1) + s.getIndex();
				WaveformSeekBar seekBar = (WaveformSeekBar) findViewById(R.id.seek_bar);
				seekBar.setWaveformUrl(s.getWaveformUrl(), mImageLoader);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onStop(Song s) {
				s.setState(Song.IDLE);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onError(Song s) {
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
				int progress = (int) ((float)position * seek.getMax() / duration);
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
				if (visibleItemCount > 0 && lastVisible >= totalItemCount) {
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
				songController.seek(seek.getProgress() * 1f
						/ seek.getMax());
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
		Toast.makeText(this.getApplicationContext(), "You clicked menu",
				Toast.LENGTH_SHORT).show();
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
		Toast.makeText(this.getApplicationContext(), "You clicked search",
				Toast.LENGTH_SHORT).show();
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
		Toast.makeText(this.getApplicationContext(),
				"You clicked commit search", Toast.LENGTH_SHORT).show();
		closeKeyboard();
	}

	/**
	 * callback for when a search is canceled
	 * 
	 * @param v
	 *            - cancel search image view
	 */
	public void onCancelSearchClicked(View v) {
		Toast.makeText(this.getApplicationContext(),
				"You clicked cancel search", Toast.LENGTH_SHORT).show();
		findViewById(R.id.top_menu_1).setVisibility(View.VISIBLE);
		findViewById(R.id.top_menu_2).setVisibility(View.GONE);
		closeKeyboard();
	}

	private void closeKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
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
		// TODO::fix this mock implementation
		View v2 = findViewById(id.bookmark_list);
		float ydpi = this.getResources().getDisplayMetrics().ydpi;
		int collapsedHeight = (int) (2f * ydpi / 160f);
		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) v2
				.getLayoutParams();
		if (v2.getHeight() > collapsedHeight) {
			p.height = collapsedHeight;
			v2.setLayoutParams(p);
		} else {
			p.height = (int) (150 * ydpi / 160f);
			v2.setLayoutParams(p);
		}
		Toast.makeText(this.getApplicationContext(),
				"You clicked toggle bookmarks", Toast.LENGTH_SHORT).show();
	}

	/**
	 * callback for clicking genre header in left-side menu
	 * 
	 * @param v
	 *            - genre header view
	 */
	public void onGenresListClicked(View v) {
		// TODO::fix this mock implementation
		View v2 = findViewById(id.genre_list);
		float ydpi = this.getResources().getDisplayMetrics().ydpi;
		int collapsedHeight = (int) (2f * ydpi / 160f);
		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) v2
				.getLayoutParams();
		if (v2.getHeight() > collapsedHeight) {
			p.height = collapsedHeight;
			v2.setLayoutParams(p);
		} else {
			p.height = (int) (150 * ydpi / 160f);
			v2.setLayoutParams(p);
		}
		Toast.makeText(this.getApplicationContext(),
				"You clicked toggle genres", Toast.LENGTH_SHORT).show();
	}

	/**
	 * loads count number of posts (new songs)
	 * 
	 * @param count
	 */
	public void loadSongs() {
		final Context c = this.getApplicationContext();
		AsyncTask<Void, Void, ArrayList<Song>> task = new AsyncTask<Void, Void, ArrayList<Song>>() {
			@Override
			protected ArrayList<Song> doInBackground(Void... params) {
				JSONArray posts = Network.getPosts(COUNT, page);
				if (posts == null)
					return null;
				return JSONUtil.parsePosts(posts, page);
			}

			@Override
			protected void onPostExecute(ArrayList<Song> songs) {
				if (songs != null) {
					ArrayList<String> tracks = new ArrayList<String>();
					for (Song s : songs) {
						adapter.addSong(s);
						tracks.add(s.getTrack());
					}
					getWaveformUrls(songs, tracks);
					if (songs.size() > 0)
						page++;
				} else {
					Toast.makeText(c,
							"Poor network connection, try turning on wifi",
							Toast.LENGTH_SHORT).show();
				}
				if (autoPlay) {
					onNextClicked(null);
					autoPlay = false;
				}
			}
		};
		task.execute();
	}

	@Override
	public void onBackPressed() {
		SongController.getInstance().destroy();
		super.onBackPressed();
	}

	public boolean dispatchTouchEvent(MotionEvent e) {
		if (e.getRawY() > findViewById(R.id.gridview).getTop()
				&& e.getRawY() < findViewById(R.id.gridview).getBottom()) {
			if (pullListener.onTouch(null, e))
				return true;
		}
		return super.dispatchTouchEvent(e);

	}

	private void getWaveformUrls(final ArrayList<Song> songs,
			final ArrayList<String> tracks) {
		AsyncTask<Void, Void, Map<String, String>> task = new AsyncTask<Void, Void, Map<String, String>>() {
			@Override
			protected Map<String, String> doInBackground(Void... params) {
				JSONArray data = Network.getTrackData(tracks);
				if (data == null)
					return null;
				return JSONUtil.parseWaveformUrls(data);
			}

			@Override
			protected void onPostExecute(Map<String, String> map) {
				for (Song s : songs) {
					String url = map.get(s.getTrack());
					s.setWaveformUrl(url);
				}
			}
		};
		task.execute();
	}
}
