package com.rtweel.twitteroauth;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class TwitterGetAccessTokenTask extends
		AsyncTask<String, String, String> {

	private final Context sContext;

	public TwitterGetAccessTokenTask(Context context) {
		sContext = context;
	}

	@Override
	protected String doInBackground(String... params) {

		Twitter twitter = TwitterUtil.getInstance().getTwitter();
		RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
		String verifier = params[0];
		if (verifier != null) {
			try {

				AccessToken accessToken = twitter.getOAuthAccessToken(
						requestToken, verifier);

				SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(sContext);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN,
						accessToken.getToken());
				editor.putString(
						ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET,
						accessToken.getTokenSecret());
				editor.putBoolean(
						ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, true);
				editor.commit();
				return twitter.showUser(accessToken.getUserId()).getName();
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		} else {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(sContext);
			String accessTokenString = sharedPreferences.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
			String accessTokenSecret = sharedPreferences.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
			AccessToken accessToken = new AccessToken(accessTokenString,
					accessTokenSecret);
			try {
				TwitterUtil.getInstance().setTwitterFactory(accessToken);
				return TwitterUtil.getInstance().getTwitter()
						.showUser(accessToken.getUserId()).getName();
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
