package com.macieklato.ragetracks.activity;

import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.widget.MyAdapter;
import com.macieklato.ragetracks.widget.OnPullListener;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.GridView;

public class MainActivity extends Activity {

	OnPullListener menuHider;
	
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
    		public void onPullLeft(View v) {
    			findViewById(R.id.leftmenu).setVisibility(View.GONE);
    		}

    		@Override
    		public void onPullRight(View v) {
    			findViewById(R.id.leftmenu).setVisibility(View.VISIBLE);
    		}
    		
    		@Override
    		public void onRelease(View v){
    			findViewById(R.id.topmenu).setVisibility(View.VISIBLE);
    			findViewById(R.id.nowplaying).setVisibility(View.VISIBLE);
    			findViewById(R.id.bottommenu).setVisibility(View.VISIBLE);
    		}
    	};
    }
    
}
