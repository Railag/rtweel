package com.rtweel.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.rtweel.R;
import com.rtweel.asynctasks.timeline.LoadTimelineTask;
import com.rtweel.asynctasks.timeline.TimelineDownTask;
import com.rtweel.asynctasks.timeline.TimelineUpTask;
import com.rtweel.cache.App;
import com.rtweel.sqlite.TweetDatabase;
import com.rtweel.timelines.FavoriteTimeline;
import com.rtweel.timelines.HomeTimeline;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Created by root on 5.4.15.
 */
public class HomeTimelineFragment extends TimelineFragment {

    private TimelineUpTask mUpTask;
    private TimelineDownTask mDownTask;

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).execute(mTimeline);
    }

    @Override
    protected void instantiateTimeline(String username, String screenUserName, long userId) {
        mTimeline = new HomeTimeline(getActivity().getApplicationContext());
        mTimeline.setUserName(username);
        mTimeline.setScreenUserName(screenUserName);
        mTimeline.setUserId(userId);
    }

    protected void updateUp() {

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
        mUpTask = new TimelineUpTask(HomeTimelineFragment.this);
        mUpTask.execute(mTimeline);

    }

    protected void updateDown() {
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
        mDownTask = new TimelineDownTask(HomeTimelineFragment.this);
        mDownTask.execute(mTimeline);
    }

    @Override
    protected void loadingAnim() {

        final View showView = mContentLoaded ? list : getLoadingBar();
        final View hideView = mContentLoaded ? getLoadingBar() : list;
        mContentLoaded = !mContentLoaded;

        showView.setVisibility(View.VISIBLE);

        ObjectAnimator.ofFloat(showView, "alpha", 0f, 1f).setDuration(ANIM_TIME).start();

        ObjectAnimator hideAnim = ObjectAnimator.ofFloat(hideView, "alpha", 1f, 0f).setDuration(ANIM_TIME);
        hideAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                hideView.setVisibility(View.GONE);
            }
        });
        hideAnim.start();

    }
}
