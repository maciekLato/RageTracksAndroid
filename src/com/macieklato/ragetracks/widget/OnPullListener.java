package com.macieklato.ragetracks.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnPullListener implements OnTouchListener{
	
	float x;
	float y;
	float epsilonX;
	float epsilonY;
	
	public OnPullListener(Context c){
		epsilonX = c.getResources().getDisplayMetrics().xdpi/4f;
		epsilonY = c.getResources().getDisplayMetrics().ydpi/4f;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		int action = e.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			x = e.getRawX();
			y = e.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			checkPull(e.getRawX(), e.getRawY());
			break;
		case MotionEvent.ACTION_UP:
			checkPull(e.getRawX(), e.getRawY());
			break;
		}
		return false;
	}
	
	public void checkPull(float ex, float ey){
			checkHorizontal(ex);
			checkVertical(ey);
	}
	
	public void checkHorizontal(float x2) {
		if(Math.abs(x-x2) > epsilonX) {
			if(x2 > x) onLeftToRight();
			else onRightToLeft();
			x = x2;
		}
	}
	
	public void checkVertical(float y2) {
		if(Math.abs(y-y2) > epsilonY) {
			if(y2 > y) onTopToBottom();
			else onBottomToTop();
			y = y2;
		}
	}
	
	public abstract void onTopToBottom();
	public abstract void onBottomToTop();
	public abstract void onLeftToRight();
	public abstract void onRightToLeft();

}
