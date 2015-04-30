package com.rtweel.tasks.timeline;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.fragments.FollowersFragment;
import com.rtweel.fragments.TimelineFragment;
import com.rtweel.storage.Tweets;
import com.rtweel.timelines.Timeline;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class FollowersGetTask extends AsyncTask<Long, Void, Long> {

    private FollowersFragment mFragment;

    public FollowersGetTask(FollowersFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Long doInBackground(Long... params) {
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());

        PagableResponseList<User> followers = null;
        Long id = params[0];
        Long cursor = params[1];

        try {
            followers = twitter.getFollowersList(id, cursor);
            mFragment.getFollowers().addAll(followers);
        } catch (TwitterException e) {
            e.printStackTrace();
        }


        if (followers != null && followers.size() > 0)
            return followers.getNextCursor();
        else {
            cancel(true);
            return -1L;
        }

    }

    @Override
    protected void onPostExecute(Long nextCursor) {
        mFragment.getAdapter().notifyDataSetChanged();

        // mActivity.loadingAnim();

        if (mFragment.getActivity() != null) {
            mFragment.setNextCursor(nextCursor);
        } else
            Log.e("Exception", "TimelineUpTask lost context");
    }
}
