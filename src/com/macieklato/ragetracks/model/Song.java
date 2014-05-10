package com.macieklato.ragetracks.model;

public class Song {
	public static final int IDLE = 0;
	public static final int PLAYING = 1;
	public static final int PAUSED = 2;
	public static final int LOADING = 3;

	private String title = "";
	private String artist = "";
	private String track = "";
	private String streamUrl = "";
	private String thumbnailUrl = "";
	private String rageTracksUrl = "";
	private String waveformUrl = "";
	private long id = -1;
	private int state = IDLE;

	public Song() {
	}

	public Song(long id, String rageTracksUrl, String title, String artist,
			String streamUrl, String thumbnail, String track) {
		setId(id);
		setRageTracksUrl(rageTracksUrl);
		setTitle(title);
		setArtist(artist);
		setStreamUrl(streamUrl);
		setThumbnailUrl(thumbnail);
		setState(IDLE);
		setTrack(track);
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

	public void setTrack(String track) {
		this.track = track;
	}

	public void setWaveformUrl(String url) {
		this.waveformUrl = url;
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

	public String getTrack() {
		return track;
	}

	public String getWaveformUrl() {
		return this.waveformUrl;
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
			if (this.id == ((Song) obj).id) {
				return true;
			}
			return this.track != null
					&& this.track.equalsIgnoreCase(((Song) obj).track);
		}
		return false;
	}
}
