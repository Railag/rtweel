package com.rtweel.timelines;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.rtweel.sqlite.TweetDatabase;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by root on 6.4.15.
 */
public class HomeTimeline extends Timeline {

    public HomeTimeline(Context context) {
        super(context);
    }

    @Override
    protected List<Status> getNewTweets(Twitter twitter, Paging page) {
        try {
            return twitter.getHomeTimeline(page);
        } catch (TwitterException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected boolean isHomeTimeline() {
        return true;
    }

    @Override
    public Cursor getOldestTweet(ContentResolver resolver, String[] projection) {
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                projection, TweetDatabase.Tweets.COLUMN_USER_ID + " != " + getUserId(), null, TweetDatabase.SELECTION_ASC + "LIMIT 1");
    }

    @Override
    public Cursor getNewestTweet(ContentResolver resolver, String[] projection) {
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
    }

    @Override
    protected Cursor getPreparedTweets(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 30");
    }

    @Override
    protected Cursor getDownTweetsFromDb(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_HOME_DB,
                projection, TweetDatabase.Tweets._ID + "<"
                        + list.get(list.size() - 1).getId(), null,
                TweetDatabase.SELECTION_DESC + "LIMIT 100");
    }

}
