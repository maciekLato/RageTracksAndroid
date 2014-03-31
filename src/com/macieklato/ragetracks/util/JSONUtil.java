package com.macieklato.ragetracks.util;

import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.macieklato.ragetracks.model.Song;

public class JSONUtil {

	public static String DIVIDER = "–";

	/**
	 * parses a JSONArray of posts into an ArrayList of Song data
	 * 
	 * @param posts
	 *            JSONArray
	 * @return ArrayList<Song> list of songs parsed
	 */
	public static ArrayList<Song> parsePosts(JSONArray posts, int page) {
		ArrayList<Song> songs = new ArrayList<Song>();
		for (int i = 0; i < posts.length(); i++) {
			try {
				Song s = JSONUtil.parsePost(posts.getJSONObject(i), page, i);
				if (s != null)
					songs.add(s);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return songs;
	}

	/**
	 * parses a single post into a single song data
	 * 
	 * @param post
	 *            String
	 * @return a Song object represented by the post
	 * @throws JSONException
	 */
	public static Song parsePost(JSONObject post, int page, int index)
			throws JSONException {
		long id = post.getInt("id");
		String rageTracksUrl = StringEscapeUtils.unescapeHtml4(post.getString("url"));
		String temp = StringEscapeUtils.unescapeHtml4(post.getString("title"));
		String title = parseTitle(temp);
		String artist = parseArtist(temp);
		String url = parseContent(post.getString("content"));
		String thumbnail = parseAttachments(post.getJSONArray("attachments"));
		Log.d("post", String.format(
				"title:%s\nartist:%s\nurl:%s\nthumbnail:%s\n", title, artist,
				url, thumbnail));
		if (rageTracksUrl == null || url == null || thumbnail == null)
			return null;
		return new Song(id, rageTracksUrl, title, artist, url, thumbnail, page, index);
	}
	
	/**
	 * parses the artist out of a string
	 * 
	 * @param str
	 * @return String that indicates the artist
	 */
	public static String parseArtist(String str) {
		if (str == null)
			return "";
		int end = str.lastIndexOf(JSONUtil.DIVIDER);
		if (end < 0)
			return "";
		return str.substring(0, end).trim();
	}

	/**
	 * parses the song title out of a string
	 * 
	 * @param str
	 * @return String that indicates the song title
	 */
	public static String parseTitle(String str) {
		if (str == null)
			return "";
		int start = str.lastIndexOf(JSONUtil.DIVIDER) + 1;
		if (start <= 0 || start > str.length())
			return str;
		return str.substring(start).trim();
	}

	/**
	 * parses the stream url out of a content string
	 * 
	 * @param content
	 * @return String representing the streaming url
	 */
	public static String parseContent(String content) {
		if (content == null)
			return null;
		Log.d("parse", "content:" + content);
		int start = content.indexOf("tracks/") + 7;
		int end = content.indexOf("&", start);
		if (end < 0 || start < 7 || end < start)
			return null;
		String track = content.substring(start, end);
		return String.format(
				"http://api.soundcloud.com/tracks/%s/stream?client_id=%s",
				track, Network.CLIENT_ID);
	}

	/**
	 * parses the thumbnail image url from the attachments string
	 * 
	 * @param attachments
	 * @return String representing the thumbnail url for the song
	 * @throws JSONException
	 *             if attachments->images->medium->url is not in the json array
	 */
	public static String parseAttachments(JSONArray attachments) {
		try {
			if (attachments == null)
				return null;
			return attachments.getJSONObject(0).getJSONObject("images")
					.getJSONObject("thumbnail").getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

}
