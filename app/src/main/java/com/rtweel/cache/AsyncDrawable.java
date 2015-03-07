package com.rtweel.cache;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

public class AsyncDrawable extends BitmapDrawable {
	private final WeakReference<AsyncTask> bitmapWorkerTaskReference;

	public AsyncDrawable(Resources res, Bitmap bitmap,
			AsyncTask bitmapWorkerTask) {
		super(res, bitmap);
		bitmapWorkerTaskReference = new WeakReference<AsyncTask>(
				bitmapWorkerTask);
	}

	public AsyncTask getBitmapWorkerTask() {
		return bitmapWorkerTaskReference.get();
	}
}
