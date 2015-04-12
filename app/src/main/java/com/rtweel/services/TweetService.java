package com.rtweel.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.rtweel.asynctasks.db.Tweets;
import com.rtweel.cache.App;
import com.rtweel.sqlite.TweetDatabase;
import com.rtweel.timelines.HomeTimeline;
import com.rtweel.timelines.Timeline;

import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class TweetService extends IntentService {

    public static final String MESSAGE = "message";

    public static final String TITLE = "title";

    private static int sNewTweets = 0;

    private Timeline mTimeline;

    public TweetService() {
        super("TweetService");
    }

    public TweetService(String name, int type) {
        super(name);
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
        //loadFromDB();
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

        Cursor cursor = null;
        if (mTimeline.getCurrentTimelineType() == Timeline.HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                    projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
        } else if (mTimeline.getCurrentTimelineType() == Timeline.USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_USER_DB,
                    projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String author = cursor.getString(cursor
                        .getColumnIndex(projection[0]));
                String text = cursor.getString(cursor
                        .getColumnIndex(projection[1])).replace("\\n", "\n");
                String pictureUrl = cursor.getString(cursor
                        .getColumnIndex(projection[2]));
                String date = cursor.getString(cursor
                        .getColumnIndex(projection[3]));
                long id = cursor.getLong(cursor.getColumnIndex(projection[4]));

                try {
                    String creation = "{text='" + text + "', id='" + id
                            + "', created_at='" + date
                            + "',user={name='" + author
                            + "', profile_image_url='" + pictureUrl + "'}}";
                    Status insert = TwitterObjectFactory.createStatus(creation);
                    mTimeline.getTweets().add(insert);
                } catch (TwitterException e1) {
                    e1.printStackTrace();
                }

            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void setNewTweets(int count) {
        sNewTweets = count;
    }
}
