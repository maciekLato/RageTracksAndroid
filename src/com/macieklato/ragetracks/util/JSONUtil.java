package com.macieklato.ragetracks.util;

import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.macieklato.ragetracks.model.Song;

public class JSONUtil {
	
	public static int DIVIDER = 8211; 

	/**
	 * parses a JSONArray of posts into an ArrayList of Song data
	 * @param posts JSONArray
	 * @return ArrayList<Song> list of songs parsed
	 */
	public static ArrayList<Song> parsePosts(JSONArray posts) {
		ArrayList<Song> songs = new ArrayList<Song>();
		for(int i=0; i<posts.length(); i++) {
			try {
				songs.add(JSONUtil.parsePost(posts.getJSONObject(i)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return songs;
	}
	
	/**
	 * parses a single post into a single song data
	 * @param post String
	 * @return a Song object represented by the post
	 * @throws JSONException
	 */
	public static Song parsePost(JSONObject post) throws JSONException {		
		long id = post.getInt("id");
		String temp = StringEscapeUtils.unescapeHtml4(post.getString("title"));
		String title = parseTitle(temp);
		String artist = parseArtist(temp);
		String url = parseContent(post.getString("content"));
		String thumbnail = parseAttachments(post.getJSONArray("attachments"));
		Log.d("post", String.format("title:%s\nartist:%s\nurl:%s\nthumbnail:%s\n", 
				title, artist, url, thumbnail));
		return new Song(id, title, artist, url, thumbnail);
	}

    
    /**
     * parses the song title out of a string
     * @param str
     * @return String that indicates the song title
     */
    public static String parseTitle(String str) {
    	int end = str.lastIndexOf(JSONUtil.DIVIDER)-1;
    	if(end < 0) return str;
    	return str.substring(0, end);
    }
    
    /**
     * parses the artist out of a string
     * @param str
     * @return String that indicates the artist
     */
    public static String parseArtist(String str) {
    	int start = str.lastIndexOf(JSONUtil.DIVIDER)+1;
    	if(start <= 0 || start > str.length()) return "";
    	return str.substring(start);
    }
    
    /**
     * parses the stream url out of a content string
     * @param content
     * @return String representing the streaming url
     */
    public static String parseContent(String content) {
    	int start = content.indexOf("tracks/") + 7;
    	int end = content.indexOf("&", start);
    	String track = content.substring(start, end);
    	return String.format("http://api.soundcloud.com/tracks/%s/stream?client_id=%s", track, Network.CLIENT_ID);
    }
    
    /**
     * parses the thumbnail image url from the attachments string
     * @param attachments
     * @return String representing the thumbnail url for the song
     * @throws JSONException if attachments->images->medium->url is not in the json array
     */
    public static String parseAttachments(JSONArray attachments) throws JSONException {
    	return attachments.getJSONObject(0)
    			.getJSONObject("images")
    			.getJSONObject("thumbnail")
    			.getString("url");
    }

}
