
package com.rtweel.asynctasks.tweet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.rtweel.R;
import com.rtweel.cache.App;
import com.rtweel.cache.DiskCache;

public class BitmapWorkerTaskProfile extends AsyncTask<String, Void, Bitmap> {
	private final WeakReference<ImageView> mImageViewReference;
	private final Context mContext;
	private final String mCacheName;
	private String mUrl = null;

	public BitmapWorkerTaskProfile(ImageView imageView, Context context,
			String cacheName) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		mImageViewReference = new WeakReference<ImageView>(imageView);
		mContext = context;
		mCacheName = cacheName;
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(String... params) {
		App app = (App) mContext;
		DiskCache cache = app.getDiskCache();
		// Bitmap bitmap = MemoryCache.getBitmap(mCacheName);

		// if (bitmap == null) {
		Bitmap bitmap = cache.getBitmap(mCacheName);
		// }

		if (bitmap == null) {
			if (!app.isOnline()) {
				Log.i("DEBUG", "picture task tweet adapter NO NETWORK");
				Options opts = new Options();
				opts.outHeight = 24;
				opts.outWidth = 24;
				opts.inScaled = true;
				// TODO: valid picture size

				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.ic_launcher, opts);
			} else {
				try {
					bitmap = downloadBitmap(params[0]);
					cache.put(mCacheName, bitmap);
					// MemoryCache.addBitmap(mCacheName, bitmap);
				} catch (NullPointerException e) {
					e.printStackTrace();
					Options opts = new Options();
					opts.outHeight = 24;
					opts.outWidth = 24;
					opts.inScaled = true;

					bitmap = BitmapFactory.decodeResource(
							mContext.getResources(), R.drawable.ic_launcher,
							opts);
				}
			}
		}

		return bitmap;
	}

	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (mImageViewReference != null && bitmap != null) {
			final ImageView imageView = mImageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	private Bitmap downloadBitmap(String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		Bitmap bitmap = null;
		
		try {
			HttpResponse response = client.execute(request);

			HttpEntity entity = response.getEntity();
			
			InputStream stream = entity.getContent();
			
		

			bitmap = BitmapFactory.decodeStream(stream);

			entity.consumeContent();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	public String getUrl() {
		return mUrl;
	}

	public String getCacheName() {
		return mCacheName;
	}
}
