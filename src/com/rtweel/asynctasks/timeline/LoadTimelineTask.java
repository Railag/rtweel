package com.rtweel.asynctasks.timeline;

import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.activities.MainActivity;
import com.rtweel.tweet.Timeline;

public class LoadTimelineTask extends AsyncTask<Timeline, Void, Void> {

	private MainActivity mActivity;

	private Timeline mTimeline;

	public LoadTimelineTask(MainActivity mainActivity) {
		Log.i("DEBUG", "construction");
		mActivity = mainActivity;
	}
	
	@Override
	protected void onPreExecute() {
		Log.i("DEBUG", "onPreExecute");
	}

	@Override
	protected Void doInBackground(Timeline... params) {
		Date t1 = new Date();
		Log.i("DEBUG", "loadtimelinetask started");
		mTimeline = params[0];
		mTimeline.loadTimeline();
		Date t2 = new Date();
		Log.i("DEBUG", "Finished timeline task in " + (t2.getTime() - t1.getTime()));
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
