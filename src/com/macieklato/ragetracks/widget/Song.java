package com.macieklato.ragetracks.widget;

import com.macieklato.ragetracks.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Song {
	private String title;
	private String artist;
	private String streamUrl;
	private Bitmap thumbnail;
	private long id;
	
	private static Context context;
	private static Bitmap none;
	
	public Song(Context c) {
		if(Song.context == null) Song.context = c;
		setThumbnail(none);
	}
	
	public Song(Context c, long id, String title, String artist, String streamUrl) {
		if(Song.context == null) Song.context = c;
		setId(id);
		setTitle(title);
		setArtist(artist);
		setStreamUrl(streamUrl);
		setThumbnail(none);
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	public void setStreamUrl(String url) {
		this.streamUrl = url;
	}
	
	public void setThumbnail(Bitmap bmp) {
		if(bmp == null) {
			bmp = BitmapFactory.decodeResource(Song.context.getResources(), R.drawable.default_cover);
		}
		this.thumbnail = bmp;
	}
	

	public long getId() {
		return this.id;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getArtist() {
		return this.artist;
	}
	
	public String getStreamUrl() {
		return this.streamUrl;
	}
	
	public Bitmap getThumbnail() {
		return this.thumbnail;
	}
}
