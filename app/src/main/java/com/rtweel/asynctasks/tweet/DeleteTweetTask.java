package com.rtweel.asynctasks.tweet;

import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.rtweel.activities.DetailActivity;
import com.rtweel.activities.MainActivity;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;

public class DeleteTweetTask extends AsyncTask<Long, Void, Long> {

	public static final int MAIN = 1;
	public static final int DETAIL = 2;

	private final Context mContext;
	private final int mActivityType;
	private final int mPosition;

	public DeleteTweetTask(Context context, int type, int position) {
		mContext = context;
		mActivityType = type;
		mPosition = position;
	}

	@Override
	protected Long doInBackground(Long... params) {
		Timeline timeline = Timeline.getDefaultTimeline();
		try {
			timeline.getTwitter().destroyStatus(params[0]);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return params[0];
	}

	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);
		if (mActivityType == DETAIL) {
			DetailActivity detailActivity = (DetailActivity) mContext;
			detailActivity.getContentResolver().delete(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
					TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "="
							+ String.valueOf(result), null);
			detailActivity.getContentResolver().delete(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
					TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "="
							+ String.valueOf(result), null);
			Timeline.getDefaultTimeline().remove(mPosition);
			detailActivity.setResult(Activity.RESULT_OK);

			detailActivity.finish();
		} else if (mActivityType == MAIN) {
			MainActivity mainActivity = (MainActivity) mContext;
			mainActivity.getContentResolver().delete(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
					TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "="
							+ String.valueOf(result), null);
			mainActivity.getContentResolver().delete(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
					TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "="
							+ String.valueOf(result), null);
			Timeline.getDefaultTimeline().remove(mPosition);
			mainActivity.getAdapter().notifyDataSetChanged();
		}

	}
}
