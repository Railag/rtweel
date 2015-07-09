package com.rtweel.trends;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.rtweel.R;
import com.rtweel.fragments.RecyclerViewFragment;
import com.rtweel.storage.AppUser;
import com.rtweel.tag.TagTask;

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
    protected void updateUp(Scroll scroll) {
        super.updateUp(scroll);

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new TrendsTask(TrendsFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void updateDown(Scroll scroll) {
        super.updateDown(scroll);

        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new TrendsTask(TrendsFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void instantiateListData(String username, String userScreenName, long userId) {
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
        return getString(R.string.trend_empty_message);
    }

    public void update(ArrayList<Trend> newTrends) {
        trends.addAll(newTrends);
    }
}
