package com.macieklato.ragetracks.widget;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ImageSwitcher implements OnTouchListener{
	
	int pressed;
	int notPressed;
	
	public ImageSwitcher(int notPressedResid, int pressedResid) {
		pressed = pressedResid;
		notPressed = notPressedResid;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		if(action == MotionEvent.ACTION_DOWN) {
			v.setBackgroundResource(pressed);
		}
		else if (action == MotionEvent.ACTION_UP) {
			v.setBackgroundResource(notPressed);
		}
		return false;
	}

}
