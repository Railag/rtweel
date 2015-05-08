package com.rtweel.tasks.timeline;

import android.os.AsyncTask;

import com.rtweel.profile.FollowersFragment;
import com.rtweel.storage.Tweets;

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
        int cursorType = params[2].intValue();

        try {
            followers = twitter.getFollowersList(id, cursor);
            followers.removeAll(mFragment.getFollowers());

            if (cursorType == FollowersFragment.FIRST_CURSOR)
                mFragment.getFollowers().addAll(0, followers);
            else if (cursorType == FollowersFragment.NEXT_CURSOR)
                mFragment.getFollowers().addAll(followers);
        } catch (TwitterException e) {
            e.printStackTrace();
            cancel(true);
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
        
        mFragment.setNextCursor(nextCursor);
    }
}
