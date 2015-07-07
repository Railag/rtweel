package com.rtweel.tasks.tweet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.rtweel.storage.App;
import com.rtweel.Const;
import com.rtweel.storage.Tweets;
import com.rtweel.utils.TwitterUtil;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class SendTweetTask extends AsyncTask<String, String, Boolean> {

    private final Context mContext;
    private final long mReplyId;

    public SendTweetTask(Context context, long replyId) {
        mContext = context; mReplyId = replyId;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            String accessTokenString = prefs.getString(
                    Const.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
            String accessTokenSecret = prefs.getString(
                    Const.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");

            if (accessTokenString != null && accessTokenSecret != null) {
                AccessToken accessToken = new AccessToken(accessTokenString,
                        accessTokenSecret);
                StatusUpdate update = new StatusUpdate(params[0]);
                File file = new File(Environment.getExternalStorageDirectory() //TODO CHANGE TO INTERNAL STORAGE
                        + App.PHOTO_PATH + ".jpg");
                if (file.exists()) {
                    update.setMedia(file);
                }

                if (mReplyId != -1L)
                    update.setInReplyToStatusId(mReplyId);

                TwitterUtil.getInstance().getTwitterFactory()
                        .getInstance(accessToken).updateStatus(update);
                return true;
            }

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mContext != null) {
            if (result) {
                Toast toast = Toast.makeText(mContext, "Tweet successfully sended",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                Toast toast = Toast.makeText(mContext, "Tweet sending failed",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else
            Log.e("Exception", "SendTweetTask lost context");
    }
}