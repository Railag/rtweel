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
                Timeline.setScreenUserName(twitter.getScreenName());

                Timeline.setUserName(twitter.showUser(
                        Timeline.getScreenUserName()).getName());

        } catch (IllegalStateException | TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
