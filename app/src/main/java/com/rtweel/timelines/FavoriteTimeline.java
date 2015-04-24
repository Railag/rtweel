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
public class FavoriteTimeline extends Timeline {

    public FavoriteTimeline(Context context) {
        super(context);
    }

    @Override
    protected List<Status> getNewTweets(Twitter twitter, Paging page) {
        try {
            return twitter.getFavorites(page);
        } catch (TwitterException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected boolean isHomeTimeline() {
        return false;
    }

    @Override
    public Cursor getOldestTweet(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets.COLUMN_IS_FAVORITE + " != '0'", null, TweetDatabase.SELECTION_ASC + "LIMIT 1");
    }

    @Override
    public Cursor getNewestTweet(ContentResolver resolver, String[] projection) {
        //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets.COLUMN_IS_FAVORITE + " != '0'", null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
    }

    @Override
    protected Cursor getPreparedTweets(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets.COLUMN_IS_FAVORITE + " != '0'", null, TweetDatabase.SELECTION_DESC + "LIMIT 30");
    }

    @Override
    protected Cursor getDownTweetsFromDb(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets._ID + "<"
                        + list.get(list.size() - 1).getId() + " AND " + TweetDatabase.Tweets.COLUMN_IS_FAVORITE + " != '0'", null,
                TweetDatabase.SELECTION_DESC + "LIMIT 100");
    }
}
