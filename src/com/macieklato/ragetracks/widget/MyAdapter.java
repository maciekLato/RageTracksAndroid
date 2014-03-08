package com.macieklato.ragetracks.widget;

import java.util.ArrayList;
import java.util.List;

import com.macieklato.ragetracks.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
    private List<Song> items = new ArrayList<Song>();
    private LayoutInflater inflater;

    public MyAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return items.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        Song song = (Song)getItem(i);

        if(v == null) {
            v = inflater.inflate(R.layout.grid_item, null, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
            v.setOnClickListener(new SongController(song));
        }
        
        ImageView picture = (ImageView)v.getTag(R.id.picture);
        TextView name = (TextView)v.getTag(R.id.text);

        picture.setImageBitmap(song.getThumbnail());
        name.setText(song.getTitle());
        
        return v;
    }
    
    public void addSong(Song s) {
    	items.add(s);
    }

}