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
		epsilonX = c.getResources().getDisplayMetrics().xdpi/4;
		epsilonY = c.getResources().getDisplayMetrics().ydpi/4;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		int action = e.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			x = e.getX();
			y = e.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			checkHorizontal(e.getX());
			checkVertical(e.getY());
			break;
		case MotionEvent.ACTION_UP:
			checkHorizontal(e.getX());
			checkVertical(e.getY());
			break;
		}
		
		return false;
	}
	
	public void checkHorizontal(float x2) {
		if(Math.abs(x-x2) > epsilonX) {
			if(x2 > x) onLeftToRight();
			else onRightToLeft();
		}
	}
	
	public void checkVertical(float y2) {
		if(Math.abs(y-y2) > epsilonY) {
			if(y2 > y) onTopToBottom();
			else onBottomToTop();
		}
	}
	
	public abstract void onTopToBottom();
	public abstract void onBottomToTop();
	public abstract void onLeftToRight();
	public abstract void onRightToLeft();

}
