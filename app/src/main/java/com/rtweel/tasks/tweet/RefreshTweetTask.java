package com.rtweel.tasks.tweet;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.rtweel.MainActivity;
import com.rtweel.storage.Tweets;
import com.rtweel.Const;
import com.rtweel.fragments.BaseFragment;
import com.rtweel.detail.DetailFragment;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.api.TweetsResources;

public abstract class RefreshTweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

    protected MainActivity mActivity;
    protected Long mId;
    protected int mPosition;

    public RefreshTweetTask(Context activity, int position) {
        mActivity = (MainActivity) activity;
        mPosition = position;
    }


    @Override
    protected twitter4j.Status doInBackground(Long... params) {
        mId = params[0];
        Twitter twitter = Tweets.getTwitter(mActivity);
        TweetsResources tw = twitter.tweets();
        twitter4j.Status result = null;
        try {
            result = tw.showStatus(mId);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }
}
