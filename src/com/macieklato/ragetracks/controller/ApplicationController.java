package com.macieklato.ragetracks.controller;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ApplicationController extends Application {

	/**
	 * Global Volley variables
	 */
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private String TAG = "RageTracks";

	/**
	 * A singleton instance of the application class for easy access in other
	 * places
	 */
	private static ApplicationController instance;

	@Override
	public void onCreate() {
		super.onCreate();

		instance = this;
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

		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(getRequestQueue(),
					new ImageLoader.ImageCache() {
						private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(
								50);

						public void putBitmap(String url, Bitmap bitmap) {
							mCache.put(url, bitmap);
						}

						public Bitmap getBitmap(String url) {
							return mCache.get(url);
						}
					});
		}
		return mImageLoader;
	}

}
