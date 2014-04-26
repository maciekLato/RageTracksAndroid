package com.macieklato.ragetracks.listener;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnPullListener implements OnTouchListener {

	private float x;
	private float y;
	private float epsilonX;
	private float epsilonY;

	public OnPullListener(Context c) {
		epsilonX = c.getResources().getDisplayMetrics().xdpi / 2f;
		epsilonY = c.getResources().getDisplayMetrics().ydpi / 2f;
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		int action = e.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			x = e.getRawX();
			y = e.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			return checkPull(e.getRawX(), e.getRawY());
		case MotionEvent.ACTION_UP:
			return checkPull(e.getRawX(), e.getRawY());
		}
		return false;
	}

	public boolean checkPull(float ex, float ey) {
		if (!checkHorizontal(ex, ey)) {
			return checkVertical(ex, ey);
		}
		return true;
	}

	public boolean checkHorizontal(float x2, float y2) {
		if (Math.abs(x - x2) > epsilonX) {
			if (x2 > x)
				onLeftToRight(x2, y2);
			else
				onRightToLeft(x2, y2);
			x = x2;
			return true;
		}
		return false;
	}

	public boolean checkVertical(float x2, float y2) {
		if (Math.abs(y - y2) > epsilonY) {
			if (y2 > y)
				onTopToBottom(x2, y2);
			else
				onBottomToTop(x2, y2);
			y = y2;
			return true;
		}
		return false;
	}

	public abstract void onTopToBottom(float x, float y);

	public abstract void onBottomToTop(float x, float y);

	public abstract void onLeftToRight(float x, float y);

	public abstract void onRightToLeft(float x, float y);

}
