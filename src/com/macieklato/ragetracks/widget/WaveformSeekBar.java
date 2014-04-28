package com.macieklato.ragetracks.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class WaveformSeekBar extends RelativeLayout {

	private View background;
	private NetworkImageView waveform;
	private int progress = 0;
	private int max = 1000;
	private OnSeekBarChangeListener listener;

	public WaveformSeekBar(Context context) {
		super(context);
		init(context);
	}

	public WaveformSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WaveformSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context c) {
		background = new View(c);
		background.setLayoutParams(new LayoutParams(1,
				LayoutParams.MATCH_PARENT));
		setBackgroundColor(Color.RED);
		this.addView(background);

		waveform = new NetworkImageView(c);
		waveform.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		waveform.setScaleType(ScaleType.FIT_XY);
		waveform.setAdjustViewBounds(false);
		this.addView(waveform);
	}

	public void setWaveformUrl(String url, ImageLoader imageLoader) {
		waveform.setImageUrl(url, imageLoader);
	}

	public void setDefaultImageResId(int defaultImage) {
		waveform.setDefaultImageResId(defaultImage);
	}

	public void setBackgroundColor(int color) {
		background.setBackgroundColor(color);
	}

	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (listener != null)
				listener.onStartTrackingTouch(null);
			move(e);
			return true;
		case MotionEvent.ACTION_MOVE:
			move(e);
			break;
		case MotionEvent.ACTION_UP:
			if (listener != null)
				listener.onStopTrackingTouch(null);
			move(e);
			break;
		}
		return false;
	}

	private void move(MotionEvent e) {
		float x = e.getX();
		float progress = x / this.getWidth();
		if (progress < 0 || progress > 1)
			return;
		setProgress((int) (progress * max), true);
	}

	public int getProgress() {
		return progress;
	}

	public int getMax() {
		return max;
	}

	public void setProgress(int progress) {
		setProgress(progress, false);
	}

	private void setProgress(int progress, boolean byUser) {
		this.progress = progress;
		LayoutParams p = (LayoutParams) background.getLayoutParams();
		p.width = (int) ((float) progress / max * this.getWidth());
		background.setLayoutParams(p);
		if (listener != null)
			listener.onProgressChanged(null, progress, byUser);
	}

	public void setMax(int max) {
		this.max = max;
		setProgress(progress);
	}

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
		this.listener = listener;
	}
}
