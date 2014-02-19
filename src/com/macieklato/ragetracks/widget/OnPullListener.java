package com.macieklato.ragetracks.widget;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnPullListener implements OnTouchListener{

	float x; //last x touch position recorded
	float y; //last y touch position recorded
	float epsilonX; //minimum distance to activate pull left/right
	float epsilonY; //minimum distance to activate pull up/down
	
	public OnPullListener(Context c){
		DisplayMetrics d = c.getResources().getDisplayMetrics();
		epsilonX = d.xdpi/4f;
		epsilonY = d.ydpi/4f;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction(); 
			if(action == MotionEvent.ACTION_DOWN) { //on touch, set x,y
				x = event.getX();
				y = event.getY();
			}
			else if(action == MotionEvent.ACTION_MOVE) { //on move calculate change in distance
				float ex = event.getX();
				float ey = event.getY();
				if(Math.abs(x-ex) > epsilonX) { 
					if(ex < x) onPullLeft(v);
					else onPullRight(v);
				}
				else if(Math.abs(y-ey) > epsilonY) {
					if(ey < y) onPullUp(v);
					else onPullDown(v);
				}
			}
			else if(action == MotionEvent.ACTION_UP) {
			onRelease(v);
		}
		
		return false;
	}
	
	public abstract void onPullUp(View v);
	public abstract void onPullDown(View v);
	public abstract void onPullLeft(View v);
	public abstract void onPullRight(View v);
	public abstract void onRelease(View v);

}
