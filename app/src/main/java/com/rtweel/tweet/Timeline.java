package com.rtweel.tweet;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.rtweel.asynctasks.db.DbWriteTask;
import com.rtweel.asynctasks.tweet.GetScreenNameTask;
import com.rtweel.cache.App;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.twitteroauth.TwitterUtil;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import twitter4j.Paging;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;

public class Timeline implements Iterable<Status> {

    public static final int USER_TIMELINE = 0;
    public static final int HOME_TIMELINE = 1;

    public static final int UP_TWEETS = 0;
    public static final int DOWN_TWEETS = 1;
    public static final int INITIALIZATION_TWEETS = 2;

    public static final int TWEETS_PER_PAGE = 30;

    private List<twitter4j.Status> list;

    private Twitter mTwitter;

    private int mCurrentTimelineType;
    private final Context mContext;

    private static Timeline sTimeline;

    private static String sUserName;
    private static String sScreenUserName;

    public Timeline(Context context, int timelineType) {
        mCurrentTimelineType = timelineType;
        list = new ArrayList<twitter4j.Status>();
        mContext = context;
        String accessTokenString = null;
        String accessTokenSecret = null;

        FileInputStream inputStream;
        byte[] inputBytes;

        String inputString = null;
        try {
            inputStream = new FileInputStream(
                    Environment.getExternalStorageDirectory() + App.PATH);
            inputBytes = new byte[inputStream.available()];
            inputStream.read(inputBytes);
            inputString = new String(inputBytes);

            int position = inputString.indexOf(' ');
            accessTokenString = inputString.substring(0, position);
            accessTokenSecret = inputString.substring(position + 1,
                    inputString.length());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (accessTokenString != null && accessTokenSecret != null) {
            AccessToken accessToken = new AccessToken(accessTokenString,
                    accessTokenSecret);
            mTwitter = TwitterUtil.getInstance().getTwitterFactory()
                    .getInstance(accessToken);
            mTwitter.addRateLimitStatusListener(new RateLimitStatusListener() {
                @Override
                public void onRateLimitStatus(RateLimitStatusEvent event) {
                    Log.i("LIMIT", "limit = " + event.getRateLimitStatus().getLimit());
                    Log.i("LIMIT", "remaining: " + event.getRateLimitStatus().getRemaining());
                    Log.i("LIMIT", "secondsUntilReset: " + event.getRateLimitStatus().getSecondsUntilReset());
                }

                @Override
                public void onRateLimitReached(RateLimitStatusEvent event) {
                    Log.i("LIMIT", "BLOCKED");
                }
            });


            new GetScreenNameTask().execute(mTwitter);
            Log.i("DEBUG", "timeline construction finished");
        }
    }

    public void loadTimeline() {
        preparingUpdate();

        if (list.isEmpty()) {
            if (!App.isOnline(mContext)) {
                Log.i("DEBUG", "No network in loadTimeline()");
                return;
            }
            list.addAll(downloadTimeline(Timeline.INITIALIZATION_TWEETS));

            new DbWriteTask(mContext, list, mCurrentTimelineType).execute();

        }
    }

    public void updateTimelineUp(List<Status> downloadedList) {

        if (downloadedList == null) {
            return;
        } else if (downloadedList.isEmpty()) {
            return;
        }
        Log.i("DEBUG", "downloading up");
        int prevSize = list.size();
        list.addAll(0, downloadedList);
        Log.i("DEBUG", "New tweets: " + (list.size() - prevSize));
        new DbWriteTask(mContext, downloadedList, mCurrentTimelineType)
                .execute();
    }

    public void updateTimelineDown(List<Status> downloadedList) {

        if (downloadedList == null) {
            return;
        } else if (downloadedList.isEmpty()) {
            return;
        }
        list.addAll(downloadedList);
        new DbWriteTask(mContext, downloadedList, mCurrentTimelineType)
                .execute();
    }

    public List<twitter4j.Status> downloadTimeline(int flag)
            throws NullPointerException {
        Log.i("DEBUG", "downloading timeline..");

        List<twitter4j.Status> downloadedList = new ArrayList<Status>();

        Paging page = new Paging();
        page.setCount(TWEETS_PER_PAGE);
        switch (flag) {
            case INITIALIZATION_TWEETS:
                page.setPage(1);
                break;
            case UP_TWEETS:
                if (list == null || list.isEmpty())
                    getLastTweetFromDb();
                page.setSinceId(list.get(0).getId());
                break;
            case DOWN_TWEETS:
                if (list == null || list.isEmpty())
                    getOldestTweetFromDb();
                page.setMaxId(list.get(list.size() - 1).getId());
                break;
        }

        switch (mCurrentTimelineType) {
            case Timeline.HOME_TIMELINE: {
                try {
                    downloadedList = mTwitter.getHomeTimeline(page);
                } catch (TwitterException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            }
            case Timeline.USER_TIMELINE: {
                try {
                    downloadedList = mTwitter.getUserTimeline(page);
                } catch (TwitterException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return downloadedList;
    }

    private void getOldestTweetFromDb() {
        String[] projection = {TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR,
                TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT,
                TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_ID};

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        if (getCurrentTimelineType() == Timeline.HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
                    projection, null, null, TweetDatabaseOpenHelper.SELECTION_ASC + "LIMIT 1");
        } else if (getCurrentTimelineType() == Timeline.USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
                    projection, null, null, TweetDatabaseOpenHelper.SELECTION_ASC + "LIMIT 1");
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

                Status tweet = buildTweet(author, text, pictureUrl, date, id, "");
                if (tweet != null)
                    getTweets().add(tweet);

            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private void getLastTweetFromDb() {

        String[] projection = {TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR,
                TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT,
                TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_ID};

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        if (getCurrentTimelineType() == Timeline.HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
                    projection, null, null, TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 1");
        } else if (getCurrentTimelineType() == Timeline.USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
                    projection, null, null, TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 1");
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

                Status tweet = buildTweet(author, text, pictureUrl, date, id, "");
                if (tweet != null)
                    getTweets().add(tweet);
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Status get(int position) {
        return list.get(position);
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

    public void addAll(List<Status> tweets) {
        list.addAll(tweets);
    }

    public void setTimelineType(int type) {
        mCurrentTimelineType = type;
    }

    public List<Status> getTweets() {
        return list;
    }

    private void preparingUpdate() {
        String[] projection = {TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR,
                TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT,
                TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_ID,
                TweetDatabaseOpenHelper.Tweets.COLUMN_MEDIA};

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        if (mCurrentTimelineType == HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
                    projection, null, null, TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 30");
        } else if (mCurrentTimelineType == USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
                    projection, null, null, TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 30");
       }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String author = cursor.getString(cursor
                        .getColumnIndex(projection[0]));
                String text = cursor.getString(cursor
                        .getColumnIndex(projection[1])).replace("\n", "\\n");
                String pictureUrl = cursor.getString(cursor
                        .getColumnIndex(projection[2]));
                String date = cursor.getString(cursor
                        .getColumnIndex(projection[3]));
                long id = cursor.getLong(cursor.getColumnIndex(projection[4]));
                String media = cursor.getString(cursor
                        .getColumnIndex(projection[5]));

                Status tweet = buildTweet(author, text, pictureUrl, date, id, media);
                if (tweet != null)
                    list.add(tweet);

            }
            if (cursor != null) {
                cursor.close();
            }

        }

    }

    public int updateFromDb() {
        int result = 0;
        String[] projection = {TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR,
                TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT,
                TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
                TweetDatabaseOpenHelper.Tweets.COLUMN_ID,
                TweetDatabaseOpenHelper.Tweets.COLUMN_MEDIA};

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        if (mCurrentTimelineType == HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
                    projection, TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "<"
                            + list.get(list.size() - 1).getId(), null,
                    TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 100");
        } else if (mCurrentTimelineType == USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
                    projection, TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "<"
                            + list.get(list.size() - 1).getId(), null,
                    TweetDatabaseOpenHelper.SELECTION_DESC + "LIMIT 100");
        }
        if (cursor != null) {
            result = cursor.getCount();
            while (cursor.moveToNext()) {
                String author = cursor.getString(cursor
                        .getColumnIndex(projection[0]));
                String text = cursor.getString(cursor
                        .getColumnIndex(projection[1])).replace("\n", "\\n");
                String pictureUrl = cursor.getString(cursor
                        .getColumnIndex(projection[2]));
                String date = cursor.getString(cursor
                        .getColumnIndex(projection[3]));
                long id = cursor.getLong(cursor.getColumnIndex(projection[4]));
                String media = cursor.getString(cursor
                        .getColumnIndex(projection[5]));

                Status tweet = buildTweet(author, text, pictureUrl, date, id, media);
                if (tweet != null)
                    list.add(tweet);

            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;

    }

    // TODO: Implementation
    /*
     * public boolean searchCheckIsAvailable(String queryString) { Query query =
	 * new Query(); query.setResultType(Query.RECENT);
	 * query.setQuery(queryString); query.setCount(1);
	 * query.setSinceId(list.get(0).getId());
	 * 
	 * QueryResult result = null; try { result = mTwitter.search(query); } catch
	 * (TwitterException e) { e.printStackTrace(); }
	 * 
	 * return !result.getTweets().isEmpty(); }
	 */

    public static Status buildTweet(String author, String text, String pictureUrl, String date, long id, String media) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{text='");
            builder.append(text);
            builder.append("', id='");
            builder.append(id);
            builder.append("', created_at='");
            builder.append(date);
            if (!TextUtils.isEmpty(media)) {
//						builder.append("', hashtags=[], symbols=[], urls=[], user_mentions=[], entities={media=[{indices=[], sizes=[],media_url='");
                builder.append("',\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[],\"media\":[{\"indices\":[-1, -2],\"url\":\"\",\"expanded_url\":\"\",\"display_url\":\"\",\"media_url_https\":\"\",\"media_url\":\"");
                builder.append(media);
                builder.append("\",\"type\":\"photo\",\"sizes\":{\"large\":{\"w\":1024,\"h\":575,\"resize\":\"fit\"},\"small\":{\"w\":340,\"h\":191,\"resize\":\"fit\"},\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"medium\":{\"w\":600,\"h\":337,\"resize\":\"fit\"}}}]}");
                //	builder.append("'}]}");
            } else {
                builder.append("'");
            }
            builder.append(",user={name='");
            builder.append(author);
            builder.append("', profile_image_url='");
            builder.append(pictureUrl);
            builder.append("'}}");
            return TwitterObjectFactory.createStatus(builder
                    .toString());
        } catch (TwitterException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    public Twitter getTwitter() {
        return mTwitter;
    }

    public static void setDefaultTimeline(Timeline timeline) {
        sTimeline = timeline;
        sTimeline.mCurrentTimelineType = timeline.getCurrentTimelineType();
    }

    public static Timeline getDefaultTimeline() {
        return sTimeline;
    }

    public int getCurrentTimelineType() {
        return mCurrentTimelineType;
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
}
