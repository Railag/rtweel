package com.rtweel.tasks.timeline;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.profile.TweetFragment;
import com.rtweel.timelines.Timeline;

import java.util.List;

public class TimelineUpTask extends AsyncTask<Timeline, Void, Integer> {

    private TweetFragment mFragment;

    public TimelineUpTask(TweetFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected Integer doInBackground(Timeline... params) {
        Timeline timeline = params[0];
        List<twitter4j.Status> downloadedList = null;
        try {
            downloadedList = timeline.downloadTimeline(Timeline.UP_TWEETS);
        } catch (Exception e) {
            cancel(true);
        }

        if (downloadedList == null) {
            cancel(true);
        }

        timeline.updateTimelineUpDb(downloadedList);

        int size = downloadedList.size();
        Log.i("DEBUG", "Finished downloading up task");
        return size;
    }

    @Override
    protected void onPostExecute(Integer result) {
        mFragment.getAdapter().notifyDataSetChanged();

        // mActivity.startAnim();
        // Toast.makeText(mActivity, "Finished", Toast.LENGTH_LONG).show();

        if (mFragment.getActivity() != null) {
            Log.i("PB", mFragment.getClass().getSimpleName() + " end hide dialog");
            mFragment.hideProgressBar();
            if (result == 0)
                return;

            if (mFragment.getTimeline().isHomeTimeline()) {

                if (result < (100 - 3)) {
                    Toast.makeText(mFragment.getActivity(), mFragment.getString(R.string.new_tweets) + result,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(
                            mFragment.getActivity(),
                            mFragment.getString(R.string.new_tweets)
                                    + result
                                    + mFragment.getString(R.string.unloaded_tweets),
                            Toast.LENGTH_LONG).show();
                }
            }
        } else
            Log.e("Exception", "TimelineUpTask lost context");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i("PB", mFragment.getClass().getSimpleName() + " cancelled hide dialog");
        mFragment.hideProgressBar();
    }

}
