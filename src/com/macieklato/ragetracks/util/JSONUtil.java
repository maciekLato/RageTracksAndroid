package com.macieklato.ragetracks.util;

import java.util.ArrayList;

import org.json.JSONArray;

import com.macieklato.ragetracks.model.Song;

public class JSONUtil {

	public static ArrayList<Song> parsePosts(JSONArray posts) {
		ArrayList<Song> songs = new ArrayList<Song>();
		for(int i=0; i<posts.length(); i++) {
			songs.add(JSONUtil.parsePost(null));
		}
		return songs;
	}
	
	public static Song parsePost(Object obj) {
		return null;
	}

}
