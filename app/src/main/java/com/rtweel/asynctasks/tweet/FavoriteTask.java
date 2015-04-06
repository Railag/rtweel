package com.rtweel.asynctasks.tweet;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.fragments.DetailFragment;
import com.rtweel.timelines.Timeline;

import twitter4j.TwitterException;

public class FavoriteTask extends AsyncTask<Long, Void, Void> {

	private final DetailFragment mFragment;
	private final ImageView mFavoriteButton;
    private final TextView mCountView;
	private Boolean mIsFavorited;

	public FavoriteTask(DetailFragment fragment, ImageView button, TextView countView, Boolean isFavorited) {
		mFragment = fragment;
		mFavoriteButton = button;
        mCountView = countView;
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
		mFragment.changeIsFavorited();
		mIsFavorited = !mIsFavorited;
		if (mIsFavorited) {
            mFavoriteButton.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
			mCountView.setText(String.valueOf(Long
					.valueOf((String) mCountView.getText()) + 1));
			Toast.makeText(mFragment.getActivity(), "Added to favorites", Toast.LENGTH_LONG)
					.show();
		} else {
            mFavoriteButton.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			mCountView.setText(String.valueOf(Long
					.valueOf((String) mCountView.getText()) - 1));
			Toast.makeText(mFragment.getActivity(), "Removed from favorites",
					Toast.LENGTH_LONG).show();
		}
	}

}
