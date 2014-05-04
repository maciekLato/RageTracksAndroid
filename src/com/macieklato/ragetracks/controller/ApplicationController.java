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

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.service.StreamingBackgroundService;
import com.macieklato.ragetracks.util.BitmapLruCache;
import com.macieklato.ragetracks.util.JSONUtil;
import com.macieklato.ragetracks.util.Network;

/**
 * This is the main controller for the applicaion, it defines a list of state
 * update codes that it broadcasts to any listening receivers It also controls
 * all networking via the Volley library
 * 
 * @author Justin Thorsen
 */

public class ApplicationController extends Application {

	// Tags - for logging messages
	public static final String TAG = "ApplicationController";
	public static final String REQUEST_TAG = "RageTracks";
	public static final String IMAGE_REQUEST_TAG = "AlbumArt";

	// Updates - for broadcasts
	public static final String ACTION_UPDATE = "com.macieklato.ragetracks.action.APP_UPDATE";
	public static final String EXTRA_UPDATE = "com.macieklato.ragetracks.extra.APP_UPDATE";
	public static final int UPDATE_LOADING_SONGS = 0;
	public static final int UPDATE_FINISH_LOADING_SONGS = 1;
	public static final int UPDATE_ERROR_LOADING_SONGS = 2;
	public static final int UPDATE_LOADING_WAVEFORMS = 3;
	public static final int UPDATE_FINISH_LOADING_WAVEFORMS = 4;
	public static final int UPDATE_ERROR_LOADING_WAVEFORMS = 5;
	public static final int UPDATE_FINISH = 6;

	// Number of songs to load during each request
	public static final int COUNT = 50;

	/**
	 * Global variables
	 */
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private BitmapLruCache cache;
	private int page;
	private String search;
	private String category;
	private boolean loading;
	private boolean moreSongs;

	/**
	 * A singleton instance of the application class for easy access in other
	 * places
	 */
	private static ApplicationController instance;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		init();
	}

	/**
	 * defines initial state
	 */
	private void init() {
		instance = this;
		page = 1;
		search = "";
		category = "";
		loading = false;
		moreSongs = true;
	}

	/**
	 * @return ApplicationController singleton instance
	 */
	public static synchronized ApplicationController getInstance() {
		return instance;
	}

	/**
	 * @return The Volley Request queue, the queue will be created if it is null
	 */
	public RequestQueue getRequestQueue() {
		Log.d(TAG, "getRequestQueue");
		// lazy initialize the request queue, the queue instance will be
		// created when it is accessed for the first time
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	/**
	 * @return The Volley image loader, the image loader will be created if it
	 *         is null
	 */
	public ImageLoader getImageLoader() {
		Log.d(TAG, "getImageLoader");

		if (mImageLoader == null) {
			cache = new BitmapLruCache();
			mImageLoader = new ImageLoader(getRequestQueue(), cache);
		}
		return mImageLoader;
	}

	/**
	 * @return current search string
	 */
	public String getSearch() {
		return search;
	}

	/**
	 * @return current category string (genre)
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return current page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page
	 *            - new page
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @param str
	 *            - new search string
	 */
	public void setSearch(String str) {
		search = str;
		category = "";
	}

	/**
	 * @param str
	 *            - new category string
	 */
	public void setCategroy(String str) {
		search = "";
		category = str;
	}

	/**
	 * Send a directed command to StreamingBackgroundService
	 * 
	 * @param action
	 *            - command action, one of StreamingBackgroundService.ACTION_*
	 */
	public void sendCommand(int action) {
		Song song = SongController.getInstance().getSong();
		if (song != null)
			sendCommand(action, song.getId());
		else
			sendCommand(action, -1);
	}

	/**
	 * Send a directed command to StreamingBackgroundService, include id of song
	 * to enact action on
	 * 
	 * @param action
	 *            - command action, one of StreamingBackgroundService.ACTION_*
	 * @param songid
	 *            - id of the sond to act on
	 */
	public void sendCommand(int action, long songid) {
		Intent intent = new Intent(getApplicationContext(),
				StreamingBackgroundService.class);
		intent.putExtra(StreamingBackgroundService.EXTRA_ACTION, action);
		intent.putExtra(StreamingBackgroundService.EXTRA_SONG_ID, songid);
		startService(intent);
	}

	/**
	 * Sends a position update command to StreamingBackground service
	 * 
	 * @param position
	 *            - the new absolute position to set in milliseconds
	 */
	public void sendSeekCommand(int position) {
		long songid = -1;
		Song song = SongController.getInstance().getSong();
		if (song != null)
			songid = song.getId();
		Intent intent = new Intent(getApplicationContext(),
				StreamingBackgroundService.class);
		intent.putExtra(StreamingBackgroundService.EXTRA_ACTION,
				StreamingBackgroundService.ACTION_SEEK);
		intent.putExtra(StreamingBackgroundService.EXTRA_ABSOLUTE_POSITION,
				position);
		intent.putExtra(StreamingBackgroundService.EXTRA_SONG_ID, songid);
		startService(intent);
	}

	/**
	 * Sends a position update command to StreamingBackground service
	 * 
	 * @param position
	 *            - the new relative position to set in percentage of duration
	 */
	public void sendSeekCommand(float percent) {
		long songid = -1;
		Song song = SongController.getInstance().getSong();
		if (song != null)
			songid = song.getId();
		Intent intent = new Intent(getApplicationContext(),
				StreamingBackgroundService.class);
		intent.putExtra(StreamingBackgroundService.EXTRA_ACTION,
				StreamingBackgroundService.ACTION_SEEK);
		intent.putExtra(StreamingBackgroundService.EXTRA_RELATIVE_POSITION,
				percent);
		intent.putExtra(StreamingBackgroundService.EXTRA_SONG_ID, songid);
		startService(intent);
	}

	/**
	 * Sends an update broadcast to notify registered receivers
	 * 
	 * @param update
	 *            - type of update, one of ApplicationController.UPDATE_*
	 */
	public void sendUpdate(int update) {
		Log.d(TAG, "sendUpdate");
		Intent intent = new Intent(ACTION_UPDATE);
		intent.putExtra(EXTRA_UPDATE, update);
		sendBroadcast(intent);
	}

	/**
	 * Asynchronously loads more songs
	 */
	public void loadSongs() {
		loadSongs(COUNT);
	}

	/**
	 * Asynchronously loads more songs
	 * 
	 * @param count
	 *            - number of songs to load
	 */
	public synchronized void loadSongs(int count) {
		Log.d(TAG, "loadSongs");
		if (loading)
			return; // only make one request at a time
		loading = true;
		if (!moreSongs)
			return;

		// response handler
		Listener<JSONObject> onResponse = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject obj) {
				try {
					// on response, parse JSON
					JSONArray arr = obj.getJSONArray(Network.POSTS);
					ArrayList<Song> songs = JSONUtil.parsePosts(arr, page);

					if (songs.size() <= 0) { // if no results notify user
						Toast.makeText(
								getApplicationContext(),
								"Sorry, no more songs matching this genre or search.",
								Toast.LENGTH_SHORT).show();
						moreSongs = false;
					} else { // if results add them to SongController and get
								// waveforms
						SongController.getInstance().addSongs(songs);
						ArrayList<String> tracks = new ArrayList<String>();
						for (Song s : songs) {
							tracks.add(s.getTrack());
						}
						getWaveformUrls(songs, tracks);
						page++;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				loading = false;
				sendUpdate(UPDATE_FINISH_LOADING_SONGS); // broadcast update
															// finish loasing
															// songs
			}
		};

		// error handler
		ErrorListener onError = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError e) {
				e.printStackTrace();
				loading = false;
				sendUpdate(UPDATE_ERROR_LOADING_SONGS); // broadcast song
														// loading error
			}

		};

		// create http post request
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair(Network.JSON, "1"));
		data.add(new BasicNameValuePair(Network.COUNT, "" + count));
		data.add(new BasicNameValuePair(Network.INCLUDE, Network.INCLUDE_ALL));
		data.add(new BasicNameValuePair(Network.PAGE, "" + page));

		if (search.length() > 0) // include search string if valid
			data.add(new BasicNameValuePair(Network.SEARCH, "" + search));

		String url = Network.HOST;
		if (category.length() > 0) // include category string (genre) if valid
			url += Network.CATEGORY + category + "/";
		url += "?";
		url += URLEncodedUtils.format(data, "utf-8");

		Log.d(TAG, "loadSongs: " + url);

		sendUpdate(UPDATE_LOADING_SONGS); // broadcast start loading update

		JsonObjectRequest req = new JsonObjectRequest(url, null, onResponse,
				onError);
		req.addMarker(REQUEST_TAG);
		getRequestQueue().add(req); // start request
	}

	/**
	 * Asynchronously load song waveform urls from soundcloud
	 * 
	 * @param songs
	 *            - list of songs to load waveforms for
	 * @param tracks
	 *            - list of tracks identifying songs on soundcloud
	 */
	public void getWaveformUrls(final ArrayList<Song> songs,
			final ArrayList<String> tracks) {
		Log.d(TAG, "getWaveformUrls");

		// response handler
		Listener<JSONArray> onResponse = new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray arg0) { // parse results
				Map<String, String> map = JSONUtil.parseWaveformUrls(arg0);
				for (Song s : songs) { // set each songs waveform url
					String url = map.get(s.getTrack());
					s.setWaveformUrl(url);
				}
				sendUpdate(UPDATE_FINISH_LOADING_WAVEFORMS); // broadcast update
			}
		};

		// error handler
		ErrorListener onError = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError e) {
				e.printStackTrace();
				sendUpdate(UPDATE_ERROR_LOADING_WAVEFORMS); // broadcast
															// waveform loading
															// error
			}

		};

		// create http post request
		String ids = tracks.toString();
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair(Network.TRACK_IDS, ids.substring(1,
				ids.length() - 1)));
		data.add(new BasicNameValuePair(Network.CLIENT_ID_ARG,
				Network.CLIENT_ID));
		String url = Network.SOUND_CLOUD_TRACKS;
		url += "?";
		url += URLEncodedUtils.format(data, "utf-8");

		Log.d(TAG, "getWaveforms: " + url);

		sendUpdate(UPDATE_LOADING_WAVEFORMS); // broadcast waveform loading
												// update

		JsonArrayRequest req = new JsonArrayRequest(url, onResponse, onError);
		req.setTag(REQUEST_TAG);
		getRequestQueue().add(req); // make request
	}

	/**
	 * cancel all volley requests
	 */
	public void cancelAll() {
		getRequestQueue().cancelAll(REQUEST_TAG);
	}

	/**
	 * reset page count, song list and current song index, also cancel any
	 * volley requests
	 */
	public synchronized void reset() {
		cancelAll();
		loading = false;
		moreSongs = true;
		page = 1;
		SongController.getInstance().reset();
		sendUpdate(UPDATE_FINISH_LOADING_SONGS); // broadcast update
	}

	/**
	 * clean up
	 */
	public void destroy() {
		reset();
		init();
		SongController.getInstance().destroy();
		sendUpdate(UPDATE_FINISH); // broadcast end of service
	}

	/**
	 * asynchronously loads a bitmap
	 * 
	 * @param url
	 *            - url of bitmap
	 * @param listener
	 *            - response handler
	 */
	public synchronized void getBitmap(final String url,
			final Listener<Bitmap> listener) {
		Log.d(TAG, "getBitmap");

		RequestQueue queue = getRequestQueue();
		queue.cancelAll(IMAGE_REQUEST_TAG);

		ImageLoader loader = getImageLoader();
		if (loader.isCached(url, 0, 0)) {
			loader.get(url, new ImageListener() {

				@Override
				public void onErrorResponse(VolleyError arg0) {
				}

				@Override
				public void onResponse(ImageContainer arg0, boolean arg1) {
					Bitmap bmp = arg0.getBitmap();
					if (bmp != null && !bmp.isRecycled()) { // if the cached
															// image
															// // is valid use
															// that
						listener.onResponse(bmp);
					} else {
						getNewBitmap(url, listener);
					}
				}
			});
		} else {
			getNewBitmap(url, listener);
		}
	}

	private synchronized void getNewBitmap(final String url,
			final Listener<Bitmap> listener) {
		ImageRequest req = new ImageRequest(url, new Listener<Bitmap>() {
			@Override
			public void onResponse(Bitmap bmp) {
				if (bmp != null && !bmp.isRecycled()) { // if the cached image
					// is valid use that
					listener.onResponse(bmp);
				} else {
					// give up
				}
			}
		}, 0, 0, null, null);
		req.addMarker(IMAGE_REQUEST_TAG);
		RequestQueue queue = getRequestQueue();
		queue.cancelAll(IMAGE_REQUEST_TAG);
		queue.add(req);
	}

}
