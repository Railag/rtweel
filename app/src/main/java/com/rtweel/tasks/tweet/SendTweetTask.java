package com.rtweel.tasks.tweet;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.storage.App;
import com.rtweel.storage.Tweets;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SendTweetTask extends AsyncTask<String, String, Boolean> {

    private final Context mContext;
    private final long mReplyId;

    public SendTweetTask(Context context, long replyId) {
        mContext = context;
        mReplyId = replyId;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        StatusUpdate update = new StatusUpdate(params[0]);
//        File file = new File(Environment.getExternalStorageDirectory()
//                + App.PHOTO_PATH + ".jpg");
        File file = new File(params[1]);
        if (file.exists()) {
            update.setMedia(file);
        }


        Twitter twitter = Tweets.getTwitter(mContext);
        if (mReplyId != -1L)
            update.setInReplyToStatusId(mReplyId);

        try {
            twitter4j.Status tweet = twitter.updateStatus(update);
            Tweets.saveLatestTweet(tweet);
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