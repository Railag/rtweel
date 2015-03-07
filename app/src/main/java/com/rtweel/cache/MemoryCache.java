package com.rtweel.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public final class MemoryCache {
	private static LruCache<String, Bitmap> sBitmapCache;

	public MemoryCache() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		final int cacheSize = maxMemory / 8;

		sBitmapCache = new LruCache<String, Bitmap>(cacheSize);
	}

	public static void addBitmap(String key, Bitmap bitmap) {
		if (getBitmap(key) == null) {
			sBitmapCache.put(key, bitmap);
		}
	}

	public static Bitmap getBitmap(String key) {
		return sBitmapCache.get(key);
	}

}
