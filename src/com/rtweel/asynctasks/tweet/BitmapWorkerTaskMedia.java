
package com.rtweel.asynctasks.tweet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class BitmapWorkerTaskMedia extends AsyncTask<String, Void, Bitmap> {
	private final WeakReference<ImageView> mImageViewReference;
	private final Context mContext;
	private final String mCacheName;
	private String mUrl = null;

	public BitmapWorkerTaskMedia(ImageView imageView, Context context,
			String cacheName) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		mImageViewReference = new WeakReference<ImageView>(imageView);
		mContext = context;
		mCacheName = cacheName;
		// imageView.setImageDrawable(null);
		// imageView.setVisibility(View.VISIBLE);
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
			// Log.i("DEBUG", imageView.getTag().toString());
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
				// imageView.setVisibility(View.VISIBLE);
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
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Fake code simulating the copy
			// You can generally do better with nio if you need...
			// And please, unlike me, do something about the Exceptions :D
			byte[] buffer = new byte[1024];
			int len;
			while ((len = stream.read(buffer)) > -1 ) {
			    baos.write(buffer, 0, len);
			}
			baos.flush();

			// Open new InputStreams using the recorded bytes
			// Can be repeated as many times as you wish
			InputStream stream1 = new ByteArrayInputStream(baos.toByteArray()); 
			InputStream stream2 = new ByteArrayInputStream(baos.toByteArray()); 

//			InputStream stream1 = entity.getContent();

//			InputStream stream2 = entity.getContent();

			bitmap = decodeSampledBitmapStream(stream1, stream2, 225, 175);// BitmapFactory.decodeStream(stream);

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

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapStream(InputStream stream,
			InputStream stream2, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(stream2, null, options);
	}

	public String getUrl() {
		return mUrl;
	}

	public String getCacheName() {
		return mCacheName;
	}
}
