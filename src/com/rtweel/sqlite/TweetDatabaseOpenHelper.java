package com.rtweel.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class TweetDatabaseOpenHelper extends SQLiteOpenHelper {

	public static final class Tweets implements BaseColumns {
		public static final Uri CONTENT_URI_HOME_DB = Uri
				.parse("content://com.rtweel.sqlite.TweetContentProvider/home_timeline");
		public static final Uri CONTENT_URI_USER_DB = Uri
				.parse("content://com.rtweel.sqlite.TweetContentProvider/user_timeline");
		public static final String TABLE_NAME_HOME = "home_timeline";
		public static final String TABLE_NAME_USER = "user_timeline";
		public static final String COLUMN_AUTHOR = "author";
		public static final String COLUMN_TEXT = "text";
		public static final String COLUMN_PICTURE = "picture_url";
		public static final String COLUMN_DATE = "created_at";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_RETWEET_COUNT = "retweet_count";
		public static final String COLUMN_FAVORITE_COUNT = "favorite_count";
	}

	private static final String DB_NAME = "tweets.db";
	private static final int VERSION = 1;
	private static final String CREATE_TABLE_HOME = "CREATE TABLE "
			+ Tweets.TABLE_NAME_HOME + "(" + Tweets._ID
			+ " INTEGER PRIMARY KEY, " + Tweets.COLUMN_AUTHOR + " TEXT, "
			+ Tweets.COLUMN_TEXT + " TEXT, " + Tweets.COLUMN_PICTURE
			+ " TEXT, " + Tweets.COLUMN_DATE + " TEXT, "
			+ Tweets.COLUMN_RETWEET_COUNT + " INTEGER, "
			+ Tweets.COLUMN_FAVORITE_COUNT + " INTEGER, " + Tweets.COLUMN_ID
			+ " INTEGER)";
	private static final String CREATE_TABLE_USER = "CREATE TABLE "
			+ Tweets.TABLE_NAME_USER + "(" + Tweets._ID
			+ " INTEGER PRIMARY KEY, " + Tweets.COLUMN_AUTHOR + " TEXT, "
			+ Tweets.COLUMN_TEXT + " TEXT, " + Tweets.COLUMN_PICTURE
			+ " TEXT, " + Tweets.COLUMN_DATE + " TEXT, "
			+ Tweets.COLUMN_RETWEET_COUNT + " INTEGER, "
			+ Tweets.COLUMN_FAVORITE_COUNT + " INTEGER, " + Tweets.COLUMN_ID
			+ " INTEGER)";

	public TweetDatabaseOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("DEBUG", "onCreate DB");
		db.execSQL(CREATE_TABLE_HOME);
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
