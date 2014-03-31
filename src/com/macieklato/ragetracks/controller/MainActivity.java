package com.macieklato.ragetracks.controller;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.R.id;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.model.SongController;
import com.macieklato.ragetracks.model.SongStateChangeListener;
import com.macieklato.ragetracks.util.JSONUtil;
import com.macieklato.ragetracks.util.Network;

public class MainActivity extends Activity {

	//constant variables
	public static final int COUNT = 5;
	
	// controllers
	private MyAdapter adapter;
	private OnPullListener pullListener;
	private SongController songController = SongController.getInstance();
	private GridView gridView;
	
	//instance vairables
	private int songIndex = -1;
	private int page = 1;
	private boolean autoPlay = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // set view
		gridView = (GridView) findViewById(R.id.gridview);
		setListeners(); // initialize state
		loadSongs();
	}

	/**
	 * initializes any variables for the initial state
	 */
	private void setListeners() {
		// set the pull events for the menu hider
		pullListener = new OnPullListener(
				this.getApplicationContext()) {
			@Override
			public void onBottomToTop() {
				findViewById(R.id.top_menu).setVisibility(View.GONE);
				// findViewById(R.id.nowplaying).setVisibility(View.GONE);
				findViewById(R.id.bottommenu).setVisibility(View.GONE);
			}

			@Override
			public void onTopToBottom() {
				findViewById(R.id.top_menu).setVisibility(View.VISIBLE);
				// findViewById(R.id.nowplaying).setVisibility(View.VISIBLE);
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
				ImageView button = (ImageView)findViewById(R.id.play_pause_button);
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
				ImageView button = (ImageView)findViewById(R.id.play_pause_button);
				button.setImageResource(R.drawable.pause);
				s.setState(Song.PLAYING);
				songIndex = COUNT*(s.getPage()-1)+s.getIndex();
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
				//adapter.removeSong(s);
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
				if(visibleItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
					loadSongs();
				}
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
		songIndex = Math.max(0, songIndex-1);
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
		if(songIndex >= adapter.getCount()-1) {
			loadSongs();
			autoPlay = true;
		} else {
			songIndex++;
			songController.toggle((Song) adapter.getItem(songIndex));
			Log.d("next", "play:"+songIndex);
		}
	}

	/**
	 * callback for clicking the share button
	 * 
	 * @param v
	 *            - the share button view
	 */
	public void onShareClicked(View v) {
		Toast.makeText(this.getApplicationContext(), "You clicked share",
				Toast.LENGTH_SHORT).show();
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
		AsyncTask<Void, Void, ArrayList<Song>> task = new AsyncTask<Void, Void, ArrayList<Song>>() {
			@Override
			protected ArrayList<Song> doInBackground(Void... params) {
				return JSONUtil.parsePosts(Network.load(COUNT, page), page);
			}

			@Override
			protected void onPostExecute(ArrayList<Song> songs) {
				if(songs != null) {
					for (Song s: songs)
					{
						adapter.addSong(s);
					}
					if(songs.size() > 0) page++;
				}
				if(autoPlay) {
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
		if(pullListener.onTouch(null, e)) return true;
		return super.dispatchTouchEvent(e);
	}
}
