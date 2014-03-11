package com.macieklato.ragetracks.controller;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnPullListener implements OnTouchListener{
	
	private float x;
	private float y;
	private float epsilonX;
	private float epsilonY;
	
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
			return checkPull(e.getRawX(), e.getRawY());
		case MotionEvent.ACTION_UP:
			return checkPull(e.getRawX(), e.getRawY());
		}
		return false;
	}
	
	public boolean checkPull(float ex, float ey){
		return checkHorizontal(ex) || checkVertical(ey);
	}
	
	public boolean checkHorizontal(float x2) {
		if(Math.abs(x-x2) > epsilonX) {
			if(x2 > x) onLeftToRight();
			else onRightToLeft();
			x = x2;
			return true;
		}
		return false;
	}
	
	public boolean checkVertical(float y2) {
		if(Math.abs(y-y2) > epsilonY) {
			if(y2 > y) onTopToBottom();
			else onBottomToTop();
			y = y2;
			return true;
		}
		return false;
	}
	
	public abstract void onTopToBottom();
	public abstract void onBottomToTop();
	public abstract void onLeftToRight();
	public abstract void onRightToLeft();

}
