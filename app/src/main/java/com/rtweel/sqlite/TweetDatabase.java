package com.rtweel.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class TweetDatabase extends SQLiteOpenHelper {

    public static final class Tweets implements BaseColumns {
        public static final Uri CONTENT_URI_TWEET_DB = Uri
                .parse("content://com.rtweel.sqlite.TweetContentProvider/all_timeline");
        public static final Uri CONTENT_URI_USER_DB = Uri
                .parse("content://com.rtweel.sqlite.TweetContentProvider/user_timeline");

        public static final String TABLE_NAME_TWEET = "all_timeline";
        public static final String TABLE_NAME_USER = "user_timeline";

        //for user timeline
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_PICTURE = "picture_url";
        public static final String COLUMN_DATE = "created_at";
        public static final String COLUMN_MEDIA = "media";

        //for other tweets
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_IS_RETWEET = "is_retweet";
        public static final String COLUMN_IS_FAVORITE = "is_favorite";
        public static final String COLUMN_MENTIONS = "mentions";

    }

    public final static String SELECTION_ASC = " ASC ";
    public final static String SELECTION_DESC = " DESC ";

    private static final String DB_NAME = "tweets.db";
    private static final int VERSION = 1;

    private static final String CREATE_TABLE_TWEET = new StringBuilder()
            .append("CREATE TABLE ")
            .append(Tweets.TABLE_NAME_TWEET)
            .append("(").append(Tweets._ID)
            .append(" INTEGER PRIMARY KEY, ")
            .append(Tweets.COLUMN_AUTHOR)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_TEXT)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_PICTURE)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_DATE)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_MEDIA)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_MENTIONS)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_IS_FAVORITE)
            .append(" INTEGER, ")
            .append(Tweets.COLUMN_IS_RETWEET)
            .append(" INTEGER, ")
            .append(Tweets.COLUMN_USER_ID)
            .append(" INTEGER)")
            .toString();

    private static final String CREATE_TABLE_USER = new StringBuilder()
            .append("CREATE TABLE ")
            .append(Tweets.TABLE_NAME_USER)
            .append("(").append(Tweets._ID)
            .append(" INTEGER PRIMARY KEY, ")
            .append(Tweets.COLUMN_AUTHOR)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_TEXT)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_PICTURE)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_DATE)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_MEDIA)
            .append(" TEXT, ")
            .append(Tweets.COLUMN_USER_ID)
            .append(" INTEGER)")
            .toString();

    public TweetDatabase(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DEBUG", "onCreate DB");
        db.execSQL(CREATE_TABLE_TWEET);
        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DEBUG", "onUpgrade");
    }

    public static String getDbName() {
        return DB_NAME;
    }

    // public void dropTable(SQLiteDatabase db, String tableName) {
    // db.execSQL("DROP TABLE " + tableName);
    // }

}
