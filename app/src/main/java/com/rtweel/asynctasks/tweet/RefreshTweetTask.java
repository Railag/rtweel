package com.rtweel.asynctasks.tweet;

import android.os.AsyncTask;
import android.os.Bundle;

import com.rtweel.constant.Extras;
import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.DetailFragment;
import com.rtweel.tweet.Timeline;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.api.TweetsResources;

public class RefreshTweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

    private BaseFragment mFragment;
    private Long mId;
    private int mPosition;

    public RefreshTweetTask(BaseFragment fragment, int position) {
        mFragment = fragment;
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
            if (mFragment.isLoading())
                mFragment.stopLoading();
            Bundle args = new Bundle();
            args.putSerializable(Extras.TWEET, result);
            args.putInt(Extras.POSITION, mPosition);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);
            mFragment.getMainActivity().setMainFragment(fragment);
        } else {
            new DeleteTweetTask(mFragment, mPosition).execute(mId);
        }
    }
}
