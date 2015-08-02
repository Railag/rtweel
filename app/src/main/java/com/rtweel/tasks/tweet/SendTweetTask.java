package com.rtweel.tasks.tweet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.rtweel.MainActivity;
import com.rtweel.R;
import com.rtweel.storage.AppUser;
import com.rtweel.storage.TweetDatabase;
import com.rtweel.storage.Tweets;

import java.io.File;
import java.util.Date;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SendTweetTask extends AsyncTask<String, String, Boolean> {

    private final Context mContext;
    private final long mReplyId;
    private final long mLastTweetId;

    public SendTweetTask(Context context, long replyId, long lastTweetId) {
        mContext = context;
        mReplyId = replyId;
        mLastTweetId = lastTweetId;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Twitter twitter = Tweets.getTwitter(mContext);

        if (mLastTweetId != -1L) {
            try {
                twitter.destroyStatus(mLastTweetId);
                mContext.getContentResolver().delete(
                        TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                        TweetDatabase.Tweets._ID + "="
                                + String.valueOf(mLastTweetId), null);
                mContext.getContentResolver().delete(
                        TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                        TweetDatabase.Tweets._ID + "="
                                + String.valueOf(mLastTweetId), null);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        StatusUpdate update = new StatusUpdate(params[0]);

        if (!TextUtils.isEmpty(params[1])) {
            File file = new File(params[1]);
            if (file.exists()) {
                update.setMedia(file);
            }
        }



        if (mReplyId != -1L)
            update.setInReplyToStatusId(mReplyId);

        try {
            twitter4j.Status tweet = twitter.updateStatus(update);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(AppUser.APP_USER_LAST_TWEET_ID, tweet.getId());
            editor.putLong(AppUser.APP_USER_LAST_TWEET_TIME, new Date().getTime());
            editor.commit();

            AppUser.setLastTweetId(tweet.getId());
            AppUser.setLastTweetTime(new Date().getTime());
            MainActivity mActivity = (MainActivity) mContext;
            mActivity.showLastTweetButton();

            return true;
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return false;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mContext != null) {
            if (result) {
                Toast toast = Toast.makeText(mContext, mContext.getString(R.string.tweet_send_success),
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                Toast toast = Toast.makeText(mContext, mContext.getString(R.string.tweet_send_failed),
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else
            Log.e("Exception", "SendTweetTask lost context");
    }
}