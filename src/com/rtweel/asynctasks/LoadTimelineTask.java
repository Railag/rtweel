package com.rtweel.asynctasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.activities.MainActivity;
import com.rtweel.tweet.Timeline;

public class LoadTimelineTask extends AsyncTask<Timeline, Void, Void> {

	private MainActivity mActivity;

	private Timeline mTimeline;

	public LoadTimelineTask(MainActivity mainActivity) {
		mActivity = mainActivity;
	}

	@Override
	protected Void doInBackground(Timeline... params) {
		mTimeline = params[0];
		mTimeline.loadTimeline();
		Log.i("DEBUG", "finished updating");
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {

		if (mTimeline.getTweets().isEmpty()) {
			Log.i("DEBUG", "OnPostExecute loadtimeline NO NETWORK");
			Toast.makeText(mActivity,
					"No network connection, couldn't load tweets!",
					Toast.LENGTH_LONG).show();
		} else {

			mActivity.getAdapter().notifyDataSetChanged();

			mActivity.crossfade();
		}
	}
}
