package com.rtweel.trends;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rtweel.R;
import com.rtweel.fragments.RecyclerViewFragment;
import com.rtweel.storage.AppUser;

import java.util.ArrayList;

import twitter4j.Trend;

/**
 * Created by firrael on 9.7.15.
 */
public class TrendsFragment extends RecyclerViewFragment {

    private ArrayList<Trend> trends = new ArrayList<>();

    private TrendsTask task;

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new TrendsAdapter(trends, getActivity());
    }

    @Override
    protected void instantiateListData(String username, String userScreenName, long userId) {
    }

    @Override
    protected void listDataLoading() {
        task = new TrendsTask(TrendsFragment.this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        return getString(R.string.trend_empty_message);
    }

    public void update(ArrayList<Trend> newTrends) {
        trends.addAll(newTrends);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  super.onCreateView(inflater, container, savedInstanceState);

        fab.hide();

        return v;
    }
}
