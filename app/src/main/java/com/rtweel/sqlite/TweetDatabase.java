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


	}

    public final static String SELECTION_ASC = " ASC ";
    public final static String SELECTION_DESC = " DESC ";

	private static final String DB_NAME = "tweets.db";
	private static final int VERSION = 1;
	private static final String CREATE_TABLE_TWEET = "CREATE TABLE "
			+ Tweets.TABLE_NAME_TWEET + "(" + Tweets._ID
			+ " INTEGER PRIMARY KEY, " + Tweets.COLUMN_AUTHOR + " TEXT, "
			+ Tweets.COLUMN_TEXT + " TEXT, " + Tweets.COLUMN_PICTURE
			+ " TEXT, " + Tweets.COLUMN_DATE + " TEXT, " + Tweets.COLUMN_MEDIA
			+ " TEXT, " + Tweets.COLUMN_USER_ID + " INTEGER)";
	private static final String CREATE_TABLE_USER = "CREATE TABLE "
			+ Tweets.TABLE_NAME_USER + "(" + Tweets._ID
			+ " INTEGER PRIMARY KEY, " + Tweets.COLUMN_AUTHOR + " TEXT, "
			+ Tweets.COLUMN_TEXT + " TEXT, " + Tweets.COLUMN_PICTURE
			+ " TEXT, " + Tweets.COLUMN_DATE + " TEXT, " + Tweets.COLUMN_MEDIA
			+ " TEXT, " + Tweets.COLUMN_USER_ID + " INTEGER)";

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
