package com.rtweel.asynctasks.tweet;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.rtweel.activities.MainActivity;
import com.rtweel.constant.Extras;
import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.DetailFragment;
import com.rtweel.Timelines.Timeline;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.api.TweetsResources;

public class RefreshTweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

    private MainActivity mActivity;
    private Long mId;
    private int mPosition;

    public RefreshTweetTask(Context activity, int position) {
        mActivity = (MainActivity) activity;
        mPosition = position;
    }


    @Override
    protected twitter4j.Status doInBackground(Long... params) {
        mId = params[0];
        Twitter twitter = Timeline.getDefaultTimeline().getTwitter();
        TweetsResources tw = twitter.tweets();
        twitter4j.Status result = null;
        try {
            result = tw.showStatus(mId);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(twitter4j.Status result) {
        super.onPostExecute(result);
        if (result != null) {
            Fragment fragment = mActivity.getCurrentFragment();
            if(fragment instanceof DetailFragment) {
                DetailFragment detailFragment = (DetailFragment) fragment;
                Bundle args = new Bundle();
                args.putSerializable(Extras.TWEET, result);
                args.putInt(Extras.POSITION, mPosition);
                detailFragment.setResult(args);
            }
        } else {
            new DeleteTweetTask( (BaseFragment) mActivity.getCurrentFragment(), mPosition).execute(mId);
        }
    }
}
