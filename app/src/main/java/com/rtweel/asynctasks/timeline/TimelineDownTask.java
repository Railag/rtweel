package com.rtweel.asynctasks.timeline;

import android.os.AsyncTask;
import android.util.Log;

import com.rtweel.fragments.TimelineFragment;
import com.rtweel.timelines.Timeline;

import java.util.List;

public class TimelineDownTask extends AsyncTask<Timeline, Void, Void> {

    private TimelineFragment mFragment;

    public TimelineDownTask(TimelineFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Void doInBackground(Timeline... params) {
        Timeline timeline = params[0];
        List<twitter4j.Status> downloadedList;
        int size = timeline.updateFromDb();
        if (size == 0) {
            downloadedList = timeline.downloadTimeline(
                    Timeline.DOWN_TWEETS);
            timeline.updateTimelineDown(downloadedList);

            Log.i("DEBUG", "finished updating down ");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        mFragment.getAdapter().notifyDataSetChanged();

    }
}
