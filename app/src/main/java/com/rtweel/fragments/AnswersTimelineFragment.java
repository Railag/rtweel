package com.rtweel.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.tasks.timeline.LoadTimelineTask;
import com.rtweel.tasks.timeline.TimelineDownTask;
import com.rtweel.tasks.timeline.TimelineUpTask;
import com.rtweel.storage.App;
import com.rtweel.timelines.AnswersTimeline;

/**
 * Created by root on 6.4.15.
 */
public class AnswersTimelineFragment extends TimelineFragment {

    private TimelineUpTask mUpTask;
    private TimelineDownTask mDownTask;

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).execute(mTimeline);
    }

    @Override
    protected void instantiateTimeline(String username, String screenUserName, long userId) {
        mTimeline = new AnswersTimeline(getActivity().getApplicationContext());
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
        mUpTask = new TimelineUpTask(AnswersTimelineFragment.this);
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
        mDownTask = new TimelineDownTask(AnswersTimelineFragment.this);
        mDownTask.execute(mTimeline);
    }

    @Override
    protected void loadingAnim() {
        //TODO
    }
}
