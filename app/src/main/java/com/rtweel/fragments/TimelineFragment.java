package com.rtweel.fragments;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.rtweel.R;
import com.rtweel.storage.AppUser;
import com.rtweel.Const;
import com.rtweel.services.TweetService;
import com.rtweel.timelines.Timeline;
import com.rtweel.TweetAdapter;

/**
 * Created by root on 21.3.15.
 */
public abstract class TimelineFragment extends PagerFragment {

    protected static final long ANIM_TIME = 200; //TODO fix with eternal loading when 400ms

    protected TweetAdapter adapter;

    protected Timeline mTimeline;

    protected RecyclerView list;

    protected boolean mContentLoaded;

    protected int mLastFirstVisibleItem = 0;

    protected LinearLayoutManager mLayoutManager;

    private boolean isAnimLocked;

    private Handler mHandler;
    private Runnable mAnimLockRunnable;
    private Runnable mRetryAnim;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timeline, null);

        setHasOptionsMenu(true);

        if (isLoading())
            stopLoading();

        Bundle args = getArguments();
        if (args != null) {
            String username = args.getString(Const.USERNAME);
            String userScreenName = args.getString(Const.SCREEN_USERNAME);
            long userId = args.getLong(Const.USER_ID);
            instantiateTimeline(username, userScreenName, userId);
        } else {
            String userName = AppUser.getUserName(getActivity());
            String screenUserName = AppUser.getScreenUserName(getActivity());
            long userId = AppUser.getUserId(getActivity());
            instantiateTimeline(userName, screenUserName, userId);
        }

        setTitle(getString(R.string.title_timeline));

        addTweetService();


        initHandler();

        initList(v);

        loadTweets();
        if (!getTimeline().isHomeTimeline())
            updateUp();

        initFloatingButton(v);

        return v;
    }

    private void initHandler() {
        mHandler = new Handler();
        mAnimLockRunnable = new Runnable() {
            @Override
            public void run() {
                isAnimLocked = false;
            }
        };
        mRetryAnim = new Runnable() {
            @Override
            public void run() {
                startLoadingAnim();
            }
        };
    }

    protected abstract void updateUp();

    protected abstract void updateDown();

    protected abstract void loadTweets();

    protected abstract void instantiateTimeline(String username, String userScreenName, long userId);

    protected abstract void loadingAnim();

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

        startLoadingAnim();

        adapter = new TweetAdapter(mTimeline, getActivity());


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

    private void addTweetService() {
        Intent serviceIntent = new Intent(getActivity(), TweetService.class);
        PendingIntent alarmIntent = PendingIntent.getService(getActivity(), 0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager
                .setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()
                                + AlarmManager.INTERVAL_HALF_HOUR,
                        AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return true;
    }


    public RecyclerView getList() {
        return list;
    }

    public TweetAdapter getAdapter() {
        return adapter;
    }

    public Timeline getTimeline() {
        return mTimeline;
    }

    public void startLoadingAnim() {
        if (isAnimLocked)
            mHandler.postDelayed(mRetryAnim, ANIM_TIME / 2);
        else {
            isAnimLocked = true;
            loadingAnim();
            mHandler.postDelayed(mAnimLockRunnable, ANIM_TIME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mAnimLockRunnable);
        mHandler.removeCallbacks(mRetryAnim);
    }

    @Override
    public long getUserId() {
        return getTimeline().getUserId();
    }
}
