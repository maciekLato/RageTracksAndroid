package com.macieklato.ragetracks.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.model.SongController;
import com.macieklato.ragetracks.widget.SquareImageView;
import com.macieklato.ragetracks.widget.SquareNetworkImageView;

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
					20);

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
		final Song song = (Song) getItem(i);

		if (v == null) {
			v = inflater.inflate(R.layout.grid_item, null, false);
			v.setTag(R.id.picture, v.findViewById(R.id.picture));
			v.setTag(R.id.artist, v.findViewById(R.id.artist));
			v.setTag(R.id.title, v.findViewById(R.id.title));
			v.setTag(R.id.overlay, v.findViewById(R.id.overlay));
			v.setTag(R.id.progressBar, v.findViewById(R.id.progressBar));
		}

		SquareNetworkImageView picture = (SquareNetworkImageView) v
				.getTag(R.id.picture);
		SquareImageView overlay = (SquareImageView) v.getTag(R.id.overlay);
		final TextView artist = (TextView) v.getTag(R.id.artist);
		final TextView title = (TextView) v.getTag(R.id.title);
		ProgressBar progressBar = (ProgressBar) v.getTag(R.id.progressBar);

		v.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SongController.getInstance().toggle(song);
			}
		});

		artist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
		artist.setHorizontallyScrolling(true);
		artist.setFocusable(true);
		artist.setFocusableInTouchMode(true);
		artist.setMarqueeRepeatLimit(Animation.INFINITE);

		title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
		title.setHorizontallyScrolling(true);
		title.setFocusable(true);
		title.setFocusableInTouchMode(true);
		title.setMarqueeRepeatLimit(Animation.INFINITE);

		if (song.isIdle()) {
			progressBar.setVisibility(View.GONE);
			overlay.setVisibility(View.GONE);
			artist.setSelected(false);
			title.setSelected(false);
		} else if (song.isPlaying()) {
			progressBar.setVisibility(View.GONE);
			overlay.setVisibility(View.VISIBLE);
			overlay.setImageResource(R.drawable.pause);
			if (!artist.isSelected())
				artist.setSelected(true);
			if (!title.isSelected())
				title.setSelected(true);
		} else if (song.isPaused()) {
			progressBar.setVisibility(View.GONE);
			overlay.setVisibility(View.VISIBLE);
			overlay.setImageResource(R.drawable.play);
		} else if (song.isLoading()) {
			overlay.setImageBitmap(null);
			overlay.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.VISIBLE);
		}

		picture.setImageUrl(song.getThumbnailURL(), mImageLoader);
		picture.setDefaultImageResId(R.drawable.default_cover);
		artist.setText(song.getArtist());
		title.setText(song.getTitle());
		v.setId((int) song.getId());

		return v;
	}

	public void addSong(Song s) {
		items.add(s);
		notifyDataSetChanged();
	}

	public void removeSong(Song s) {
		items.remove(s);
		notifyDataSetChanged();
	}

	public int getMaxSongIndex() {
		Song s = items.get(items.size() - 1);
		return MainActivity.COUNT * (s.getPage() - 1) + s.getIndex();
	}

}