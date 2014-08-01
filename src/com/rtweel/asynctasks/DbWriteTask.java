package com.rtweel.asynctasks;

import java.util.ArrayList;
import java.util.List;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

public class DbWriteTask extends AsyncTask<Void, Void, Void> {

	private final Context sContext;

	private List<twitter4j.Status> mList = new ArrayList<twitter4j.Status>();

	private int mTimelineType;

	public DbWriteTask(Context context, List<twitter4j.Status> list,
			int timelineType) {
		sContext = context;
		mList.addAll(list);
		mTimelineType = timelineType;
	}

	@Override
	protected Void doInBackground(Void... params) {

		ContentResolver resolver = sContext.getContentResolver();
		ContentValues values = new ContentValues();
		for (twitter4j.Status s : mList) {
			values.put(TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR, s
					.getUser().getName());
			String text = s.getText().replace('\'', ' ').replace('\n', ' ');
			values.put(TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT, text);
			values.put(TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE, s
					.getUser().getProfileImageURL());

			// String date = DateParser.parse(s.getCreatedAt().toString());
			// values.put(TweetDatabaseOpenHelper.Tweets.COLUMN_DATE, date);

			values.put(TweetDatabaseOpenHelper.Tweets.COLUMN_DATE, s
					.getCreatedAt().toString());

			// values.put(TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
			// s.getCreatedAt().getTime());//.toString());
			values.put(TweetDatabaseOpenHelper.Tweets.COLUMN_ID, s.getId());
			if(mTimelineType == Timeline.HOME_TIMELINE) {
				resolver.insert(TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
					values);
			} else if (mTimelineType == Timeline.USER_TIMELINE) {
				resolver.insert(TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
						values);
			}
		}

		return null;
	}
}