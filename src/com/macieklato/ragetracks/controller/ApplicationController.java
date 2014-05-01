package com.macieklato.ragetracks.controller;

import java.net.URL;
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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.service.StreamingBackgroundService;
import com.macieklato.ragetracks.util.BitmapLruCache;
import com.macieklato.ragetracks.util.JSONUtil;
import com.macieklato.ragetracks.util.Network;

public class ApplicationController extends Application {

	// TAG
	public static final String TAG = "ApplicationController";
	public static final String REQUEST_TAG = "RageTracks";

	// Updates
	public static final String ACTION_UPDATE = "com.macieklato.ragetracks.action.APP_UPDATE";
	public static final String EXTRA_UPDATE = "com.macieklato.ragetracks.extra.APP_UPDATE";
	public static final int UPDATE_LOADING_SONGS = 0;
	public static final int UPDATE_FINISH_LOADING_SONGS = 1;
	public static final int UPDATE_ERROR_LOADING_SONGS = 2;
	public static final int UPDATE_LOADING_WAVEFORMS = 3;
	public static final int UPDATE_FINISH_LOADING_WAVEFORMS = 4;
	public static final int UPDATE_ERROR_LOADING_WAVEFORMS = 5;
	public static final int UPDATE_FINISH = 6;

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

	private void init() {
		instance = this;
		page = 1;
		search = "";
		category = "";
		loading = false;
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

	public String getSearch() {
		return search;
	}

	public String getCategory() {
		return category;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setSearch(String str) {
		search = str;
		category = "";
	}

	public void setCategroy(String str) {
		search = "";
		category = str;
	}

	public void sendCommand(int action) {
		Song song = SongController.getInstance().getSong();
		if (song != null)
			sendCommand(action, song.getId());
		else
			sendCommand(action, -1);
	}

	public void sendCommand(int action, long songid) {
		Intent intent = new Intent(getApplicationContext(),
				StreamingBackgroundService.class);
		intent.putExtra(StreamingBackgroundService.EXTRA_ACTION, action);
		intent.putExtra(StreamingBackgroundService.EXTRA_SONG_ID, songid);
		startService(intent);
	}

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

	public void sendUpdate(int update) {
		Log.d(TAG, "sendUpdate");
		Intent intent = new Intent(ACTION_UPDATE);
		intent.putExtra(EXTRA_UPDATE, update);
		sendBroadcast(intent);
	}

	public void loadSongs() {
		loadSongs(COUNT);
	}

	public synchronized void loadSongs(int count) {
		Log.d(TAG, "loadSongs");
		if (loading)
			return;
		loading = true;
		Listener<JSONObject> onResponse = new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject obj) {
				try {
					JSONArray arr = obj.getJSONArray(Network.POSTS);
					ArrayList<Song> songs = JSONUtil.parsePosts(arr, page);
					if (songs.size() <= 0) {
						Toast.makeText(
								getApplicationContext(),
								"Sorry, no songs matching this genre or search.",
								Toast.LENGTH_SHORT).show();
					} else {
						ArrayList<String> tracks = new ArrayList<String>();
						for (Song s : songs) {
							tracks.add(s.getTrack());
						}
						getWaveformUrls(songs, tracks);
						if (songs.size() > 0)
							page++;
						SongController.getInstance().addSongs(songs);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				loading = false;
				sendUpdate(UPDATE_FINISH_LOADING_SONGS);
			}
		};

		ErrorListener onError = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError e) {
				e.printStackTrace();
				loading = false;
				sendUpdate(UPDATE_ERROR_LOADING_SONGS);
			}

		};

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair(Network.JSON, "1"));
		data.add(new BasicNameValuePair(Network.COUNT, "" + count));
		data.add(new BasicNameValuePair(Network.INCLUDE, Network.INCLUDE_ALL));
		data.add(new BasicNameValuePair(Network.PAGE, "" + page));
		if (search.length() > 0)
			data.add(new BasicNameValuePair(Network.SEARCH, "" + search));

		String url = Network.HOST;
		if (category.length() > 0)
			url += Network.CATEGORY + category + "/";
		url += "?";
		url += URLEncodedUtils.format(data, "utf-8");

		Log.d(TAG, "loadSongs: " + url);

		sendUpdate(UPDATE_LOADING_SONGS);

		JsonObjectRequest req = new JsonObjectRequest(url, null, onResponse,
				onError);
		req.addMarker(REQUEST_TAG);
		getRequestQueue().add(req);
	}

	public void getWaveformUrls(final ArrayList<Song> songs,
			final ArrayList<String> tracks) {
		Log.d(TAG, "getWaveformUrls");

		Listener<JSONArray> onResponse = new Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray arg0) {
				Map<String, String> map = JSONUtil.parseWaveformUrls(arg0);
				for (Song s : songs) {
					String url = map.get(s.getTrack());
					s.setWaveformUrl(url);
				}
				sendUpdate(UPDATE_FINISH_LOADING_WAVEFORMS);
			}
		};

		ErrorListener onError = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError e) {
				e.printStackTrace();
				sendUpdate(UPDATE_ERROR_LOADING_WAVEFORMS);
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

		Log.d(TAG, "getWaveforms: " + url);

		sendUpdate(UPDATE_LOADING_WAVEFORMS);

		JsonArrayRequest req = new JsonArrayRequest(url, onResponse, onError);
		req.setTag(REQUEST_TAG);
		getRequestQueue().add(req);
	}

	public void cancelAll() {
		getRequestQueue().cancelAll(REQUEST_TAG);
	}

	public void reset() {
		cancelAll();
		page = 0;
		SongController.getInstance().reset();
		sendUpdate(UPDATE_FINISH_LOADING_SONGS);
	}

	public void destroy() {
		reset();
		init();
		SongController.getInstance().destroy();
		sendUpdate(UPDATE_FINISH);
	}

	public void getBitmap(final String url, final Listener<Bitmap> listener) {
		Log.d(TAG, "getBitmap");

		getImageLoader().get(url, new ImageListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
			}

			@Override
			public void onResponse(ImageContainer arg0, boolean arg1) {
				Bitmap bmp = arg0.getBitmap();
				if (bmp != null && bmp.isRecycled()) {
					listener.onResponse(bmp);
				} else {
					getBitmapAsync(url, listener);
				}
			}
		});
	}

	private void getBitmapAsync(final String url,
			final Listener<Bitmap> listener) {
		Log.d(TAG, "getBitmapAsync");

		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			protected Bitmap doInBackground(Void... params) {
				try {
					Bitmap bmp = BitmapFactory.decodeStream(new URL(url)
							.openStream());
					return bmp;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			protected void onPostExecute(Bitmap bmp) {
				if (bmp != null && !bmp.isRecycled()) {
					Log.d(TAG, "not Recycled");

					listener.onResponse(bmp);
				}
			}
		};
		task.execute();
	}

}
