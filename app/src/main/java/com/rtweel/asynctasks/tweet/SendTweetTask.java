package com.rtweel.asynctasks.tweet;

import java.io.File;
import com.rtweel.cache.App;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class SendTweetTask extends AsyncTask<String, String, Boolean> {

	private final Context mContext;

	public SendTweetTask(Context context) {
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			String accessTokenString = prefs.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
			String accessTokenSecret = prefs.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");

			if (accessTokenString != null && accessTokenSecret != null) {
				AccessToken accessToken = new AccessToken(accessTokenString,
						accessTokenSecret);
				StatusUpdate update = new StatusUpdate(params[0]);
				File file = new File(Environment.getExternalStorageDirectory() //TODO CHANGE TO INTERNAL STORAGE
						+ App.PHOTO_PATH + ".jpg");
				if (file.exists()) {
					update.setMedia(file);
				}

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
        if(mContext != null) {
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
        }  else
            Log.e("Exception", "SendTweetTask lost context");
	}
}