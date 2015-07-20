package com.rtweel.tasks.tweet;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.rtweel.profile.AnswersTweetFragment;
import com.rtweel.storage.Tweets;
import com.rtweel.timelines.Timeline;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by firrael on 20.7.15.
 */
public class MentionsTask extends AsyncTask<String, Void, List<Status>> {

    private AnswersTweetFragment mFragment;
    private boolean mIsUp;

    public MentionsTask(AnswersTweetFragment fragment, boolean isUp) {
        mFragment = fragment;
        mIsUp = isUp;
    }

    @Override
    protected List<twitter4j.Status> doInBackground(String... params) {
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());

        String userName = params[0];

     //   String queryString = "@" + userName + " -from:@" + userName;
        String queryString = "@" + userName;

        if (TextUtils.isEmpty(queryString))
            return null;

        Query query = new Query();
        query.setResultType(Query.MIXED);
        query.setQuery(queryString);
        query.setCount(50);

        if (mFragment.getTimeline().getTweets().size() > 0) {
            if (mIsUp)
                query.setSinceId(mFragment.getNewestItemId());
            else
                query.setMaxId(mFragment.getOldestItemId());
        }

        List<twitter4j.Status> resultList = null;
        try {
            QueryResult result = twitter.search(query);
            resultList = result.getTweets();
        } catch (TwitterException e) {
            e.printStackTrace();
        }


        return resultList;
    }

    @Override
    protected void onPostExecute(List<twitter4j.Status> resultList) {
        super.onPostExecute(resultList);

        if (mFragment != null) {
            Timeline timeline = mFragment.getTimeline();
            for (twitter4j.Status s : resultList) {
                if (!timeline.getTweets().contains(s))
                    if (mIsUp)
                        timeline.getTweets().add(0, s);
                    else
                        timeline.getTweets().add(s);
            }
            mFragment.getAdapter().notifyDataSetChanged();

            if (mFragment.getTimeline().getTweets().size() > 0)
                mFragment.setStateLoaded();

            mFragment.stopLoadingAnim();
        }
    }

}

