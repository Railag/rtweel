package com.rtweel.asynctasks.timeline;

import java.util.List;

import com.rtweel.activities.MainActivity;
import com.rtweel.fragments.TimelineFragment;
import com.rtweel.tweet.Timeline;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TimelineUpTask extends AsyncTask<Timeline, Void, Integer> {

	private TimelineFragment mFragment;

	public TimelineUpTask(TimelineFragment fragment) {
		mFragment = fragment;
	}

	@Override
	protected Integer doInBackground(Timeline... params) {
		Timeline timeline = params[0];
		List<twitter4j.Status> downloadedList = timeline.downloadTimeline(//0,
	//			Timeline.getTweetsPerPage(),
				Timeline.UP_TWEETS);
		// boolean available;
		// try {
		// available = timeline.searchCheckIsAvailable(timeline
		// .getTwitter().getAccountSettings().getScreenName());
		// if (available) {
		// downloadedList.addAll(timeline.downloadTimeline(0,
		// Timeline.getTweetsPerPage(), Timeline.UP_TWEETS));
		// }
		// } catch (TwitterException e) {
		// e.printStackTrace();
		// }
		timeline.updateTimelineUp(downloadedList);

		int size = downloadedList.size();
		Log.i("DEBUG", "Finished downloading down task");
		return size;
	}

	@Override
	protected void onPostExecute(Integer result) {
		mFragment.getAdapter().notifyDataSetChanged();
		// adapter.notifyDataSetInvalidated();
		// mActivity.crossfade();
		// Toast.makeText(mActivity, "Finished", Toast.LENGTH_LONG).show();

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
								+ "\n There are unloaded new tweets, you can make right swipe one more time",
						Toast.LENGTH_LONG).show();
			}
		}

		// super.onPostExecute(result);
	}
}
