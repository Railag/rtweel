package com.rtweel.asynctasks.db;

import com.rtweel.sqlite.TweetDatabase;

import java.util.ArrayList;

/**
 * Created by root on 10.4.15.
 */



public class Tweets {
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
}
