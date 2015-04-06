package com.rtweel.asynctasks.tweet;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.rtweel.timelines.Timeline;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.Map;

public class GetScreenNameTask extends AsyncTask<Twitter, Void, Void> {

    @Override
    protected Void doInBackground(Twitter... params) {
        Twitter twitter = params[0];
        try {
            Map<String, RateLimitStatus> rates = null;
            try {
                rates =  twitter.getRateLimitStatus();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(Timeline.getUserName()))
                Timeline.setScreenUserName(twitter.getScreenName());

            if (TextUtils.isEmpty(Timeline.getScreenUserName()))
                Timeline.setUserName(twitter.showUser(
                        Timeline.getScreenUserName()).getName());

        } catch (IllegalStateException | TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
