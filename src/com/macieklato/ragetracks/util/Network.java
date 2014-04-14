package com.macieklato.ragetracks.util;

public class Network {
	
	// host
	public static final String HOST = "http://ragetracks.com/";
	public static final String SOUND_CLOUD_TRACKS = "http://api.soundcloud.com/tracks.json";

	// parts
	public static final String JSON = "json";
	public static final String COUNT = "count";
	public static final String INCLUDE = "include";
	public static final String PAGE = "page";
	public static final String CUSTOM_FIELDS = "custom_fields";
	public static final String CLIENT_ID_ARG = "client_id";
	public static final String TRACK_IDS = "ids";

	// values
	public static final String FIELD_POSTTHUMB = "PostThumb";
	public static final String INCLUDE_TITLE = "title";
	public static final String INCLUDE_CONTENT = "content";
	public static final String INCLUDE_ATTACHMENTS = "attachments";
	public static final String INCLUDE_URL = "url";
	public static final String INCLUDE_ALL = INCLUDE_TITLE + ","
			+ INCLUDE_CONTENT + "," + INCLUDE_ATTACHMENTS + "," + INCLUDE_URL;

	// return values
	public static final String POSTS = "posts";

	// authorization
	public static final String CLIENT_ID = "7622aa84a50c9f7609e2f7ed8bc85e81";
}
