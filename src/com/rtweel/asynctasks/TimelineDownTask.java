package com.rtweel.asynctasks;

import java.util.List;

import com.rtweel.activities.MainActivity;
import com.rtweel.tweet.Timeline;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TimelineDownTask extends AsyncTask<Timeline, Void, Integer> {

	private MainActivity mActivity;

	public TimelineDownTask(MainActivity mainActivity) {
		mActivity = mainActivity;
	}

	@Override
	protected Integer doInBackground(Timeline... params) {
		Timeline timeline = params[0];
		List<twitter4j.Status> downloadedList = timeline.downloadTimeline(
				Timeline.getTweetsCount(), Timeline.getTweetsPerPage(),
				Timeline.DOWN_TWEETS);
		timeline.updateTimelineDown(downloadedList);
		Log.i("DEBUG", "finished updating");
		return downloadedList.size();
	}

	@Override
	protected void onPostExecute(Integer result) {

		mActivity.getAdapter().notifyDataSetChanged();

		mActivity.crossfade();

		if (result != 0) {
			Toast.makeText(mActivity, "New tweets: " + result,
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mActivity, "No new tweets", Toast.LENGTH_LONG)
					.show();
		}
	}
}
