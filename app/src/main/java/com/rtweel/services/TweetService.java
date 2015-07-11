package com.rtweel.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.rtweel.R;
import com.rtweel.fragments.SettingsFragment;
import com.rtweel.storage.App;
import com.rtweel.storage.Tweets;
import com.rtweel.timelines.HomeTimeline;
import com.rtweel.timelines.Timeline;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class TweetService extends IntentService {

    public static final String MESSAGE = "message";
    public static final String TITLE = "title";
    public static final String TYPE = "type";

    private static int sNewTweets = 0;

    private Timeline mTimeline;

    public enum PN {
        MESSAGE,
        MENTION,
        UPDATE
    }

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
        if (!App.isOnline(getApplicationContext()))
            return;

        loadFromDB();

        Paging paging = new Paging();
        paging.setCount(1);


        //check for new direct message
        try {
            ResponseList<DirectMessage> messages = Tweets.getTwitter(this).getDirectMessages(paging);
            if (messages != null && messages.size() > 0 && isNew(messages.get(0).getCreatedAt())) {
                DirectMessage message = messages.get(0);
                sendPN(PN.MESSAGE, message.getText(), getString(R.string.pn_dmessage_title));
                return;
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }


        //check for new mentions

        try {
            ResponseList<Status> mentions = Tweets.getTwitter(this).getMentionsTimeline(paging);
            if (mentions != null && mentions.size() > 0 && isNew(mentions.get(0).getCreatedAt())) {
                Status mention = mentions.get(0);
                sendPN(PN.MENTION, mention.getText(), getString(R.string.pn_mention_title));
                return;
            }

        } catch (TwitterException e) {
            e.printStackTrace();
        }


        //check for new tweets

        try {
            List<twitter4j.Status> downloadedList = mTimeline.downloadTimeline(
                    Timeline.UP_TWEETS);
            mTimeline.updateTimelineUpDb(downloadedList);

            int size = downloadedList.size();
            if (size > 0) {
                sNewTweets += size;

                String message = getString(R.string.pn_new_message) + sNewTweets;
                String title = getString(R.string.pn_new_title);

                sendPN(PN.UPDATE, message, title);
            }
        } catch (NullPointerException e) {
        }
    }

    private boolean isNew(Date createdAt) {
        Date currentDate = new Date();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int interval = prefs.getInt(SettingsFragment.PN_INTERVAL, 4 * 60);
        long intervalInMillis = TimeUnit.MINUTES.toMillis(interval);
        return ! (currentDate.getTime() - createdAt.getTime() > intervalInMillis);

    }

    private void sendPN(PN type, String message, String title) {
        Intent localIntent = new Intent(TweetReceiver.BROADCAST_ACTION)
                .putExtra(MESSAGE, message)
                .putExtra(TITLE, title)
                .putExtra(TYPE, type);
        sendBroadcast(localIntent);
    }

    private void loadFromDB() {

        String[] projection = Tweets.getProjection();

        ContentResolver resolver = getContentResolver();

        Cursor cursor = mTimeline.getNewestTweet(resolver, projection);

        List<Status> tweets = Timeline.buildTweets(cursor, false);
        mTimeline.getTweets().addAll(tweets);
    }

    public static void setNewTweets(int count) {
        sNewTweets = count;
    }
}
