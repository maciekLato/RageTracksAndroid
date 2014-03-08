package com.macieklato.ragetracks.widget;


public class Song {
	private String title;
	private String artist;
	private String streamUrl;
	private String thumbnailUrl;
	private long id;
		
	public Song() { }
	
	public Song(long id, String title, String artist, String streamUrl, String thumbnailUrl) {
		setId(id);
		setTitle(title);
		setArtist(artist);
		setStreamUrl(streamUrl);
		setThumbnailUrl(thumbnailUrl);
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
	
	public void setThumbnailUrl(String url) {
		this.thumbnailUrl = url;
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
	
	public String getThumbnailUrl() {
		return this.thumbnailUrl;
	}
}
