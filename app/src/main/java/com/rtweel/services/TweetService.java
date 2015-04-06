package com.rtweel.services;

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
import android.util.Log;

import com.rtweel.Timelines.UserTimeline;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.Timelines.Timeline;

public class TweetService extends IntentService {

	public static final String MESSAGE = "message";

	public static final String TITLE = "title";

	private static int sNewTweets = 0;

	private Timeline mTimeline;

	public TweetService() {
		super("TestService");
		mTimeline = new UserTimeline(this, Timeline.USER_TIMELINE);
	}

	public TweetService(String name, int type) {
		super(name);
		mTimeline = new UserTimeline(this, type);
	}

	@Override
	protected void onHandleIntent(Intent data) {
		if (!isOnline()) {
			return;
		}
		String message = null;
		Log.i("DEBUG", "onHandleIntent");
		if (Timeline.getDefaultTimeline() != null) {
			mTimeline = Timeline.getDefaultTimeline();
		} else {
			loadFromDB();
		}
		try {
			List<twitter4j.Status> downloadedList = mTimeline.downloadTimeline(
			// 0, Timeline.getTweetsPerPage(),
					Timeline.UP_TWEETS);
			mTimeline.updateTimelineUp(downloadedList);

			int size = downloadedList.size();
			Log.i("DEBUG", "Size: " + String.valueOf(size));
			if (size > 0) {
				if (sNewTweets != 0) {
					sNewTweets += size;
					size = sNewTweets;
				} else {
					sNewTweets = size;
				}
				message = "Tweets downloaded: " + size;
				String title = "Tweet checking";

				Intent localIntent = new Intent(TweetReceiver.BROADCAST_ACTION)
						.putExtra(MESSAGE, message);
				localIntent.putExtra(TITLE, title);
				// LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
				sendBroadcast(localIntent);

			} else {
				message = "No new tweets";
			}
			Log.i("DEBUG", message);
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.i("DEBUG", "NPE in service");
		}
	}

	private void loadFromDB() {
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
					projection, null, null, TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 1");
		} else if (mTimeline.getCurrentTimelineType() == Timeline.USER_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
					projection, null, null, TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 1");
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String author = cursor.getString(cursor
						.getColumnIndex(projection[0]));
				String text = cursor.getString(cursor
						.getColumnIndex(projection[1])).replace("\\n", "\n");
				String pictureUrl = cursor.getString(cursor
						.getColumnIndex(projection[2]));
				String date = cursor.getString(cursor
						.getColumnIndex(projection[3]));
				long id = cursor.getLong(cursor.getColumnIndex(projection[4]));

				try {
					String creation = "{text='" + text + "', id='" + id
							+ "', created_at='" + date
							+ "',user={name='" + author
							+ "', profile_image_url='" + pictureUrl + "'}}";
					Status insert = TwitterObjectFactory.createStatus(creation);
					mTimeline.getTweets().add(insert);
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}

			}

			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	public static void setNewTweets(int count) {
		sNewTweets = count;
	}
}
