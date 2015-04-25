package com.rtweel.storage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class TweetContentProvider extends ContentProvider {

    private static final int TWEET = 1;
    private static final int HOME = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(TweetDatabase.Tweets.CONTENT_URI_TWEET_DB.getAuthority(), TweetDatabase.Tweets.TABLE_NAME_TWEET, TWEET);
        uriMatcher.addURI(TweetDatabase.Tweets.CONTENT_URI_HOME_DB.getAuthority(), TweetDatabase.Tweets.TABLE_NAME_HOME, HOME);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int choose = uriMatcher.match(uri);
        Cursor result = null;
        switch (choose) {
            case TWEET:
                result = App.getDB().query(TweetDatabase.Tweets.TABLE_NAME_TWEET, projection,
                        selection, selectionArgs, null, null,
                        TweetDatabase.Tweets._ID + sortOrder);

                break;
            case HOME:
                result = App.getDB().query(TweetDatabase.Tweets.TABLE_NAME_HOME, projection,
                        selection, selectionArgs, null, null,
                        TweetDatabase.Tweets._ID + sortOrder);

                break;
        }
        Log.i("DEBUG", "DB select..");
        return result;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int choose = uriMatcher.match(uri);
        switch (choose) {
            case TWEET:
                App.getDB().insert(TweetDatabase.Tweets.TABLE_NAME_TWEET, null, values);
                break;
            case HOME:
                App.getDB().insert(TweetDatabase.Tweets.TABLE_NAME_HOME, null, values);
                break;
        }

        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i("DEBUG", "delete " + uri.toString());

        int choose = uriMatcher.match(uri);
        int result = 0;
        switch (choose) {
            case TWEET:
                result = App.getDB().delete(TweetDatabase.Tweets.TABLE_NAME_TWEET, selection, selectionArgs);
                break;
            case HOME:
                result = App.getDB().delete(TweetDatabase.Tweets.TABLE_NAME_HOME, selection, selectionArgs);
                break;
        }

        Log.i("DEBUG", result + " rows deleted");
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.i("DEBUG", "update " + uri.toString());

        int choose = uriMatcher.match(uri);
        int result = 0;
        switch (choose) {
            case TWEET:
                result = App.getDB().update(TweetDatabase.Tweets.TABLE_NAME_TWEET, values, selection, selectionArgs);
                break;
            case HOME:
                result = App.getDB().update(TweetDatabase.Tweets.TABLE_NAME_HOME, values, selection, selectionArgs);
                break;
        }

        Log.i("DEBUG", "Update result: " + result);
        return result;
    }
}
