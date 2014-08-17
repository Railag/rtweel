package com.rtweel.cache;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rtweel.sqlite.TweetDatabaseOpenHelper;

public class App extends Application {

	public static final String PATH = "/Android/data/com.rtweel/sp";
	public static final String PHOTO_PATH = "/Android/data/com.rtweel/photo";
	
	private static DiskCache sDiskCache;

	private static SQLiteDatabase sDb;

	private static TweetDatabaseOpenHelper sHelper;
	
	@Override
	public void onCreate() {
		super.onCreate();
		// Date first = new Date();
		sHelper = new TweetDatabaseOpenHelper(this);
		sDb = sHelper.getWritableDatabase();
		// Date second = new Date();
		// Log.i("DEBUG", "DB initialization: " + (second.getTime() -
		// first.getTime()));
		new MemoryCache();
		sDiskCache = new DiskCache(getApplicationContext(), "thumbnails",
				10 * 1024 * 1024, CompressFormat.JPEG, 100);
	}

	public DiskCache getDiskCache() {
		return sDiskCache;
	}

	public SQLiteDatabase getDB() {
		return sDb;
	}
	
	public void createDb() {
		sHelper = new TweetDatabaseOpenHelper(this);
		sDb = sHelper.getWritableDatabase();
	}

	public boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

}
