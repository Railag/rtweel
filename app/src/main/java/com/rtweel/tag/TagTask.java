package com.rtweel.tag;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.rtweel.storage.Tweets;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by firrael on 9.7.15.
 */
public class TagTask extends AsyncTask<Query, Void, Void> {

    private TagFragment mFragment;

    public TagTask(TagFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Void doInBackground(Query... params) {
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());

        Query query = params[0];
        query.setResultType(Query.ResultType.mixed);
        query.setCount(50);

        List<twitter4j.Status> resultList;

        try {
            QueryResult result = twitter.search(query);
            Query nextQuery = null;

            if (!TextUtils.isEmpty(result.getRefreshURL())) {
                nextQuery = new Query();
                nextQuery.setQuery(result.getQuery());
                nextQuery.setMaxId(result.getMaxId());
            }
            
            if (result.hasNext())
                nextQuery = result.nextQuery();

            resultList = result.getTweets();
            mFragment.update(resultList, nextQuery);
        } catch
                (TwitterException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    protected void onPostExecute(Void result) {
        mFragment.getAdapter().notifyDataSetChanged();

        if (mFragment.getActivity() != null) {
            Log.i("PB", mFragment.getClass().getSimpleName() + " end hide dialog");

            mFragment.hideProgressBar();
        } else
            Log.e("Exception", "FollowersGetTask lost context");

        // mActivity.startAnim();

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i("PB", mFragment.getClass().getSimpleName() + " cancelled hide dialog");
        mFragment.hideProgressBar();
    }
}

