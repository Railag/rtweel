package com.rtweel.asynctasks;

import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class TwitterSendTweetTask extends AsyncTask<String, String, Boolean> {

	private final Context sContext;

	public TwitterSendTweetTask(Context context) {
		sContext = context;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(sContext);
			String accessTokenString = sharedPreferences.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
			String accessTokenSecret = sharedPreferences.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");

			if (accessTokenString != null && accessTokenSecret != null) {
				AccessToken accessToken = new AccessToken(accessTokenString,
						accessTokenSecret);
				TwitterUtil.getInstance().getTwitterFactory()
						.getInstance(accessToken).updateStatus(params[0]);
				return true;
			}

		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return false;

	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result)
			Toast.makeText(sContext, "Tweet successfully", Toast.LENGTH_SHORT)
					.show();
		else
			Toast.makeText(sContext, "Tweet failed", Toast.LENGTH_SHORT).show();
	}
}