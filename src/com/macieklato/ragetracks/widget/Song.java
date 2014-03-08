package com.macieklato.ragetracks.widget;


public class Song {
	private String title;
	private String artist;
	private String streamUrl;
	private String thumbnailURL;
	private long id;
	
	public Song() {
		setThumbnailURL(null);
	}
	
	public Song(long id, String title, String artist, String streamUrl, String thumbnail) {
		setId(id);
		setTitle(title);
		setArtist(artist);
		setStreamUrl(streamUrl);
		setThumbnailURL(thumbnail);
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
}
