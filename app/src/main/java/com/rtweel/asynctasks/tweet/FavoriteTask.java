package com.rtweel.asynctasks.tweet;

import twitter4j.TwitterException;

import com.rtweel.activities.DetailActivity;
import com.rtweel.tweet.Timeline;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

public class FavoriteTask extends AsyncTask<Long, Void, Void> {

	private final Context mContext;
	private final Button mFavoriteButton;
	private Boolean mIsFavorited;

	public FavoriteTask(Context context, Button button, Boolean isFavorited) {
		mContext = context;
		mFavoriteButton = button;
		mIsFavorited = isFavorited;
	}

	@Override
	protected Void doInBackground(Long... params) {
		Timeline timeline = Timeline.getDefaultTimeline();
		try {
			if (mIsFavorited) {
				timeline.getTwitter().destroyFavorite(params[0]);
			} else {
				timeline.getTwitter().createFavorite(params[0]);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		DetailActivity detailActivity = (DetailActivity) mContext;
		detailActivity.changeIsFavorited();
		mIsFavorited = !mIsFavorited;
		if (mIsFavorited) {
			mFavoriteButton.setBackgroundColor(Color.GREEN);
			mFavoriteButton.setText(String.valueOf(Long
					.valueOf((String) mFavoriteButton.getText()) + 1));
			Toast.makeText(mContext, "Added to favorites", Toast.LENGTH_LONG)
					.show();
		} else {
			mFavoriteButton.setBackgroundColor(Color.DKGRAY);
			mFavoriteButton.setText(String.valueOf(Long
					.valueOf((String) mFavoriteButton.getText()) - 1));
			Toast.makeText(mContext, "Removed from favorites",
					Toast.LENGTH_LONG).show();
		}
	}

}
