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
public class TagTask extends AsyncTask<String, Void, Void> {

    private TagFragment mFragment;

    public TagTask(TagFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Void doInBackground(String... params) {
        Twitter twitter = Tweets.getTwitter(mFragment.getActivity());

        String queryString = params[0];

        if (TextUtils.isEmpty(queryString))
            return null;

        Query query = new Query();
        query.setResultType(Query.ResultType.popular);
        query.setQuery(queryString);
        query.setCount(100);
        //    query.setSinceId(mAdapter.getItem(0).getId());
        List<twitter4j.Status> resultList = null;
        try {
            QueryResult result = twitter.search(query);
            resultList = result.getTweets();
            mFragment.update(resultList);
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

