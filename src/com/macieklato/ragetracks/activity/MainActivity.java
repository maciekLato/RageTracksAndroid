package com.macieklato.ragetracks.activity;

import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.widget.MyAdapter;
import com.macieklato.ragetracks.widget.OnPullListener;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int PLAY = 0;
	public static final int PAUSE = 1;
	
	OnPullListener menuHider;
	int state = PAUSE;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.activity_main);
        GridView gridView = (GridView)findViewById(R.id.gridview);
        gridView.setOnTouchListener(menuHider);
        gridView.setAdapter(new MyAdapter(this));
    }
    
    private void init() {
    	menuHider = new OnPullListener(this.getApplicationContext()) {
    		@Override
    		public void onPullUp(View v) {	
    			findViewById(R.id.topmenu).setVisibility(View.GONE);
    			findViewById(R.id.nowplaying).setVisibility(View.GONE);
    			findViewById(R.id.bottommenu).setVisibility(View.VISIBLE);
    		}

    		@Override
    		public void onPullDown(View v) {	
    			findViewById(R.id.topmenu).setVisibility(View.VISIBLE);
    			findViewById(R.id.nowplaying).setVisibility(View.VISIBLE);
    			findViewById(R.id.bottommenu).setVisibility(View.GONE);
    		}

    		@Override
    		public void onPullLeft(View v) {}

    		@Override
    		public void onPullRight(View v) {}
    		
    		@Override
    		public void onRelease(View v){
    			findViewById(R.id.topmenu).setVisibility(View.VISIBLE);
    			findViewById(R.id.nowplaying).setVisibility(View.VISIBLE);
    			findViewById(R.id.bottommenu).setVisibility(View.VISIBLE);
    		}
    	};
    }
    
    public void onMenuClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked menu", Toast.LENGTH_SHORT).show();
    	View v2 = findViewById(R.id.leftmenu);
    	LinearLayout v3 = (LinearLayout) findViewById(R.id.main_screen);
		LayoutParams p = (FrameLayout.LayoutParams) v3.getLayoutParams();
    	if(v2.getVisibility() == View.VISIBLE) {
    		v2.setVisibility(View.GONE);
    		p.rightMargin = 0;
    		v3.setLayoutParams(p);
    	} 
    	else {
    		p.rightMargin = -v2.getWidth();
    		v3.setLayoutParams(p);
    		v2.setVisibility(View.VISIBLE);
    	}
    }
    
    public void onSearchClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked next", Toast.LENGTH_SHORT).show();
    }
    
    public void onBookmarkClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked bookmark", Toast.LENGTH_SHORT).show();
    }
    
    public void onPreviousClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked previous", Toast.LENGTH_SHORT).show();
    }
    
    public void onPlayPauseClicked(View v){
    	if(state == PLAY){
    		Toast.makeText(this.getApplicationContext(), "You clicked pause", Toast.LENGTH_SHORT).show();
    		ImageView img = (ImageView) findViewById(R.id.play_pause_button);
    		img.setImageResource(R.drawable.pause);
    		state = PAUSE;
    	} else {
    		Toast.makeText(this.getApplicationContext(), "You clicked play", Toast.LENGTH_SHORT).show();
    		ImageView img = (ImageView) findViewById(R.id.play_pause_button);
    		img.setImageResource(R.drawable.play);
    		state = PLAY;
    	}
    }
    
    public void onNextClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked next", Toast.LENGTH_SHORT).show();
    }
    
    public void onShareClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked share", Toast.LENGTH_SHORT).show();
    }
    
    
    
}
