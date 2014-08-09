package com.rtweel.services;

import java.util.Date;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;

public class TweetService extends IntentService {

	public static final String MESSAGE = "message";

	public static final String TITLE = "title";

	private Timeline mTimeline;

	public TweetService() {
		super("TestService");
		mTimeline = new Timeline(this);
	}

	public TweetService(String name) {
		super(name);
		mTimeline = new Timeline(this);
	}

	@Override
	protected void onHandleIntent(Intent data) {
		Log.i("DEBUG", "onHandleIntent");
		if (Timeline.getDefaultTimeline() != null) {
			mTimeline = Timeline.getDefaultTimeline();
		} else {
			loadFromDB();
		}
		List<twitter4j.Status> downloadedList = mTimeline.downloadTimeline(0,
				Timeline.getTweetsPerPage(), Timeline.UP_TWEETS);
		mTimeline.updateTimelineUp(downloadedList);

		int size = downloadedList.size();
		String message = null;
		if (size > 0) {
			message = "Tweets downloaded: " + size;
		} else {
			message = "No new tweets";
		}
		Log.i("DEBUG", message);
		String title = "Tweet checking";

		Intent localIntent = new Intent(TweetReceiver.BROADCAST_ACTION)
				.putExtra(MESSAGE, message);
		localIntent.putExtra(TITLE, title);
	//	LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	sendBroadcast(localIntent);
	}

	// private int load() {
	// loadFromDB();

	/*
	 * if (mTimeline.getTweets().isEmpty()) { if (!isOnline()) { Log.i("DEBUG",
	 * "No network in TweetService"); return 0; } Log.i("DEBUG",
	 * "Loading timeline in service.."); mTimeline.getTweets().addAll(
	 * mTimeline.downloadTimeline(Timeline.getTweetsCount(),
	 * Timeline.getTweetsPerPage(), Timeline.INITIALIZATION_TWEETS));
	 * 
	 * Timeline.setTweetsCount(Timeline.getTweetsCount() +
	 * mTimeline.getTweets().size()); new DbWriteTask(this,
	 * mTimeline.getTweets(), mTimeline.getCurrentTimelineType()).execute();
	 * return mTimeline.getTweets().size(); }
	 */
	// return -1;
	// }

	private void loadFromDB() {
		Log.i("DEBUG", "loading from DB started..");
		Date time = new Date();
		String[] projection = { TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR,
				TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT,
				TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE,
				TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
				TweetDatabaseOpenHelper.Tweets.COLUMN_ID };

		ContentResolver resolver = getContentResolver();

		Cursor cursor = null;
		if (mTimeline.getCurrentTimelineType() == Timeline.HOME_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
					projection, null, null, null);
		} else if (mTimeline.getCurrentTimelineType() == Timeline.USER_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
					projection, null, null, null);
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String author = cursor.getString(cursor
						.getColumnIndex(projection[0]));
				String text = cursor.getString(cursor
						.getColumnIndex(projection[1]));
				String pictureUrl = cursor.getString(cursor
						.getColumnIndex(projection[2]));
				String date = cursor.getString(cursor
						.getColumnIndex(projection[3]));
				long id = cursor.getLong(cursor.getColumnIndex(projection[4]));

				try {
					String creation = "{text='" + text + "', id='" + id
							+ "', created_at='" + date + "',user={name='"
							+ author + "', profile_image_url='" + pictureUrl
							+ "'}}";

					Status insert = TwitterObjectFactory.createStatus(creation);
					mTimeline.getTweets().add(insert);
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}

			}
			Timeline.setTweetsCount(Timeline.getTweetsCount()
					+ mTimeline.getTweets().size());
			if (cursor != null) {
				cursor.close();
			}
			Date tmp = new Date();
			Log.i("DEBUG",
					"DB finished after " + (tmp.getTime() - time.getTime())
							+ "ms");
		}
	}

	private boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}
}
