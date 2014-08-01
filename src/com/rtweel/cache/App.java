package com.rtweel.cache;

import java.util.Date;

import com.rtweel.sqlite.TweetDatabaseOpenHelper;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class App extends Application {

	private static DiskCache sDiskCache;
	
	private static SQLiteDatabase sDb;

	@Override
	public void onCreate() {
		super.onCreate();
		Date first = new Date();
		sDb = new TweetDatabaseOpenHelper(this).getWritableDatabase();
		Date second = new Date();
		Log.i("DEBUG", "DB initialization: " + (second.getTime() - first.getTime()));
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
	
	public boolean isOnline() {
	    ConnectivityManager connMgr = (ConnectivityManager) 
	            getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    return (networkInfo != null && networkInfo.isConnected());
	}  

}
