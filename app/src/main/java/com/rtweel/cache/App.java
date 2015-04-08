package com.rtweel.cache;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rtweel.BuildConfig;
import com.rtweel.sqlite.TweetDatabase;

public class App extends Application {


    public static final String BUILD = BuildConfig.APPLICATION_ID;
	public static final String PATH = "/Android/data/" + BUILD +"/sp";
	public static final String PHOTO_PATH = "/Android/data/" + BUILD + "/photo";

	private static DiskCache sDiskCache;

	private static SQLiteDatabase sDb;

	private static TweetDatabase sHelper;

	private static Bitmap sBitmap;

	@Override
	public void onCreate() {
		super.onCreate();
		// Date first = new Date();
		sHelper = new TweetDatabase(this);
		sDb = sHelper.getWritableDatabase();
		// Date second = new Date();
		// Log.i("DEBUG", "DB initialization: " + (second.getTime() -
		// first.getTime()));
		new MemoryCache();
		sDiskCache = new DiskCache(getApplicationContext(), "thumbnails",
				10 * 1024 * 1024, CompressFormat.JPEG, 100);
		Options opts = new Options();
		opts.inSampleSize = 4;
		setBitmap(BitmapFactory.decodeResource(getResources(),
				com.rtweel.R.drawable.rtweel, opts));
	}

	public DiskCache getDiskCache() {
		return sDiskCache;
	}

	public SQLiteDatabase getDB() {
		return sDb;
	}

	public void createDb() {
		sHelper = new TweetDatabase(this);
		sDb = sHelper.getWritableDatabase();
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	public static Bitmap getBitmap() {
		return sBitmap;
	}

	public static void setBitmap(Bitmap sBitmap) {
		App.sBitmap = sBitmap;
	}

}
