package com.rtweel.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.Timelines.FavoriteTimeline;
import com.rtweel.asynctasks.timeline.LoadTimelineTask;
import com.rtweel.asynctasks.timeline.TimelineDownTask;
import com.rtweel.asynctasks.timeline.TimelineUpTask;
import com.rtweel.cache.App;
import com.rtweel.Timelines.Timeline;

/**
 * Created by root on 6.4.15.
 */
public class FavoriteTimelineFragment extends TimelineFragment {

    private TimelineUpTask mUpTask;
    private TimelineDownTask mDownTask;

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).execute(mTimeline);
    }

    @Override
    protected void instantiateTimeline() {
        mTimeline = new FavoriteTimeline(getActivity().getApplicationContext(), Timeline.FAVORITE_TIMELINE);

        Timeline.setDefaultTimeline(mTimeline);
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
        mUpTask = new TimelineUpTask(FavoriteTimelineFragment.this);
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
        mDownTask = new TimelineDownTask(FavoriteTimelineFragment.this);
        mDownTask.execute(mTimeline);
    }

}
