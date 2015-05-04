package com.rtweel.tasks.tweet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.rtweel.Const;
import com.rtweel.storage.Tweets;
import com.rtweel.utils.TwitterUtil;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterGetAccessTokenTask extends
        AsyncTask<String, String, String> {

    private final Context mContext;

    public TwitterGetAccessTokenTask(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {

        Twitter twitter = Tweets.getTwitter(mContext);
        RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
        String verifier = params[0];
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        if (verifier != null) {
            try {

                AccessToken accessToken = twitter.getOAuthAccessToken(
                        requestToken, verifier);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Const.PREFERENCE_TWITTER_OAUTH_TOKEN,
                        accessToken.getToken());
                editor.putString(
                        Const.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET,
                        accessToken.getTokenSecret());
                editor.putBoolean(
                        Const.PREFERENCE_TWITTER_IS_LOGGED_IN, true);
                editor.commit();
                return twitter.showUser(accessToken.getUserId()).getName();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {

            String accessTokenString = prefs.getString(
                    Const.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
            String accessTokenSecret = prefs.getString(
                    Const.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");

            AccessToken accessToken = new AccessToken(accessTokenString,
                    accessTokenSecret);
            try {
                TwitterUtil.getInstance().setTwitterFactory(accessToken);
                return Tweets.getTwitter(mContext)
                        .showUser(accessToken.getUserId()).getName();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
