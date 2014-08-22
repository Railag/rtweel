package com.rtweel.asynctasks.tweet;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.rtweel.tweet.Timeline;

import android.os.AsyncTask;

public class GetScreenNameTask extends AsyncTask<Twitter, Void, Void> {

	@Override
	protected Void doInBackground(Twitter... params) {
		try {
			Timeline.setScreenUserName(params[0].getScreenName());
			Timeline.setUserName(params[0].showUser(
					Timeline.getScreenUserName()).getName());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

}
