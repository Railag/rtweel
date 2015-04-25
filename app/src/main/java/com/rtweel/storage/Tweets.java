package com.rtweel.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rtweel.tasks.tweet.GetScreenNameTask;
import com.rtweel.Const;
import com.rtweel.utils.TwitterUtil;

import java.util.ArrayList;

import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

/**
 * Created by root on 10.4.15.
 */


public class Tweets {
    private static Twitter sTwitter;

    public static String[] getProjection() {
        return getProjection(false);
    }

    public static String[] getProjection(boolean withMedia) {
        ArrayList<String> projection = new ArrayList<>();

        projection.add(TweetDatabase.Tweets.COLUMN_AUTHOR);
        projection.add(TweetDatabase.Tweets.COLUMN_TEXT);
        projection.add(TweetDatabase.Tweets.COLUMN_PICTURE);
        projection.add(TweetDatabase.Tweets.COLUMN_DATE);
        projection.add(TweetDatabase.Tweets._ID);

        if (withMedia)
            projection.add(TweetDatabase.Tweets.COLUMN_MEDIA);

        return projection.toArray(new String[projection.size()]);
    }

    public static Twitter getTwitter(Context context) {
        if (sTwitter != null)
            return sTwitter;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String accessTokenString = prefs.getString(Const.PREFERENCE_TWITTER_OAUTH_TOKEN, null);
        String accessTokenSecret = prefs.getString(Const.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, null);

        if (accessTokenString != null && accessTokenSecret != null) {
            AccessToken accessToken = new AccessToken(accessTokenString,
                    accessTokenSecret);
            sTwitter = TwitterUtil.getInstance().getTwitterFactory()
                    .getInstance(accessToken);
            sTwitter.addRateLimitStatusListener(new RateLimitStatusListener() {
                @Override
                public void onRateLimitStatus(RateLimitStatusEvent event) {
                    Log.i("LIMIT", "limit = " + event.getRateLimitStatus().getLimit());
                    Log.i("LIMIT", "remaining: " + event.getRateLimitStatus().getRemaining());
                    Log.i("LIMIT", "secondsUntilReset: " + event.getRateLimitStatus().getSecondsUntilReset());
                }

                @Override
                public void onRateLimitReached(RateLimitStatusEvent event) {
                    Log.i("LIMIT", "BLOCKED");
                }
            });
        }

        new GetScreenNameTask(context, null, null).execute(sTwitter);

        return sTwitter;
    }


}
