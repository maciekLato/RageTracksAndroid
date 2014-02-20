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
	
	//current state
	int state = PAUSE;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //set view
        setListeners(); //initialize state
        GridView gridView = (GridView)findViewById(R.id.gridview); 
        gridView.setAdapter(new MyAdapter(this)); //add grid view adapter
    }
    
    /**
     * initializes any variables for the initial state
     */
    private void setListeners() {
    	//set the pull events for the menu hider
    	OnPullListener gridListener = new OnPullListener(this.getApplicationContext()) {
    		@Override
    		public void onBottomToTop() {	
    			findViewById(R.id.top_menu).setVisibility(View.GONE);
    			//findViewById(R.id.nowplaying).setVisibility(View.GONE);
    			findViewById(R.id.bottommenu).setVisibility(View.GONE);
    		}

    		@Override
    		public void onTopToBottom() {	
    			findViewById(R.id.top_menu).setVisibility(View.VISIBLE);
    			//findViewById(R.id.nowplaying).setVisibility(View.VISIBLE);
    			findViewById(R.id.bottommenu).setVisibility(View.VISIBLE);
    		}

    		@Override
    		public void onLeftToRight() {
    			showMenu();
    		}
    		@Override
    		public void onRightToLeft() {
    			hideMenu();
    		}
       	};
       	findViewById(R.id.gridview).setOnTouchListener(gridListener);
    }
    
    /**
     * hides the left side menu and adjusts the main screen
     */
    private void hideMenu(){
    	
    	//hide side menu
    	View menu = findViewById(R.id.leftmenu);
    	if(menu.getVisibility() == View.GONE) return;
    	menu.setVisibility(View.GONE);
    	
    	//move main screen to the left
    	View mainScreen = findViewById(R.id.main_screen);
    	LayoutParams p = (FrameLayout.LayoutParams) mainScreen.getLayoutParams();
    	p.rightMargin = 0;
    }
    
    /**
     * makes the left side menu visible and adjusts the main screen
     */
    private void showMenu(){
    	//show side menu
    	View menu = findViewById(R.id.leftmenu);
    	if(menu.getVisibility() == View.VISIBLE) return;
    	menu.setVisibility(View.VISIBLE);
    	
    	//move main screen to the right
    	View mainScreen = findViewById(R.id.main_screen);
    	LayoutParams p = (FrameLayout.LayoutParams) mainScreen.getLayoutParams();
    	p.rightMargin = -menu.getWidth();
    	if(p.rightMargin == 0) { //something went wrong (only happens the first time), this is a bit of a hack
			p.rightMargin = (int) (-200*this.getResources().getDisplayMetrics().xdpi/160f);
		}
    }

    
    /**
     * callback for clicking the menu button
     * @param v - the menu button view
     */
    public void onMenuClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked menu", Toast.LENGTH_SHORT).show();
    	View v2 = findViewById(R.id.leftmenu);
    	if(v2.getVisibility() == View.VISIBLE) {
    		hideMenu();
    	} 
    	else {
    		showMenu();
    	}
    }
    
    /**
     * callback for clicking the search button
     * @param v - the search button view
     */
    public void onSearchClicked(View v){
    	Toast.makeText(this.getApplicationContext(), "You clicked search", Toast.LENGTH_SHORT).show();
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
