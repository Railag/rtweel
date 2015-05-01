package com.rtweel.fragments;

import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.rtweel.Const;
import com.rtweel.FavoriteAdapter;
import com.rtweel.R;
import com.rtweel.listeners.HideHeaderOnScrollListener;
import com.rtweel.storage.AppUser;
import com.rtweel.tasks.timeline.FollowersGetTask;

import java.util.ArrayList;
import java.util.List;

import twitter4j.User;

/**
 * Created by root on 28.4.15.
 */
public class FollowersFragment extends PagerFragment {

    public final static long FIRST_CURSOR = 1L;
    public final static long NEXT_CURSOR = 2L;

    private FavoriteAdapter adapter;

    private ArrayList<User> users = new ArrayList<>();
    private Long userId = -1L;

    private FollowersGetTask task;

    private RecyclerView list;

    private int mLastFirstVisibleItem = 0;

    private LinearLayoutManager mLayoutManager;

    private HideHeaderOnScrollListener mListener;

    private Long mNextCursor = -1L;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timeline, null);

        setHasOptionsMenu(true);

        if (isLoading())
            stopLoading();

        Bundle args = getArguments();
        if (args != null)
            userId = args.getLong(Const.USER_ID);
        else
            userId = AppUser.getUserId(getActivity());


        setTitle(getString(R.string.title_timeline));

        updateUp();

        initList(v);

        initFloatingButton(v);

        return v;
    }

    private void updateUp() {
        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new FollowersGetTask(FollowersFragment.this);
            task.execute(userId, -1L, FIRST_CURSOR);
        }
    }

    private void updateDown() {
        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new FollowersGetTask(FollowersFragment.this);
            task.execute(userId, mNextCursor, NEXT_CURSOR);
        }
    }

    private void loadingAnim() {

    }

    private void initList(View v) {

        list = (RecyclerView) v.findViewById(R.id.list);

        list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int firstVisibleItem = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                int historySize = event.getHistorySize();
                int action = event.getAction();


                //only swipes
                if (action == MotionEvent.ACTION_MOVE && historySize > 0) {
                    float historicalY = event.getHistoricalY(historySize - 1);
                    float y = event.getY();

                    //showing profile header swipe
                    if (firstVisibleItem <= mLastFirstVisibleItem && firstVisibleItem <= 0 && detectSwipeUp(y, historicalY))
                        if (mListener != null && mListener.isHidden()) {
                            mListener.onTop();
                            return true;
                        }

                    //hiding profile header swipe
                    if (firstVisibleItem > 0 && detectSwipeDown(y, historicalY)) {
                        if (mListener != null && !mListener.isHidden()) {
                            mListener.onScrollDown();
                            return true;
                        }
                    }

                    //basic swipe up updating at top
                    if (firstVisibleItem < 1 && mLastFirstVisibleItem == 0) {
                        if (detectSwipeUp(y, historicalY)) {
                            updateUp();
                            mLastFirstVisibleItem = firstVisibleItem;
                            return true;
                        }
                    }

                    //basic swipe down updating
                    if (firstVisibleItem > mLastFirstVisibleItem && (adapter.getItemCount() - firstVisibleItem) < 4) {
                        updateDown();
                        mLastFirstVisibleItem = firstVisibleItem;
                        return true;
                    }

                    //pre-basic swipe down updating
                    int count = adapter.getItemCount();
                    int last = mLayoutManager.findLastVisibleItemPosition();

                    if (count - last < 3 && detectSwipeDown(y, historicalY)) {
                        updateDown();
                        return true;
                    }
                }

                mLastFirstVisibleItem = firstVisibleItem;
                return false;
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());

        adapter = new FavoriteAdapter(users, getActivity());

        list.setAdapter(adapter);
    }

    private void initFloatingButton(View v) {
        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setType(1);
        fab.setColorNormal(R.color.green);
        fab.setColorPressed(R.color.blue);
        fab.setColorRipple(R.color.red);
        fab.attachToRecyclerView(list);
        fab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        fab.setImageDrawable(getResources().getDrawable(R.drawable.up_arrow));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.scrollToPosition(0);
                updateUp();
                fab.hide(true);
            }
        });
    }

    private boolean detectSwipeDown(float y, float historicalY) {
        return y < historicalY;
    }

    private boolean detectSwipeUp(float y, float historicalY) {
        return y > historicalY;
    }

    public void blink() {

        ValueAnimator fade = new ValueAnimator();
        fade.setFloatValues(1, 0.6f, 1);
        fade.setDuration(1400);
        fade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                list.setAlpha(value);
            }
        });

        fade.start();
    }

    public RecyclerView getList() {
        return list;
    }

    public FavoriteAdapter getAdapter() {
        return adapter;
    }

    public void setHideHeaderListener(HideHeaderOnScrollListener listener) {
        mListener = listener;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setNextCursor(Long nextCursor) {
        mNextCursor = nextCursor;
    }

    public List<User> getFollowers() {
        return users;
    }
}

