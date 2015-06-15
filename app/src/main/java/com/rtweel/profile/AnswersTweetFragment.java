package com.rtweel.profile;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.storage.AppUser;
import com.rtweel.tasks.timeline.LoadTimelineTask;
import com.rtweel.tasks.timeline.TimelineDownTask;
import com.rtweel.tasks.timeline.TimelineUpTask;
import com.rtweel.storage.App;
import com.rtweel.timelines.AnswersTimeline;

/**
 * Created by firrael on 6.4.15.
 */
public class AnswersTweetFragment extends TweetFragment {

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
    }

    @Override
    protected void instantiateListData(String username, String screenUserName, long userId) {
        mTimeline = new AnswersTimeline(getActivity().getApplicationContext());
        mTimeline.setUserName(username);
        mTimeline.setScreenUserName(screenUserName);
        mTimeline.setUserId(userId);
    }

    @Override
    protected void updateUp(Scroll scroll) {
        super.updateUp(scroll);

        if (!scroll.equals(Scroll.UPDATE_UP)) {
            return;
        }

        if (getUserId() != AppUser.getUserId(getActivity())) {
            hideProgressBar();
            return;
        }

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
        mUpTask = new TimelineUpTask(AnswersTweetFragment.this);
        mUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);

    }

    @Override
    protected void updateDown(Scroll scroll) {
        super.updateDown(scroll);

        if (!scroll.equals(Scroll.UPDATE_DOWN))
            return;

        if (getUserId() != AppUser.getUserId(getActivity())) {
            hideProgressBar();
            return;
        }

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
        mDownTask = new TimelineDownTask(AnswersTweetFragment.this);
        mDownTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTimeline);
    }

    @Override
    protected void startAnim() {
        showProgressBar();
    }

    @Override
    protected void stopAnim() {
        hideProgressBar();
    }
}
