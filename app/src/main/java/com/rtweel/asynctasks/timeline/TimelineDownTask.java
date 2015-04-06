package com.rtweel.asynctasks.timeline;

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.rtweel.fragments.TimelineFragment;
import com.rtweel.Timelines.Timeline;

public class TimelineDownTask extends AsyncTask<Timeline, Void, Integer> {

	private TimelineFragment mFragment;

	public TimelineDownTask(TimelineFragment fragment) {
		mFragment = fragment;
	}

	@Override
	protected void onPreExecute() {

		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(Timeline... params) {
		Timeline timeline = params[0];
		List<twitter4j.Status> downloadedList = null;
		int size = timeline.updateFromDb();
		if (size == 0) {
			downloadedList = timeline.downloadTimeline(
				//	Timeline.getTweetsCount(), Timeline.getTweetsPerPage(),
					Timeline.DOWN_TWEETS);
			timeline.updateTimelineDown(downloadedList);
		
		Log.i("DEBUG", "finished updating");
		return downloadedList.size();
		} else {
			return size;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {

		mFragment.getAdapter().notifyDataSetChanged();

		// mActivity.crossfade();
/*
		if (result != 0) {
			Toast.makeText(mFragment.getActivity(), "New tweets: " + result,
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mFragment.getActivity(), "No new tweets", Toast.LENGTH_LONG)
					.show();
		}
*/
	}
}
