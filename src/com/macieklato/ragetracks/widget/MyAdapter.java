package com.macieklato.ragetracks.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.macieklato.ragetracks.R;

public class MyAdapter extends BaseAdapter {
	private RequestQueue queue;
	private ImageLoader mImageLoader;

	private List<Song> items = new ArrayList<Song>();
	private LayoutInflater inflater;

	public MyAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		queue = Volley.newRequestQueue(context);
		mImageLoader = new ImageLoader(queue, new ImageLoader.ImageCache() {
			private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(
					10);

			public void putBitmap(String url, Bitmap bitmap) {
				mCache.put(url, bitmap);
			}

			public Bitmap getBitmap(String url) {
				return mCache.get(url);
			}
		});
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int i) {
		return items.get(i);
	}

	@Override
	public long getItemId(int i) {
		return items.get(i).getId();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		View v = view;
		Song song = (Song) getItem(i);

		if (v == null) {
			v = inflater.inflate(R.layout.grid_item, null, false);
			v.setTag(R.id.picture, v.findViewById(R.id.picture));
			v.setTag(R.id.text, v.findViewById(R.id.text));
			v.setOnClickListener(new SongController(song));
		}

		SquareImageView picture = (SquareImageView) v.getTag(R.id.picture);
		TextView name = (TextView) v.getTag(R.id.text);

		picture.setImageUrl(song.getThumbnailURL(), mImageLoader);
		picture.setDefaultImageResId(R.drawable.default_cover);
		name.setText(song.getTitle());

		return v;
	}

	public void addSong(Song s) {
		items.add(s);
		notifyDataSetChanged();
	}

}