package com.rtweel.tasks.timeline;

import java.util.List;

import com.rtweel.fragments.TweetFragment;
import com.rtweel.timelines.Timeline;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TimelineUpTask extends AsyncTask<Timeline, Void, Integer> {

    private TweetFragment mFragment;

    public TimelineUpTask(TweetFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Integer doInBackground(Timeline... params) {
        Timeline timeline = params[0];
        List<twitter4j.Status> downloadedList = timeline.downloadTimeline(Timeline.UP_TWEETS);

        if (downloadedList == null)
            cancel(true);

        timeline.updateTimelineUpDb(downloadedList);

        int size = downloadedList.size();
        Log.i("DEBUG", "Finished downloading up task");
        return size;
    }

    @Override
    protected void onPostExecute(Integer result) {
        mFragment.getAdapter().notifyDataSetChanged();

        // mActivity.loadingAnim();
        // Toast.makeText(mActivity, "Finished", Toast.LENGTH_LONG).show();

        if (mFragment.getActivity() != null) {
            if (result == 0) {
                Toast.makeText(mFragment.getActivity(), "No new tweets", Toast.LENGTH_LONG)
                        .show();

            } else {
                if (result < (100 - 3)) {
                    Toast.makeText(mFragment.getActivity(), "New tweets: " + result,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(
                            mFragment.getActivity(),
                            "New tweets: "
                                    + result
                                    + "\n There are unloaded new tweets, you can make up swipe one more time",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else
            Log.e("Exception", "TimelineUpTask lost context");
    }
}
