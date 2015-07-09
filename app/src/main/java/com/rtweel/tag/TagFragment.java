package com.rtweel.tag;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.rtweel.R;
import com.rtweel.TweetAdapter;
import com.rtweel.fragments.RecyclerViewFragment;
import com.rtweel.storage.AppUser;
import com.rtweel.tasks.timeline.FollowersGetTask;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;

/**
 * Created by firrael on 9.7.15.
 */
public class TagFragment extends RecyclerViewFragment {

    public final static String QUERY = "query";

    private ArrayList<Status> tweets = new ArrayList<>();

    private TagTask task;

    private String query;

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new TweetAdapter(tweets, getActivity());
    }

    @Override
    protected void updateUp(Scroll scroll) {
        super.updateUp(scroll);

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new TagTask(TagFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        }
    }

    @Override
    protected void updateDown(Scroll scroll) {
        super.updateDown(scroll);

        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new TagTask(TagFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        }
    }

    @Override
    protected void instantiateListData(String username, String userScreenName, long userId) {
        Bundle args = getArguments();
        query = args.getString(QUERY);
    }

    @Override
    protected void listDataLoading() {
        updateUp(Scroll.UPDATE_UP);
    }

    @Override
    protected void startAnim() {
        showProgressBar();
    }

    @Override
    protected void stopAnim() {
        hideProgressBar();
    }

    @Override
    protected long getUserId() {
        return AppUser.getUserId(getActivity());
    }

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.tweet_empty_message);
    }

    public void update(List<Status> resultList) {
        tweets.addAll(resultList);
    }
}
