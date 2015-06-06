package com.rtweel.fragments;

import android.animation.ObjectAnimator;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.profile.TweetFragment;
import com.rtweel.tasks.timeline.LoadTimelineTask;
import com.rtweel.tasks.timeline.TimelineDownTask;
import com.rtweel.tasks.timeline.TimelineUpTask;
import com.rtweel.storage.App;
import com.rtweel.timelines.HomeTimeline;

/**
 * Created by root on 5.4.15.
 */
public class HomeTweetFragment extends TweetFragment {

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
    }

    @Override
    protected void instantiateListData(String username, String screenUserName, long userId) {
        mTimeline = new HomeTimeline(getActivity().getApplicationContext());
        mTimeline.setUserName(username);
        mTimeline.setScreenUserName(screenUserName);
        mTimeline.setUserId(userId);
    }

    @Override
    public void onStart() {
        super.onStart();
        setTitle(getString(R.string.title_home));
    }

    @Override
    protected void updateUp(Scroll scroll) {

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

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
        mUpTask = new TimelineUpTask(HomeTweetFragment.this);
        mUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);

    }

    @Override
    protected void updateDown(Scroll scroll) {

        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

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
        mDownTask = new TimelineDownTask(HomeTweetFragment.this);
        mDownTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
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
