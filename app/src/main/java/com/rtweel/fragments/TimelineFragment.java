package com.rtweel.fragments;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.rtweel.R;
import com.rtweel.cache.App;
import com.rtweel.listeners.HideHeaderOnScrollListener;
import com.rtweel.services.TweetService;
import com.rtweel.sqlite.TweetDatabase;
import com.rtweel.timelines.Timeline;
import com.rtweel.tweet.TweetAdapter;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

/**
 * Created by root on 21.3.15.
 */
public abstract class TimelineFragment extends BaseFragment {

    protected TweetAdapter adapter;

    protected Timeline mTimeline;

    protected RecyclerView list;

    protected boolean mContentLoaded;

    protected int mLastFirstVisibleItem = 0;

    protected LinearLayoutManager mLayoutManager;

    private HideHeaderOnScrollListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timeline, null);

        setHasOptionsMenu(true);

        if (isLoading())
            stopLoading();

        instantiateTimeline();

        setTitle(getString(R.string.title_timeline));

        addTweetService();

        loadTweets();

        initList(v);

        initFloatingButton(v);

        return v;
    }

    protected abstract void updateUp();

    protected abstract void updateDown();

    protected abstract void loadTweets();

    protected abstract void instantiateTimeline();

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
                    if (firstVisibleItem > 0 && detectSwipeDown(y, historicalY) ) {
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

        loadingAnim();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
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
            case R.id.tweet_send_open: {
                getMainActivity().setMainFragment(new SendTweetFragment());
                break;
            }
            case R.id.logout_button: {
                App app = (App) getActivity().getApplication();

                boolean dbDeleted = getActivity().deleteDatabase(TweetDatabase
                        .getDbName());
                Log.i("DEBUG", "DB DELETED = " + dbDeleted);

                app.createDb();

                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                editor.putString(
                        ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
                editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,
                        false);
                editor.commit();

                TwitterUtil.getInstance().reset();
                getMainActivity().finish();
                break;
            }
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

    public void setHideHeaderListener(HideHeaderOnScrollListener listener) {
        mListener = listener;
    }

    public void startLoadingAnim() {
        loadingAnim();
    }

}
