package com.rtweel.storage;

import android.app.Application;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rtweel.BuildConfig;

public class App extends Application {
    
    private static SQLiteDatabase sDb;

    @Override
    public void onCreate() {
        super.onCreate();

        createDb();
    }

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
}
