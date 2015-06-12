package com.rtweel.tasks.timeline;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rtweel.profile.TweetFragment;
import com.rtweel.timelines.Timeline;

public class LoadTimelineTask extends AsyncTask<Timeline, Void, Void> {

	private TweetFragment mFragment;

	private Timeline mTimeline;

	public LoadTimelineTask(TweetFragment fragment) {
		mFragment = fragment;
	}

	@Override
	protected Void doInBackground(Timeline... params) {
		Log.i("DEBUG", "loadtimelinetask started");
		mTimeline = params[0];
		mTimeline.loadTimeline();

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
        if(mFragment.getActivity() != null) {

            if (mTimeline.getTweets().isEmpty()) {
                Log.i("DEBUG", "OnPostExecute loadtimeline NO NETWORK");
                Toast.makeText(mFragment.getActivity(),
                        "No network connection, couldn't load tweets!",
                        Toast.LENGTH_LONG).show();
            } else {
                mFragment.getAdapter().notifyDataSetChanged();

                Log.i("Anim", "anim from loadtimelinetask");
                mFragment.stopLoadingAnim();
            }
        } else
            Log.e("Exception", "LoadTimelineTask lost context");
	}
}
