package com.macieklato.ragetracks.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class Network {

	//host
	public static final String HOST = "http://ragetracks.com/";

	//parts
	public static final String JSON = "json";
	public static final String COUNT = "count";
	public static final String INCLUDE = "include";
	public static final String PAGE = "page";
	public static final String CUSTOM_FIELDS = "custom_fields";

	//values
	public static final String FIELD_POSTTHUMB = "PostThumb";
	public static final String INCLUDE_TITLE = "title";
	public static final String INCLUDE_CONTENT = "content";
	public static final String INCLUDE_ATTACHMENTS = "attachments";
	public static final String INCLUDE_ALL = INCLUDE_TITLE + "," + INCLUDE_CONTENT + "," + INCLUDE_ATTACHMENTS;
	
	//return values
	public static final String POSTS = "posts";

	//authorization
	public static final String CLIENT_ID = "7622aa84a50c9f7609e2f7ed8bc85e81";
	
	/**
	 * Makes a request to the server
	 * @param url - the path to a php endpoint
	 * @param data - the data to post
	 * @return - the HttpResponse
	 */
	public static HttpResponse get(String url, List<NameValuePair> data) {
		

	    if(!url.endsWith("?")) url += "?";
	    url += URLEncodedUtils.format(data, "utf-8");
	    
	    HttpClient httpClient = new DefaultHttpClient();
	    HttpContext localContext = new BasicHttpContext();
	    HttpGet httpGet = new HttpGet(url);
	    
	    try {
		    Log.d("get", url);
	        return httpClient.execute(httpGet, localContext);	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        Log.e("get", "error");
	    }
	    
	    return null;
	}
	
	/**
	 * loads count number of song data
	 * @param count - number of songs to load meta-data about
	 * @return JSONArray containing the song meta-data
	 */
	public static JSONArray load(int count, int page) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair(JSON, "1"));
		data.add(new BasicNameValuePair(COUNT, ""+count));
		data.add(new BasicNameValuePair(INCLUDE, INCLUDE_ALL));
		data.add(new BasicNameValuePair(PAGE, ""+page));
		
		HttpResponse resp = get(HOST, data);
		try {
	        String str = EntityUtils.toString(resp.getEntity());
			Log.d("get", str);
			JSONObject obj = new JSONObject(str);
			return obj.getJSONArray(POSTS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
