package com.rtweel.asynctasks.tweet;

import android.os.AsyncTask;

import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.DetailFragment;
import com.rtweel.fragments.TimelineFragment;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;

import twitter4j.TwitterException;

public class DeleteTweetTask extends AsyncTask<Long, Void, Long> {

	private final BaseFragment mFragment;
	private final int mPosition;

	public DeleteTweetTask(BaseFragment fragment, int position) {
		mFragment = fragment;
		mPosition = position;
	}

	@Override
	protected Long doInBackground(Long... params) {
		Timeline timeline = Timeline.getDefaultTimeline();
		try {
			timeline.getTwitter().destroyStatus(params[0]);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return params[0];
	}

	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);

        mFragment.getActivity().getContentResolver().delete(
                TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
                TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "="
                        + String.valueOf(result), null);
        mFragment.getActivity().getContentResolver().delete(
                TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
                TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "="
                        + String.valueOf(result), null);
        Timeline.getDefaultTimeline().remove(mPosition);

		if (mFragment instanceof DetailFragment) {
			mFragment.getMainActivity().setMainFragment(new TimelineFragment());
		} else if (mFragment instanceof TimelineFragment) {
			TimelineFragment timelineFragment = (TimelineFragment) mFragment;
			timelineFragment.getAdapter().notifyDataSetChanged();
		}

	}
}
