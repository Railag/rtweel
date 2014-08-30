package com.rtweel.asynctasks.tweet;

import twitter4j.TwitterException;

import com.rtweel.activities.DetailActivity;
import com.rtweel.tweet.Timeline;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

public class RetweetTask extends AsyncTask<Long, Void, Long> {

	private final Context mContext;
	private final Button mRetweetButton;
	private Boolean mIsRetweeted;

	public RetweetTask(Context context, Button button, Boolean isRetweeted) {
		mContext = context;
		mRetweetButton = button;
		mIsRetweeted = isRetweeted;
	}

	@Override
	protected Long doInBackground(Long... params) {
		Timeline timeline = Timeline.getDefaultTimeline();
		Long result = 0L;
		try {
			if (mIsRetweeted) {
				timeline.getTwitter().destroyStatus(params[1]);
				result = 0L;
			} else {
				result = timeline.getTwitter().retweetStatus(params[0]).getId();
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);
		DetailActivity detailActivity = (DetailActivity) mContext;
		detailActivity.changeIsRetweeted();
		detailActivity.setRetweetId(result);
		mIsRetweeted = !mIsRetweeted;
		if (mIsRetweeted) {
			mRetweetButton.setBackgroundColor(Color.GREEN);
			mRetweetButton.setText(String.valueOf(Long
					.valueOf((String) mRetweetButton.getText()) + 1));
			Toast.makeText(mContext, "Retweeted", Toast.LENGTH_LONG).show();
		} else {
			mRetweetButton.setBackgroundColor(Color.DKGRAY);
			mRetweetButton.setText(String.valueOf(Long
					.valueOf((String) mRetweetButton.getText()) - 1));
			Toast.makeText(mContext, "Unretweeted", Toast.LENGTH_LONG).show();
		}
	}

}
