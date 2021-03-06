package com.rtweel.profile;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.rtweel.FavoriteAdapter;
import com.rtweel.R;
import com.rtweel.fragments.RecyclerViewFragment;
import com.rtweel.tasks.timeline.FollowersGetTask;

import java.util.ArrayList;
import java.util.List;

import twitter4j.User;

/**
 * Created by firrael on 28.4.15.
 */
public class FollowersFragment extends RecyclerViewFragment {

    public final static long FIRST_CURSOR = 1L;
    public final static long NEXT_CURSOR = 2L;

    private ArrayList<User> users = new ArrayList<>();

    private FollowersGetTask task;

    private Long mNextCursor = -1L;


    @Override
    protected void updateUp(Scroll scroll) {
        super.updateUp(scroll);

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new FollowersGetTask(FollowersFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUserId, -1L, FIRST_CURSOR);
        }
    }

    @Override
    protected void updateDown(Scroll scroll) {
        super.updateDown(scroll);

        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new FollowersGetTask(FollowersFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUserId, mNextCursor, NEXT_CURSOR);
        }
    }

    @Override
    protected void instantiateListData(String username, String userScreenName, long userId) {
        mUserId = userId;
    }

    @Override
    protected void listDataLoading() {
        updateUp(Scroll.UPDATE_UP);
    }

    protected void startAnim() {
        showProgressBar();
    }

    @Override
    protected void stopAnim() {
        hideProgressBar();
    }

    @Override
    protected long getUserId() {
        return mUserId;
    }

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.followers_empty_message);
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new FavoriteAdapter(users, getActivity());
    }

    public void setNextCursor(Long nextCursor) {
        mNextCursor = nextCursor;
    }

    public List<User> getFollowers() {
        return users;
    }

    @Override
    protected String getTitle() {
        return null;
    }
}

