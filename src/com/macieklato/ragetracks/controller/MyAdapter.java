package com.macieklato.ragetracks.controller;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.model.Song;
import com.macieklato.ragetracks.service.StreamingBackgroundService;
import com.macieklato.ragetracks.widget.SquareImageView;
import com.macieklato.ragetracks.widget.SquareNetworkImageView;

public class MyAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	public MyAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return SongController.getInstance().getNumSongs();
	}

	@Override
	public Object getItem(int i) {
		return SongController.getInstance().getSongAt(i);
	}

	@Override
	public long getItemId(int i) {
		Song song = SongController.getInstance().getSongAt(i);
		if (song != null)
			return song.getId();
		return -1;
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
				switch (song.getState()) {
				case Song.LOADING:
					break;
				case Song.IDLE:
					ApplicationController.getInstance().sendCommand(
							StreamingBackgroundService.ACTION_LOAD,
							song.getId());
					break;
				default:
					ApplicationController.getInstance().sendCommand(
							StreamingBackgroundService.ACTION_TOGGLE_PLAYBACK);
				}
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

		picture.setImageUrl(song.getThumbnailUrl(), ApplicationController
				.getInstance().getImageLoader());
		picture.setDefaultImageResId(R.drawable.default_cover);
		artist.setText(song.getArtist());
		title.setText(song.getTitle());
		v.setId((int) song.getId());

		return v;
	}
}