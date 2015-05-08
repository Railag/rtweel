package com.rtweel.direct;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.rtweel.fragments.BaseListFragment;

import java.util.ArrayList;

/**
 * Created by root on 7.5.15.
 */
public class DirectMessagesMainFragment extends BaseListFragment {

    private DirectMessagesTask task;

    private ArrayList<DirectUser> users = new ArrayList<>();

    private Integer pageReceived = 1;
    private Integer pageSent = 1;

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new DirectAdapter(users, getActivity());
    }

    @Override
    protected void updateUp(Scroll scroll) {

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new DirectMessagesTask(DirectMessagesMainFragment.this);
            task.execute(1, 1);
        }
    }

    @Override
    protected void updateDown(Scroll scroll) {
        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new DirectMessagesTask(DirectMessagesMainFragment.this);
            task.execute(pageReceived, pageSent);
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
    protected void loadingAnim() {
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