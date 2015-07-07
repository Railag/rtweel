package com.rtweel.timelines;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.rtweel.storage.AppUser;
import com.rtweel.storage.TweetDatabase;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by firrael on 6.4.15.
 */
public class AnswersTimeline extends Timeline {

    public AnswersTimeline(Context context) {
        super(context);
    }

    @Override
    protected List<Status> getNewTweets(Twitter twitter, Paging page) {
        if (getUserId() != AppUser.getUserId(mContext))
            return null;

        try {
            return twitter.getMentionsTimeline(page);
        } catch (TwitterException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isHomeTimeline() {
        return false;
    }

    @Override
    public Cursor getOldestTweet(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets.COLUMN_MENTIONS + " = '" + getScreenUserName() + "'", null, TweetDatabase.SELECTION_ASC + "LIMIT 1");
    }

    @Override
    public Cursor getNewestTweet(ContentResolver resolver, String[] projection) {
        //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets.COLUMN_MENTIONS + " = '" + getScreenUserName() + "'", null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
    }

    @Override
    protected Cursor getPreparedTweets(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets.COLUMN_MENTIONS + " = '" + getScreenUserName() + "'", null, TweetDatabase.SELECTION_DESC + "LIMIT 30");
    }

    @Override
    protected Cursor getDownTweetsFromDb(ContentResolver resolver, String[] projection) { //TODO impl
        return resolver.query(
                TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                projection, TweetDatabase.Tweets._ID + "<"
                        + getLastItemIdOrMax() + " AND " + TweetDatabase.Tweets.COLUMN_MENTIONS + " = '" + getScreenUserName() + "'", null,
                TweetDatabase.SELECTION_DESC + "LIMIT 100");
    }
}
