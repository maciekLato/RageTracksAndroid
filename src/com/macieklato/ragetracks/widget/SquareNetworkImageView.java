package com.macieklato.ragetracks.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public class SquareNetworkImageView extends NetworkImageView {
	public SquareNetworkImageView(Context context) {
		super(context);
	}

	public SquareNetworkImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareNetworkImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = Math.min(getMeasuredWidth(), getMeasuredWidth());
		setMeasuredDimension(size, size); // Snap to min dimension
	}
}