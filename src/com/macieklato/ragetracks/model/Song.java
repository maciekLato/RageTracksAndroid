package com.macieklato.ragetracks.model;


public class Song {
	public static final int IDLE = 0;
	public static final int PLAYING = 1;
	public static final int PAUSED = 2;
	public static final int LOADING = 3;
	
	private String title;
	private String artist;
	private String streamUrl;
	private String thumbnailURL;
	private long id;
	private int state;
	
	public Song() {
		setThumbnailURL(null);
		setState(IDLE);
	}
	
	public Song(long id, String title, String artist, String streamUrl, String thumbnail) {
		setId(id);
		setTitle(title);
		setArtist(artist);
		setStreamUrl(streamUrl);
		setThumbnailURL(thumbnail);
		setState(IDLE);
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
	
	public void setThumbnailURL(String thumbnail) {
		this.thumbnailURL = thumbnail;
	}
	
	public void setState(int state) {
		this.state = state;
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
	
	public String getThumbnailURL() {
		return this.thumbnailURL;
	}
	
	public int getState() {
		return state;
	}
	
	public boolean isIdle() {
		return state == IDLE;
	}
	
	public boolean isPlaying() {
		return state == PLAYING;
	}
	
	public boolean isPaused() {
		return state == PAUSED;
	}
	
	public boolean isLoading() {
		return state == LOADING;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Song) {
			return this.streamUrl.equals(((Song)obj).streamUrl);
		} 
		return false;
	}
}
