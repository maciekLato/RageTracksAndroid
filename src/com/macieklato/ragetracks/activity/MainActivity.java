package com.macieklato.ragetracks.activity;

import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.R.id;
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

	//state variables
	public static final int PLAY = 0;
	public static final int PAUSE = 1;
	
	//touch listener for menu hiding
	OnPullListener menuHider;
	
	//current state
	int state = PAUSE;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(); //initialize state
        setContentView(R.layout.activity_main); //set view
        GridView gridView = (GridView)findViewById(R.id.gridview); 
        gridView.setAdapter(new MyAdapter(this)); //add grid view adapter
        gridView.setOnTouchListener(menuHider);
    }
    
    /**
     * initializes any variables for the initial state
     */
    private void init() {
    	//set the pull events for the menu hider
    	menuHider = new OnPullListener(this.getApplicationContext()) {
    		@Override
    		public void onBottomToTop() {	
    			//hide top menus and display bottom menu
    			findViewById(R.id.top_menu).setVisibility(View.GONE);
    			findViewById(R.id.nowplaying).setVisibility(View.GONE);
    			findViewById(R.id.bottommenu).setVisibility(View.VISIBLE);
    		}

    		@Override
    		public void onTopToBottom() {	
    			//hide bottom menu and display top menus
    			findViewById(R.id.top_menu).setVisibility(View.VISIBLE);
    			findViewById(R.id.nowplaying).setVisibility(View.VISIBLE);
    			findViewById(R.id.bottommenu).setVisibility(View.GONE);
    		}

    		@Override
    		public void onLeftToRight() {}

    		@Override
    		public void onRightToLeft() {}
       	};
    }

    
    /**
     * callback for clicking the menu button
     * @param v - the menu button view
     */
    public void onMenuClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked menu", Toast.LENGTH_SHORT).show();
    	View v2 = findViewById(R.id.leftmenu);
    	LinearLayout v3 = (LinearLayout) findViewById(R.id.main_screen);
		LayoutParams p = (FrameLayout.LayoutParams) v3.getLayoutParams();
    	if(v2.getVisibility() == View.VISIBLE) {
    		p.rightMargin = 0;
    		v2.setVisibility(View.GONE);
    	} 
    	else {
    		v2.setVisibility(View.VISIBLE);
    		p.rightMargin = -v2.getWidth();
    		if(p.rightMargin == 0) { //something went wrong (only happens the first time), this is a bit of a hack
    			p.rightMargin = (int) (-200*this.getResources().getDisplayMetrics().xdpi/160f);
    		}
    		//v3.setLayoutParams(p);
    	}
    }
    
    /**
     * callback for clicking the search button
     * @param v - the search button view
     */
    public void onSearchClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked next", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * callback for clicking the bookmark button
     * @param v - the bookmark view
     */
    public void onBookmarkClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked bookmark", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * callback for clicking the previous button
     * @param v - the previous button view
     */
    public void onPreviousClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked previous", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * callback for clicking the play or pause button (same button, different states)
     * @param v - the play/pause view
     */
    public void onPlayPauseClicked(View v){
    	if(state == PLAY){
    		onPauseClicked(v);
    	} else {
    		onPlayClicked(v);
    	}
    }
    
    /**
     * callback for clicking the play button
     * @param v - play view
     */
    public void onPlayClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked play", Toast.LENGTH_SHORT).show();
		ImageView img = (ImageView) findViewById(R.id.play_pause_button);
		img.setImageResource(R.drawable.pause);
		state = PLAY;
    }
    
    /**
     * callback for click the pause button
     * @param v - pause view
     */
    public void onPauseClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked pause", Toast.LENGTH_SHORT).show();
		ImageView img = (ImageView) findViewById(R.id.play_pause_button);
		img.setImageResource(R.drawable.play);
		state = PAUSE;
    }
    
    /**
     * callback for clicking the next button
     * @param v - the next botton view
     */
    public void onNextClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked next", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * callback for clicking the share button
     * @param v - the share button view
     */
    public void onShareClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked share", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * callback for clicking bookmark header in left-side menu
     * @param v - bookmark header view
     */
    public void onBookmarksListClicked(View v){ 
    	//TODO::fix this mock implementation
    	View v2 = findViewById(id.bookmark_list);
    	float ydpi = this.getResources().getDisplayMetrics().ydpi;
    	int collapsedHeight = (int)(2f*ydpi/160f);
    	LinearLayout.LayoutParams p =  (LinearLayout.LayoutParams) v2.getLayoutParams();
    	if(v2.getHeight() > collapsedHeight) {
    		p.height = collapsedHeight;
    		v2.setLayoutParams(p);
    	} 
    	else {
    		p.height = (int)(150*ydpi/160f);
    		v2.setLayoutParams(p);
    	}
    	Toast.makeText(this.getApplicationContext(), "You clicked toggle bookmarks", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * callback for clicking genere header in left-side menu
     * @param v - genere header view
     */
    public void onGeneresListClicked(View v){
    	//TODO::fix this mock implementation
    	View v2 = findViewById(id.genere_list);
    	float ydpi = this.getResources().getDisplayMetrics().ydpi;
    	int collapsedHeight = (int)(2f*ydpi/160f);
    	LinearLayout.LayoutParams p =  (LinearLayout.LayoutParams) v2.getLayoutParams();
    	if(v2.getHeight() > collapsedHeight) {
    		p.height = collapsedHeight;
    		v2.setLayoutParams(p);
    	} 
    	else {
    		p.height = (int)(150*ydpi/160f);
    		v2.setLayoutParams(p);
    	}
    	Toast.makeText(this.getApplicationContext(), "You clicked toggle generes", Toast.LENGTH_SHORT).show();
    }
    
    
    
}
