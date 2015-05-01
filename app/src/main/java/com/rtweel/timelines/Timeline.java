package com.rtweel.timelines;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.rtweel.tasks.db.DbWriteTask;
import com.rtweel.storage.Tweets;
import com.rtweel.storage.App;
import com.rtweel.utils.MediaParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public abstract class Timeline implements Iterable<Status> {

    public static final int UP_TWEETS = 0;
    public static final int DOWN_TWEETS = 1;
    public static final int INITIALIZATION_TWEETS = 2;

    public static final int TWEETS_PER_PAGE = 30;

    protected List<twitter4j.Status> list;

    private final Context mContext;

    private String mUserName;
    private String mScreenUserName;
    private long mUserId;


    protected abstract List<Status> getNewTweets(Twitter twitter, Paging page);

    public abstract boolean isHomeTimeline();

    protected abstract Cursor getPreparedTweets(ContentResolver resolver, String[] projection);

    protected abstract Cursor getDownTweetsFromDb(ContentResolver resolver, String[] projection);

    public abstract Cursor getOldestTweet(ContentResolver resolver, String[] projection);

    public abstract Cursor getNewestTweet(ContentResolver resolver, String[] projection);


    public Timeline(Context context) {
        list = new ArrayList<>();
        mContext = context;

        Log.i("DEBUG", "timeline construction finished");

    }

    public void loadTimeline() {
        preparingUpdate();

        if (list.isEmpty()) {
            if (!App.isOnline(mContext)) {
                Log.i("DEBUG", "No network in loadTimeline()");
                return;
            }


            List<Status> tweets = downloadTimeline(Timeline.INITIALIZATION_TWEETS);

            if (tweets != null && tweets.size() > 0) {

                list.addAll(tweets);

                new DbWriteTask(mContext, list, isHomeTimeline()).execute();
            }

        }
    }

    public void updateTimelineUpDb(List<Status> downloadedList) {

        if (downloadedList == null || downloadedList.size() == 0) {
            return;
        }

        int prevSize = list.size();
        list.addAll(0, downloadedList);
        Log.i("DEBUG", "New tweets: " + (list.size() - prevSize));
        new DbWriteTask(mContext, downloadedList, isHomeTimeline())
                .execute();
    }

    public void updateTimelineDown(List<Status> downloadedList) {

        if (downloadedList == null || downloadedList.isEmpty())
            return;

        list.addAll(downloadedList);
        new DbWriteTask(mContext, downloadedList, isHomeTimeline())
                .execute();
    }

    public List<twitter4j.Status> downloadTimeline(int flag)
            throws NullPointerException {
        Log.i("DEBUG", "downloading timeline..");

        Paging page = new Paging();
        page.setCount(TWEETS_PER_PAGE);

        long id;
        switch (flag) {
            case INITIALIZATION_TWEETS:
                page.setPage(1);
                break;
            case UP_TWEETS:
                Status lastTweet = getLastTweetFromDb();

                if (lastTweet != null)
                    id = lastTweet.getId();
                else if (list != null && list.size() > 0)
                    id = list.get(0).getId();
                else
                    id = 1;

                page.setSinceId(id);
                break;
            case DOWN_TWEETS:
                Status oldestTweet = getOldestTweetFromDb();

                if (oldestTweet != null)
                    id = oldestTweet.getId();
                else if (list != null && list.size() > 0)
                    id = list.get(list.size() - 1).getId();
                else
                    id = Long.MAX_VALUE;

                page.setMaxId(id - 1);
                break;
        }

        Twitter twitter = Tweets.getTwitter(mContext);
        return getNewTweets(twitter, page);
    }

    private Status getOldestTweetFromDb() {
        String[] projection = Tweets.getProjection();

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = getOldestTweet(resolver, projection);

        ArrayList<Status> tweets = new ArrayList<Status>(buildTweets(cursor, false));

        return tweets.size() > 0 ? tweets.get(0) : null;
    }

    private Status getLastTweetFromDb() {

        String[] projection = Tweets.getProjection();

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = getNewestTweet(resolver, projection);

        ArrayList<Status> tweets = new ArrayList<Status>(buildTweets(cursor, false));

        return tweets.size() > 0 ? tweets.get(0) : null;
    }

    public void remove(int position) {
        list.remove(position);
    }

    @Override
    public Iterator<Status> iterator() {
        return list.iterator();
    }

    public void clear() {
        list.clear();
    }

    public List<Status> getTweets() {
        return list;
    }

    private void preparingUpdate() {
        String[] projection = Tweets.getProjection(true);

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = getPreparedTweets(resolver, projection);

        ArrayList<Status> tweets = new ArrayList<Status>(buildTweets(cursor, true));
        list.addAll(tweets);

    }

    public int updateFromDb() {

        String[] projection = Tweets.getProjection(true);

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = getDownTweetsFromDb(resolver, projection);

        ArrayList<Status> tweets = new ArrayList<Status>(buildTweets(cursor, true));
        list.addAll(tweets);

        return tweets.size();

    }

    // TODO: Implementation

    public ArrayList<Status> search(String queryString) {
        Query query = new Query();
        query.setResultType(Query.RECENT);
        query.setQuery(queryString);
        query.setCount(100);
        //query.setSinceId(list.get(0).getId());

        List<Status> resultList = null;
        try {
            QueryResult result = Tweets.getTwitter(mContext).search(query);
            resultList = result.getTweets();
        } catch
                (TwitterException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(resultList);
    }

    public static Status buildTweet(String author, String text, String pictureUrl, String date, long id, String[] media) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{text='")
                    .append(text)
                    .append("', id='")
                    .append(id)
                    .append("', created_at='")
                    .append(date)
                    .append("'");
            if (media.length > 0 && !TextUtils.isEmpty(media[0])) {
//						builder.append("', hashtags=[], symbols=[], urls=[], user_mentions=[], entities={media=[{indices=[], sizes=[],media_url='");
                builder.append(",\"extended_entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[],\"media\":[");

                for (int i = 0; i < media.length; i++)
                    builder.append(constructMedia(media[i], i == media.length - 1));

           //     Log.i("DEBUG", builder.toString());
//                      .append("'}]}");
            }

            builder.append(",user={name='")
                    .append(author)
                    .append("', profile_image_url='")
                    .append(pictureUrl)
                    .append("'}}");
            //TODO add isfavorited, mentions, etc
            return TwitterObjectFactory.createStatus(builder
                    .toString());
        } catch (TwitterException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    private static String constructMedia(String media, boolean isLast) {
        StringBuilder builder = new StringBuilder("{\"indices\":[-1, -2],\"url\":\"\",\"expanded_url\":\"\",\"display_url\":\"\",\"media_url_https\":\"\",\"media_url\":\"")
                .append(media)
                .append("\",\"type\":\"photo\",\"sizes\":{\"large\":{\"w\":1024,\"h\":575,\"resize\":\"fit\"},\"small\":{\"w\":340,\"h\":191,\"resize\":\"fit\"},\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"medium\":{\"w\":600,\"h\":337,\"resize\":\"fit\"}}}")
                .append(isLast ? "]}" : ",");
        return builder.toString();
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String name) {
        mUserName = name;
    }

    public String getScreenUserName() {
        return mScreenUserName;
    }

    public void setScreenUserName(String screenName) {
        mScreenUserName = screenName;
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long id) {
        mUserId = id;
    }

    public static List<Status> buildTweets(Cursor cursor, boolean withMedia) {
        ArrayList<Status> tweets = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String author = getColumnString(0, cursor);
                String text = getColumnString(1, cursor).replace("\n", "\\n");
                String pictureUrl = getColumnString(2, cursor);
                String date = getColumnString(3, cursor);
                long id = getColumnLong(4, cursor);

                String[] media = {};
                if (withMedia)
                    media = MediaParser.deserialize(getColumnString(5, cursor));

                Status tweet = buildTweet(author, text, pictureUrl, date, id, media);
                if (tweet != null)
                    tweets.add(tweet);
            }

            if (cursor != null) {
                cursor.close();
            }

        }

        return tweets;

    }

    private static String getColumnString(int position, Cursor cursor) {
        String[] projection = Tweets.getProjection(true);
        return cursor.getString(cursor.getColumnIndex(projection[position]));
    }

    private static long getColumnLong(int position, Cursor cursor) {
        String[] projection = Tweets.getProjection(true);
        return cursor.getLong(cursor.getColumnIndex(projection[position]));
    }

    public long getLastItemIdOrMax() {
        if (list != null && list.size()  > 0)
            return list.get(list.size() - 1).getId();
        else
            return Long.MAX_VALUE;
    }

}
