package com.macieklato.ragetracks.network;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	ImageView im;

	public DownloadImageTask(ImageView v) {
		this.im = v;
	}

	protected Bitmap doInBackground(String... urls) {
		String urldisplay = urls[0];
		Bitmap bmp = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			bmp = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		return bmp;
	}

	protected void onPostExecute(Bitmap bmp) {
		Log.d("download", "image downloaded");
		im.setImageBitmap(bmp);
	}
}
