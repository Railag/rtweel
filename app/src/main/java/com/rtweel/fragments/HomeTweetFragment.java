package com.rtweel.fragments;

import android.animation.ObjectAnimator;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.profile.TweetFragment;
import com.rtweel.storage.App;
import com.rtweel.tasks.timeline.LoadTimelineTask;
import com.rtweel.tasks.timeline.TimelineDownTask;
import com.rtweel.tasks.timeline.TimelineUpTask;
import com.rtweel.timelines.HomeTimeline;

/**
 * Created by firrael on 5.4.15.
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

        setRetainInstance(true);
    }

    @Override
    protected void updateUp(Scroll scroll) {

        if (!scroll.equals(Scroll.UPDATE_UP))
            return;

        blink();

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

        Log.i("DEBUG", "SWIPE DOWN");

        if (mDownTask != null)
            if (!mDownTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mDownTask = new TimelineDownTask(HomeTweetFragment.this);
        mDownTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
    }

    @Override
    protected void startAnim() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(list, "alpha", 1f, 0f).setDuration(ANIM_TIME);
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (getActivity() != null)
                    getLoadingBar().setVisibility(View.VISIBLE);
            }
        });

        anim.start();
    }

    @Override
    protected void stopAnim() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(list, "alpha", 0f, 1f).setDuration(ANIM_TIME);
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (getActivity() != null)
                    getLoadingBar().setVisibility(View.GONE);
            }
        });

        anim.start();

    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_home);
    }
}
