package com.rtweel.asynctasks.tweet;

import com.rtweel.activities.DetailActivity;
import com.rtweel.activities.MainActivity;
import com.rtweel.constant.Extras;
import com.rtweel.tweet.Timeline;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.api.TweetsResources;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class RefreshTweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

    private Context mContext;
    private Long mId;
    private int mPosition;

    public RefreshTweetTask(Context context, int position) {
        mContext = context;
        mPosition = position;
    }


    @Override
    protected twitter4j.Status doInBackground(Long... params) {
        mId = params[0];
        Twitter twitter = Timeline.getDefaultTimeline().getTwitter();
        TweetsResources tw = twitter.tweets();
        twitter4j.Status result = null;
        try {
            result = tw.showStatus(mId);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(twitter4j.Status result) {
        super.onPostExecute(result);
        MainActivity activity = (MainActivity) mContext;
        if (result != null) {
            if(activity.isLoading())
                activity.stopLoading();
            Intent intent = new Intent(activity,
                    DetailActivity.class);
            intent.putExtra(Extras.TWEET, result);
            intent.putExtra(Extras.POSITION, mPosition);
            activity.startActivityForResult(intent, MainActivity.DELETE_REQUEST);
        } else {
            new DeleteTweetTask(activity,
                    DeleteTweetTask.MAIN, mPosition).execute(mId);
        }
    }
}
