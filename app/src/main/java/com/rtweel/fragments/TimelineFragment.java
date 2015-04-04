package com.rtweel.fragments;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.rtweel.R;
import com.rtweel.asynctasks.timeline.LoadTimelineTask;
import com.rtweel.asynctasks.timeline.TimelineDownTask;
import com.rtweel.asynctasks.timeline.TimelineUpTask;
import com.rtweel.cache.App;
import com.rtweel.services.TweetService;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;
import com.rtweel.tweet.TweetAdapter;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Created by root on 21.3.15.
 */
public class TimelineFragment extends BaseFragment {

    private TweetAdapter adapter;

    private Timeline mTimeline;

    private RecyclerView list;

    private boolean mContentLoaded;

    private int mLastVisibleItem = 0;

    private TimelineUpTask mUpTask;
    private TimelineDownTask mDownTask;

    private LinearLayoutManager mLayoutManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timeline, null);

        setHasOptionsMenu(true);

        if (isLoading())
            stopLoading();

        //    if(Timeline.getDefaultTimeline() == null) {
        mTimeline = new Timeline(getActivity().getApplicationContext());

        Timeline.setDefaultTimeline(mTimeline);
        //    } else

        //    mTimeline = Timeline.getDefaultTimeline();

        //    mTimeline.l

        setTitle(getString(R.string.title_timeline));

        addTweetService();

        new LoadTimelineTask(this).execute(mTimeline);

        initList(v);

        return v;
    }

    private void initList(View v) {

        list = (RecyclerView) v.findViewById(R.id.list);

        //list.setDivider(getResources().getDrawable(android.R.drawable.divider_horizontal_textfield));


        list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int firstVisibleItem = mLayoutManager.findFirstCompletelyVisibleItemPosition();

                if(event.getAction() == MotionEvent.ACTION_UP && mLastVisibleItem == 0) {
                    updateUp();
                    return true;
                }

                if (firstVisibleItem == 0 && mLastVisibleItem > firstVisibleItem) {
                    updateUp();
                    mLastVisibleItem = 0;
                    return true;
                }
                if ( event.getAction() == MotionEvent.ACTION_MOVE && firstVisibleItem > mLastVisibleItem && (adapter.getItemCount() - firstVisibleItem) < 4) {
                    updateDown();
                    mLastVisibleItem = firstVisibleItem;
                    return true;
                }

                int count = adapter.getItemCount();
                int last = mLayoutManager.findLastVisibleItemPosition();
                if(event.getAction() == MotionEvent.ACTION_UP && count - last < 3) {
                    updateDown();
                    return true;
                }

                mLastVisibleItem = firstVisibleItem;
                return false;
            }
        });

//        list.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                Log.i("DEBUG", "Coords:" + dx + dy);
//                super.onScrolled(recyclerView, dx, dy);
////                mLastYChange = dy;
//
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                Log.i("DEBUG", "Axis y scroll");
//                super.onScrollStateChanged(recyclerView, newState);
//     //           if (newState == RecyclerView.SCROLL_AXIS_VERTICAL)
//
//                //    if(== RecyclerView.SCROLL_STATE_IDLE && mLastYChange);
//            }
//        });


        mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());




        list.setVisibility(View.GONE);
        crossfade();


        /*
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == SCROLL_STATE_IDLE && mLastVisibleItem == 0)
                    updateUp();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0 && mLastVisibleItem > firstVisibleItem) {
                    updateUp();
                    mLastVisibleItem = 0;
                    return;
                }
                if (firstVisibleItem > mLastVisibleItem && (totalItemCount - firstVisibleItem) < 5) {
                    updateDown();
                    mLastVisibleItem = firstVisibleItem;
                    return;
                }

                mLastVisibleItem = firstVisibleItem;
            }
        });
    */
        //mAdapter = list;

        adapter = new TweetAdapter(mTimeline, getActivity());

        //mAdapter.setAdapter(adapter);

        //mTimeline.setAdapter(adapter);

        list.setAdapter(adapter);


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
                fab.hide(true);
            }
        });
    }

    private void updateUp() {

        blink();
        if (!App.isOnline(getActivity())) {
            Log.i("DEBUG", "Up swipe NO NETWORK");
            Toast.makeText(
                    getActivity(),
                    "No network connection, couldn't load tweets!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("DEBUG", "SWIPE UP");
        if (mUpTask != null)
            if (!mUpTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mUpTask = new TimelineUpTask(TimelineFragment.this);
        mUpTask.execute(mTimeline);

    }

    private void updateDown() {
        blink();
        if (!App.isOnline(getActivity())) {
            Log.i("DEBUG", "Down swipe NO NETWORK");
            Toast.makeText(
                    getActivity(),
                    "No network connection, couldn't load tweets!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("DEBUG", "SWIPE DOWN");

        if (mDownTask != null)
            if (!mDownTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mDownTask = new TimelineDownTask(TimelineFragment.this);
        mDownTask.execute(mTimeline);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_home_timeline: {

                if (!App.isOnline(getActivity())) {
                    Log.i("DEBUG", "home timeline button onClick NO NETWORK");
                    Toast.makeText(getActivity(),
                            "No network connection, couldn't load tweets!",
                            Toast.LENGTH_LONG).show();
                    break;
                }
                if (mTimeline.getCurrentTimelineType() != Timeline.HOME_TIMELINE) {

                    mTimeline.clear();

                    mTimeline.setTimelineType(Timeline.HOME_TIMELINE);
                    list.setVisibility(View.GONE);
                    crossfade();
                    Log.i("DEBUG", "Updating home timeline...");
                    new LoadTimelineTask(this).execute(mTimeline);
                } else {
                    Toast.makeText(getActivity(),
                            "It's your current timeline!", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            }
            case R.id.switch_user_timeline: {
                if (!App.isOnline(getActivity())) {
                    Log.i("DEBUG", "user timeline button onClick NO NETWORK");
                    Toast.makeText(getActivity(),
                            "No network connection, couldn't load tweets!",
                            Toast.LENGTH_LONG).show();
                    break;
                }
                if (mTimeline.getCurrentTimelineType() != Timeline.USER_TIMELINE) {
                    mTimeline.clear();

                    mTimeline.setTimelineType(Timeline.USER_TIMELINE);
                    list.setVisibility(View.GONE);
                    crossfade();
                    Log.i("DEBUG", "Updating user timeline...");
                    new LoadTimelineTask(this).execute(mTimeline);
                } else {
                    Toast.makeText(getActivity(),
                            "It's your current timeline!", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            }
            case R.id.tweet_send_open: {
                getMainActivity().setMainFragment(new SendTweetFragment());
                break;
            }
            case R.id.logout_button: {
                App app = (App) getActivity().getApplication();

                boolean dbDeleted = getActivity().deleteDatabase(TweetDatabaseOpenHelper
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    public void crossfade() {
        mContentLoaded = !mContentLoaded;

        final View showView = mContentLoaded ? getLoadingBar() : list;
        final View hideView = mContentLoaded ? list : getLoadingBar();

        ViewHelper.setAlpha(list, 0f);

        showView.setVisibility(View.VISIBLE);

        int mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        animate(showView).alpha(1f).setDuration(mShortAnimationDuration)
                .setListener(null);

        animate(hideView).alpha(0f).setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });
    }

    private void blink() {

        ValueAnimator fade = new ValueAnimator();
        fade.setFloatValues(1, 0.3f, 1);
        fade.setDuration(2000);
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

    public TweetAdapter getAdapter() {
        return adapter;
    }

//    public AdapterView<ListAdapter> getAdapterView() {
//        return mAdapter;
//    }
//
//    public void setAdapterView(ListView list) {
//        mAdapter = list;
//    }

    public Timeline getTimeline() {
        return mTimeline;
    }
}
