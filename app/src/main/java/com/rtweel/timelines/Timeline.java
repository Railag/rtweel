package com.rtweel.timelines;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.rtweel.asynctasks.db.DbWriteTask;
import com.rtweel.asynctasks.db.Tweets;
import com.rtweel.asynctasks.tweet.GetScreenNameTask;
import com.rtweel.cache.App;

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

    private static String sUserName;
    private static String sScreenUserName;


    protected abstract List<Status> getNewTweets(Twitter twitter, Paging page);

    protected abstract boolean isUserTimeline();

    protected abstract Cursor getPreparedTweets(ContentResolver resolver, String[] projection);

    protected abstract Cursor getDownTweetsFromDb(ContentResolver resolver, String[] projection);

    public abstract Cursor getOldestTweet(ContentResolver resolver, String[] projection);

    public abstract Cursor getNewestTweet(ContentResolver resolver, String[] projection);


    public Timeline(Context context) {
        list = new ArrayList<>();
        mContext = context;

        new GetScreenNameTask().execute(Tweets.getTwitter(mContext));
        Log.i("DEBUG", "timeline construction finished");

    }

    public void loadTimeline() {
        preparingUpdate();

        if (list.isEmpty()) {
            if (!App.isOnline(mContext)) {
                Log.i("DEBUG", "No network in loadTimeline()");
                return;
            }

            list.addAll(downloadTimeline(Timeline.INITIALIZATION_TWEETS));

            new DbWriteTask(mContext, list, isUserTimeline()).execute();

        }
    }

    public void updateTimelineUpDb(List<Status> downloadedList) {

        if (downloadedList == null || downloadedList.size() == 0) {
            return;
        }

        int prevSize = list.size();
        list.addAll(0, downloadedList);
        Log.i("DEBUG", "New tweets: " + (list.size() - prevSize));
        new DbWriteTask(mContext, downloadedList, isUserTimeline())
                .execute();
    }

    public void updateTimelineDown(List<Status> downloadedList) {

        if (downloadedList == null) {
            return;
        } else if (downloadedList.isEmpty()) {
            return;
        }
        list.addAll(downloadedList);
        new DbWriteTask(mContext, downloadedList, isUserTimeline())
                .execute();
    }

    public List<twitter4j.Status> downloadTimeline(int flag)
            throws NullPointerException {
        Log.i("DEBUG", "downloading timeline..");

        Paging page = new Paging();
        page.setCount(TWEETS_PER_PAGE);
        switch (flag) {
            case INITIALIZATION_TWEETS:
                page.setPage(1);
                break;
            case UP_TWEETS:
                getLastTweetFromDb();
                if (list.size() > 0)
                    page.setSinceId(list.get(0).getId());
                break;
            case DOWN_TWEETS:
                getOldestTweetFromDb();
                if (list.size() > 0)
                    page.setMaxId(list.get(0).getId());
                break;
        }

        Twitter twitter = Tweets.getTwitter(mContext);
        return getNewTweets(twitter, page);
    }

    private void getOldestTweetFromDb() {
        String[] projection = Tweets.getProjection();

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = getOldestTweet(resolver, projection);

        ArrayList<Status> tweets = new ArrayList<Status>(buildTweets(cursor, true));
        list.addAll(tweets);

    }

    private void getLastTweetFromDb() { //TODO ABSTRACT

        String[] projection = Tweets.getProjection();

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = getNewestTweet(resolver, projection);

        ArrayList<Status> tweets = new ArrayList<Status>(buildTweets(cursor, false));
        list.addAll(tweets);
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


    public static Status buildTweet(String author, String text, String pictureUrl, String date, long id) {
        return buildTweet(author, text, pictureUrl, date, id, "");
    }

    public static Status buildTweet(String author, String text, String pictureUrl, String date, long id, String media) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{text='")
                    .append(text)
                    .append("', id='")
                    .append(id)
                    .append("', created_at='")
                    .append(date)
                    .append("'");
            if (!TextUtils.isEmpty(media)) {
//						builder.append("', hashtags=[], symbols=[], urls=[], user_mentions=[], entities={media=[{indices=[], sizes=[],media_url='");
                builder.append(",\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[],\"media\":[{\"indices\":[-1, -2],\"url\":\"\",\"expanded_url\":\"\",\"display_url\":\"\",\"media_url_https\":\"\",\"media_url\":\"")
                        .append(media)
                        .append("\",\"type\":\"photo\",\"sizes\":{\"large\":{\"w\":1024,\"h\":575,\"resize\":\"fit\"},\"small\":{\"w\":340,\"h\":191,\"resize\":\"fit\"},\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"medium\":{\"w\":600,\"h\":337,\"resize\":\"fit\"}}}]}");
//                      .append("'}]}");
            }

            builder.append(",user={name='")
                    .append(author)
                    .append("', profile_image_url='")
                    .append(pictureUrl)
                    .append("'}}");
            Log.i("CreateStatus", builder.toString());
            return TwitterObjectFactory.createStatus(builder
                    .toString());
        } catch (TwitterException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    public static String getUserName() {
        return sUserName;
    }

    public static void setUserName(String name) {
        sUserName = name;
    }

    public static String getScreenUserName() {
        return sScreenUserName;
    }

    public static void setScreenUserName(String screenName) {
        sScreenUserName = screenName;
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

                String media = "";
                if (withMedia)
                    media = getColumnString(5, cursor);

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


}
