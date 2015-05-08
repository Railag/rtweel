package com.rtweel.tasks.tweet;

import android.os.AsyncTask;
import android.util.Log;

import com.rtweel.fragments.BaseFragment;
import com.rtweel.detail.DetailFragment;
import com.rtweel.fragments.HomeTweetFragment;
import com.rtweel.profile.TweetFragment;
import com.rtweel.storage.TweetDatabase;
import com.rtweel.storage.Tweets;

import twitter4j.TwitterException;

public class DeleteTweetTask extends AsyncTask<Long, Void, Long> {

    private final BaseFragment mFragment;
    private final int mPosition;

    public DeleteTweetTask(BaseFragment fragment, int position) {
        mFragment = fragment;
        mPosition = position;
    }

    @Override
    protected Long doInBackground(Long... params) {
        try {
            Tweets.getTwitter(mFragment.getActivity()).destroyStatus(params[0]);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);

        if (mFragment.getActivity() != null) {
            mFragment.getActivity().getContentResolver().delete(
                    TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                    TweetDatabase.Tweets._ID + "="
                            + String.valueOf(result), null);
            mFragment.getActivity().getContentResolver().delete(
                    TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                    TweetDatabase.Tweets._ID + "="
                            + String.valueOf(result), null);

            if (mFragment instanceof DetailFragment) {
                mFragment.getMainActivity().setMainFragment(new HomeTweetFragment());
            } else if (mFragment instanceof TweetFragment) {
                TweetFragment timelineFragment = (TweetFragment) mFragment;
                timelineFragment.getTimeline().remove(mPosition);
                timelineFragment.getAdapter().notifyDataSetChanged();
            }
        } else
            Log.e("Exception", "DeleteTweetTask lost context");

    }
}
