package com.rtweel.direct;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.fragments.RecyclerViewFragment;

import java.util.ArrayList;
import java.util.Collections;

import twitter4j.DirectMessage;
import twitter4j.User;

/**
 * Created by firrael on 7.5.15.
 */
public class DirectMessagesMainFragment extends RecyclerViewFragment {

    private DirectMessagesTask task;

    private ArrayList<DirectUser> users = new ArrayList<>();

    private Integer pageReceived = 1;
    private Integer pageSent = 1;

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new DirectAdapter(users, getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        setTitle(getString(R.string.title_messages));
    }

    @Override
    protected void updateUp(Scroll scroll) {

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            startLoadingAnim();
            task = new DirectMessagesTask(DirectMessagesMainFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1, 1);
        }
    }

    @Override
    protected void updateDown(Scroll scroll) {
        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            startLoadingAnim();
            task = new DirectMessagesTask(DirectMessagesMainFragment.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pageReceived, pageSent);
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
        if (getActivity() != null)
            getMainActivity().showLoadingBar();
    }

    @Override
    protected void stopAnim() {
        if (getActivity() != null)
            getMainActivity().hideLoadingBar();
    }

    @Override
    protected long getUserId() {
        return mUserId;
    }

    public ArrayList<DirectUser> getUsers() {
        return users;
    }

    public void setPageReceived(int page) {
        pageReceived = page;
    }

    public void setPageSent(int page) {
        pageSent = page;
    }
}