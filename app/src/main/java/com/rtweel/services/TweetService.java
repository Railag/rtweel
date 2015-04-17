package com.rtweel.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.rtweel.asynctasks.db.Tweets;
import com.rtweel.cache.App;
import com.rtweel.timelines.HomeTimeline;
import com.rtweel.timelines.Timeline;

import java.util.List;

import twitter4j.Status;

public class TweetService extends IntentService {

    public static final String MESSAGE = "message";

    public static final String TITLE = "title";

    private static int sNewTweets = 0;

    private Timeline mTimeline;

    public TweetService() {
        super("TweetService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimeline = new HomeTimeline(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent data) {
        if (!App.isOnline(getApplicationContext())) {
            return;
        }

        String message = null;
        Log.i("DEBUG", "onHandleIntent");
        loadFromDB();
        try {
            List<twitter4j.Status> downloadedList = mTimeline.downloadTimeline(
                    Timeline.UP_TWEETS);
            mTimeline.updateTimelineUpDb(downloadedList);

            int size = downloadedList.size();
            Log.i("DEBUG", "Size: " + String.valueOf(size));
            if (size > 0) {
                sNewTweets += size;

                message = "Tweets downloaded: " + sNewTweets;
                String title = "Tweet checking";

                Intent localIntent = new Intent(TweetReceiver.BROADCAST_ACTION)
                        .putExtra(MESSAGE, message);
                localIntent.putExtra(TITLE, title);
                sendBroadcast(localIntent);

            } else {
                message = "No new tweets";
            }
            Log.i("DEBUG", message);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("DEBUG", "NPE in service");
        }
    }

    private void loadFromDB() {

        String[] projection = Tweets.getProjection();

        ContentResolver resolver = getContentResolver();

        Cursor cursor = mTimeline.getNewestTweet(resolver, projection);

        List<Status> tweets = Timeline.buildTweets(cursor, true);
        mTimeline.getTweets().addAll(tweets);
    }

    public static void setNewTweets(int count) {
        sNewTweets = count;
    }
}
