package com.rtweel.tasks.tweet;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;

import com.rtweel.Const;
import com.rtweel.MainActivity;
import com.rtweel.fragments.HomeTweetFragment;
import com.rtweel.utils.TwitterUtil;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterGetAccessTokenTask extends
        AsyncTask<String, Void, Boolean> {

    private final MainActivity mActivity;

    public TwitterGetAccessTokenTask(MainActivity mainActivity) {
        mActivity = mainActivity;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        Twitter twitter = TwitterUtil.getInstance().getTwitter();
        RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
        String verifier = params[0];
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mActivity);
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

                return true;

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
            TwitterUtil.getInstance().setTwitterFactory(accessToken);
            return false;
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean isValid) {
        super.onPostExecute(isValid);

        if (mActivity != null) {

            mActivity.hideLoadingBar();

            if (isValid) {
                mActivity.setMainFragment(new HomeTweetFragment());
                mActivity.getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mActivity.getToggle().setDrawerIndicatorEnabled(true);
            }
        }

    }
}