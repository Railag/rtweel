package com.rtweel.asynctasks;

import com.rtweel.tweet.Timeline;

import android.os.AsyncTask;

public class TwitterSearchTask extends AsyncTask<Timeline, Void, Void> {


	@Override
	protected Void doInBackground(Timeline... params) {
		Timeline timeline = params[0];
		
		return null;
	}

}
