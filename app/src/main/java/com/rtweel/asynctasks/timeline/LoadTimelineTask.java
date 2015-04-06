package com.rtweel.asynctasks.timeline;

import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.fragments.TimelineFragment;
import com.rtweel.Timelines.Timeline;

public class LoadTimelineTask extends AsyncTask<Timeline, Void, Void> {

	private TimelineFragment mFragment;

	private Timeline mTimeline;

	public LoadTimelineTask(TimelineFragment fragment) {
		mFragment = fragment;
	}

	@Override
	protected Void doInBackground(Timeline... params) {
		Date t1 = new Date();
		Log.i("DEBUG", "loadtimelinetask started");
		mTimeline = params[0];
		mTimeline.loadTimeline();
		Date t2 = new Date();
		Log.i("DEBUG",
				"Finished timeline task in " + (t2.getTime() - t1.getTime()));
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {

		if (mTimeline.getTweets().isEmpty()) {
			Log.i("DEBUG", "OnPostExecute loadtimeline NO NETWORK");
			Toast.makeText(mFragment.getActivity(),
					"No network connection, couldn't load tweets!",
					Toast.LENGTH_LONG).show();
		} else {
			mFragment.getAdapter().notifyDataSetChanged();

			mFragment.crossfade();
		}
	}
}
