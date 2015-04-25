package com.rtweel.tasks.tweet;

import android.os.AsyncTask;
import android.util.Log;

import com.rtweel.storage.Tweets;
import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.DetailFragment;
import com.rtweel.fragments.TimelineFragment;
import com.rtweel.fragments.UserTimelineFragment;
import com.rtweel.storage.TweetDatabase;

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
		try {
            Tweets.getTwitter(mFragment.getActivity()).destroyStatus(params[0]);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return params[0];
	}

	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);

        if (mFragment.getActivity() != null) {
            mFragment.getActivity().getContentResolver().delete(
                    TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                    TweetDatabase.Tweets._ID + "="
                            + String.valueOf(result), null);
            mFragment.getActivity().getContentResolver().delete(
                    TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                    TweetDatabase.Tweets._ID + "="
                            + String.valueOf(result), null);

            if (mFragment instanceof DetailFragment) {
                mFragment.getMainActivity().setMainFragment(new UserTimelineFragment());
            } else if (mFragment instanceof TimelineFragment) {
                TimelineFragment timelineFragment = (TimelineFragment) mFragment;
                timelineFragment.getTimeline().remove(mPosition);
                timelineFragment.getAdapter().notifyDataSetChanged();
            }
        } else
            Log.e("Exception", "DeleteTweetTask lost context");

	}
}
