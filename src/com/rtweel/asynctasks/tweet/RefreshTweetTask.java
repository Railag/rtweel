package com.rtweel.asynctasks.tweet;

import com.rtweel.tweet.Timeline;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.api.TweetsResources;
import android.os.AsyncTask;

public class RefreshTweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

	@Override
	protected twitter4j.Status doInBackground(Long... params) {
		Twitter twitter = Timeline.getDefaultTimeline().getTwitter();
		TweetsResources tw = twitter.tweets();
		twitter4j.Status result = null;
		try {
			result = tw.showStatus(params[0]);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return result;
	}

}
