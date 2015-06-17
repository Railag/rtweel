package com.rtweel.tasks.tweet;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

import com.rtweel.storage.Tweets;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by firrael on 17.6.15.
 */
public class SearchTask extends AsyncTask<String, Void, List<twitter4j.Status>> {

    private final Context mContext;
    private final ArrayAdapter<String> mAdapter;
    private final MultiAutoCompleteTextView mMactv;

    public SearchTask(Context context, ArrayAdapter<String> adapter, MultiAutoCompleteTextView mactv) {
        mContext = context;
        mAdapter = adapter;
        mMactv = mactv;
    }

    @Override
    protected List<twitter4j.Status> doInBackground(String... params) {
        Twitter twitter = Tweets.getTwitter(mContext);

        String queryString = params[0];

        if (TextUtils.isEmpty(queryString))
            return null;

        Query query = new Query();
        query.setResultType(Query.RECENT);
        query.setQuery(queryString);
        query.setCount(100);
        //    query.setSinceId(mAdapter.getItem(0).getId());
        List<twitter4j.Status> resultList = null;
        try {
            QueryResult result = Tweets.getTwitter(mContext).search(query);
            resultList = result.getTweets();

            //    TODO fetch next page   result.nextQuery();
        } catch
                (TwitterException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    @Override
    protected void onPostExecute(List<twitter4j.Status> resultList) {
        super.onPostExecute(resultList);

        mAdapter.clear();
        if (resultList != null && resultList.size() > 0) {
            for (int i = 0; i < resultList.size(); i++) {
                String s = resultList.get(i).getUser().getScreenName();
                Log.i("Search", s);
                mAdapter.add(s);
            }
        }

        mMactv.showDropDown();
    }

}
