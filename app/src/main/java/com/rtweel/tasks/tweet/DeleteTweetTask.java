package com.rtweel.tasks.tweet;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rtweel.detail.DetailFragment;
import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.HomeTweetFragment;
import com.rtweel.profile.TweetFragment;
import com.rtweel.storage.AppUser;
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
            if (result == AppUser.getLastUserTweetId(mFragment.getActivity())) {
                AppUser.setLastTweetId(0L);
                AppUser.setLastTweetTime(0L);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mFragment.getActivity());
                prefs.edit()
                        .putLong(AppUser.APP_USER_LAST_TWEET_ID, 0L)
                        .putLong(AppUser.APP_USER_LAST_TWEET_TIME, 0L)
                        .commit();
                mFragment.getMainActivity().hideLastTweetButton();
            }
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
