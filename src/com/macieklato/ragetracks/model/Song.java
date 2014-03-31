package com.macieklato.ragetracks.model;

public class Song {
	public static final int IDLE = 0;
	public static final int PLAYING = 1;
	public static final int PAUSED = 2;
	public static final int LOADING = 3;

	private String title;
	private String artist;
	private String streamUrl;
	private String thumbnailUrl;
	private String rageTracksUrl;
	private int page;
	private int index;
	private long id;
	private int state;

	public Song() {
		setThumbnailUrl(null);
		setState(IDLE);
	}

	public Song(long id, String rageTracksUrl, String title, String artist, String streamUrl,
			String thumbnail, int page, int index) {
		setId(id);
		setRageTracksUrl(rageTracksUrl);
		setTitle(title);
		setArtist(artist);
		setStreamUrl(streamUrl);
		setThumbnailUrl(thumbnail);
		setState(IDLE);
		setPage(page);
		setIndex(index);
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public void setRageTracksUrl(String url) {
		this.rageTracksUrl = url;
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

	public void setThumbnailUrl(String thumbnail) {
		this.thumbnailUrl = thumbnail;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getId() {
		return this.id;
	}
	
	public String getRageTracksUrl() {
		return this.rageTracksUrl;
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

	public String getThumbnailUrl() {
		return this.thumbnailUrl;
	}

	public int getState() {
		return state;
	}

	public int getPage() {
		return page;
	}

	public int getIndex() {
		return index;
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
		if (obj instanceof Song) {
			return this.streamUrl.equals(((Song) obj).streamUrl);
		}
		return false;
	}
}
