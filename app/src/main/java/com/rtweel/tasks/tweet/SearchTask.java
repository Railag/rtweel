package com.rtweel.tasks.tweet;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;

import com.rtweel.SearchAdapter;
import com.rtweel.SearchItem;
import com.rtweel.storage.Tweets;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by firrael on 17.6.15.
 */
public class SearchTask extends AsyncTask<String, Void, List<SearchItem>> {

    private final Context mContext;
    private SearchAdapter mAdapter;
    private final AutoCompleteTextView mActv;

    public SearchTask(Context context, SearchAdapter adapter, AutoCompleteTextView actv) {
        mContext = context;
        mAdapter = adapter;
        mActv = actv;
    }

    @Override
    protected List<SearchItem> doInBackground(String... params) {
        Twitter twitter = Tweets.getTwitter(mContext);

        String queryString = params[0];

        if (TextUtils.isEmpty(queryString))
            return null;

        Query query = new Query();
        query.setResultType(Query.RECENT);
        query.setQuery(queryString);
        query.setCount(10);

        List<twitter4j.Status> resultList = null;
        List<twitter4j.User> users = null;
        try {
            QueryResult result = twitter.search(query);
            users = twitter.searchUsers(queryString, 0);
            resultList = result.getTweets();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        List<SearchItem> items = new ArrayList<>();

        if (users != null && users.size() > 0) {
            if (users.size() > 10)
                users = users.subList(0, 10);
            for (User u : users)
                items.add(new SearchItem(u));
        }

        if (resultList != null && resultList.size() > 0)
            for (twitter4j.Status s : resultList)
                items.add(new SearchItem(s));

        return items;
    }

    @Override
    protected void onPostExecute(List<SearchItem> resultList) {
        super.onPostExecute(resultList);

        if (mContext != null) {
            mAdapter = new SearchAdapter(resultList, mContext);
            mActv.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mActv.showDropDown();
        }
    }

}
