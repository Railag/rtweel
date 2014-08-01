package com.rtweel.asynctasks;

import java.util.List;

import com.rtweel.activities.MainActivity;
import com.rtweel.tweet.Timeline;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TimelineUpTask extends AsyncTask<Timeline, Void, Integer> {

	private MainActivity mActivity;

	public TimelineUpTask(MainActivity mainActivity) {
		mActivity = mainActivity;
	}

	@Override
	protected Integer doInBackground(Timeline... params) {
		Timeline timeline = params[0];
		List<twitter4j.Status> downloadedList = timeline.downloadTimeline(0,
				Timeline.getTweetsPerPage(), Timeline.UP_TWEETS);
		timeline.updateTimelineUp(downloadedList);
		Log.i("DEBUG", "finished updating");
		return downloadedList.size();
	}

	@Override
	protected void onPostExecute(Integer result) {
		mActivity.getAdapter().notifyDataSetChanged();
		// adapter.notifyDataSetInvalidated();
		mActivity.crossfade();
		// Toast.makeText(mActivity, "Finished", Toast.LENGTH_LONG).show();

		if (result == 0) {
			Toast.makeText(mActivity, "No new tweets", Toast.LENGTH_LONG)
			.show();
			
		} else {
			if(result < (Timeline.getTweetsPerPage() - 3) ) {
				Toast.makeText(mActivity, "New tweets: " + result,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(mActivity, "New tweets: " + result + "\n There are unloaded new tweets, you can make right swipe one more time", Toast.LENGTH_LONG).show();
			}
		}

		// super.onPostExecute(result);
	}
}
