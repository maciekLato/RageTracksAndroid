package com.macieklato.ragetracks.activity;

import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.widget.MyAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.widget.GridView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GridView gridView = (GridView)findViewById(R.id.gridview);
        gridView.setAdapter(new MyAdapter(this));
    }
    
}
