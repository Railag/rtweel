package com.rtweel.cache;

import android.app.Application;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rtweel.BuildConfig;
import com.rtweel.sqlite.TweetDatabase;

public class App extends Application {


    public static final String BUILD = BuildConfig.APPLICATION_ID;
    public static final String PHOTO_PATH = "/Android/data/" + BUILD + "/photo";

//	private static DiskCache sDiskCache;

    private static SQLiteDatabase sDb;

    private static Bitmap sBitmap;

    @Override
    public void onCreate() {
        super.onCreate();

        createDb();

//      new MemoryCache();
//		sDiskCache = new DiskCache(getApplicationContext(), "thumbnails",
//				10 * 1024 * 1024, CompressFormat.JPEG, 100);
//		Options opts = new Options();
//		opts.inSampleSize = 4;
//		setBitmap(BitmapFactory.decodeResource(getResources(),
//				com.rtweel.R.drawable.rtweel, opts));
    }

//	public static DiskCache getDiskCache() {
//		return sDiskCache;
//	}

    public static SQLiteDatabase getDB() {
        return sDb;
    }

    public void createDb() {
        TweetDatabase dbHelper = new TweetDatabase(getApplicationContext());
        sDb = dbHelper.getWritableDatabase();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean existsInDb(long _id, String tableName) {
        return DatabaseUtils.longForQuery(getDB(), "select count(*) from " + tableName + " where _ID=? limit 1", new String[]{String.valueOf(_id)}) > 0;
    }

//	public static Bitmap getBitmap() {
//		return sBitmap;
//	}

//	public static void setBitmap(Bitmap sBitmap) {
//		App.sBitmap = sBitmap;
//	}

}
