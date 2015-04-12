package com.rtweel.asynctasks.tweet;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.asynctasks.db.Tweets;
import com.rtweel.fragments.DetailFragment;
import com.rtweel.timelines.Timeline;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class RetweetTask extends AsyncTask<Long, Void, Long> {

    private final DetailFragment mFragment;
    private final ImageView mRetweetButton;
    private final TextView mCountView;
    private Boolean mIsRetweeted;

    public RetweetTask(DetailFragment fragment, ImageView button, TextView countView, Boolean isRetweeted) {
        mFragment = fragment;
        mRetweetButton = button;
        mCountView = countView;
        mIsRetweeted = isRetweeted;
    }

    @Override
    protected Long doInBackground(Long... params) {
        Long result = 0L;
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());

        try {
            if (mIsRetweeted) {
                twitter.destroyStatus(params[1]);
                result = 0L;
            } else {
                result = twitter.retweetStatus(params[0]).getId();
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);

        if (mFragment.getActivity() != null) {
            mFragment.changeIsRetweeted();
            mFragment.setRetweetId(result);
            mIsRetweeted = !mIsRetweeted;
            if (mIsRetweeted) {
                mRetweetButton.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
                mCountView.setText(String.valueOf(Long
                        .valueOf((String) mCountView.getText()) + 1));
                Toast.makeText(mFragment.getActivity(), "Retweeted", Toast.LENGTH_LONG).show();
            } else {
                mRetweetButton.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                mCountView.setText(String.valueOf(Long
                        .valueOf((String) mCountView.getText()) - 1));
                Toast.makeText(mFragment.getActivity(), "Unretweeted", Toast.LENGTH_LONG).show();
            }
        } else
            Log.e("Exception", "RetweetTask lost context");
    }

}
