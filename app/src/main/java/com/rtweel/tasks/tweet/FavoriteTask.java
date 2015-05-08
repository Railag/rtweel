package com.rtweel.tasks.tweet;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.storage.Tweets;
import com.rtweel.detail.DetailFragment;

import twitter4j.Twitter;
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
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());
        try {
			if (mIsFavorited) {
				twitter.destroyFavorite(params[0]);
			} else {
				twitter.createFavorite(params[0]);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
        if(mFragment.getActivity() != null) {
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
        } else
            Log.e("Exception", "FavoriteTask lost context");
	}

}
