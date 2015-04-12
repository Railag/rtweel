package com.rtweel.timelines;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.rtweel.asynctasks.db.DbWriteTask;
import com.rtweel.asynctasks.db.Tweets;
import com.rtweel.asynctasks.tweet.GetScreenNameTask;
import com.rtweel.cache.App;
import com.rtweel.sqlite.TweetDatabase;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;

public abstract class Timeline implements Iterable<Status> {

    public static final int USER_TIMELINE = 0;
    public static final int HOME_TIMELINE = 1;
    public static final int FAVORITE_TIMELINE = 2;
    public static final int ANSWERS_TIMELINE = 3;
    public static final int IMAGES_TIMELINE = 4;

    public static final int UP_TWEETS = 0;
    public static final int DOWN_TWEETS = 1;
    public static final int INITIALIZATION_TWEETS = 2;

    public static final int TWEETS_PER_PAGE = 30;

    protected List<twitter4j.Status> list;

    private int mCurrentTimelineType;
    private final Context mContext;

    private static String sUserName;
    private static String sScreenUserName;

    public Timeline(Context context, int timelineType) {
        mCurrentTimelineType = timelineType;
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

            new DbWriteTask(mContext, list, mCurrentTimelineType).execute();

        }
    }

    public void updateTimelineUpDb(List<Status> downloadedList) {

        if (downloadedList == null || downloadedList.size() == 0) {
            return;
        }

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
                //    if (list == null || list.isEmpty())
                getLastTweetFromDb();
                if (list.size() > 0)
                    page.setSinceId(list.get(0).getId());
                break;
            case DOWN_TWEETS:
                //   if (list == null || list.isEmpty())
                getOldestTweetFromDb();
                if (list.size() > 0)
                    page.setMaxId(list.get(0).getId());//(list.size() - 1).getId());
                break;
        }

        Twitter twitter = Tweets.getTwitter(mContext);
        switch (mCurrentTimelineType) {
            case Timeline.HOME_TIMELINE:
                try {
                    downloadedList = twitter.getHomeTimeline(page);
                } catch (TwitterException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case Timeline.USER_TIMELINE:
                try {
                    downloadedList = twitter.getUserTimeline(page);
                    //mTwitter.users(). //TODO
                } catch (TwitterException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case Timeline.FAVORITE_TIMELINE:
                try {
                    downloadedList = twitter.getFavorites(page);
                } catch (TwitterException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case Timeline.ANSWERS_TIMELINE:
                try {
                    downloadedList = twitter.getMentionsTimeline(page); //TODO change
                } catch (TwitterException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case Timeline.IMAGES_TIMELINE:
                try {
                    List<Status> download = twitter.getHomeTimeline(page);

                    for (Status s : download)
                        if (s.getMediaEntities().length > 0 && s.getUser().getScreenName().equals(getScreenUserName()))
                            downloadedList.add(s);

                } catch (TwitterException | NullPointerException e) {
                    e.printStackTrace();
                }
                break;

        }
        return downloadedList;
    }

    private void getOldestTweetFromDb() {
        String[] projection = Tweets.getProjection();

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        if (getCurrentTimelineType() == Timeline.HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                    projection, null, null, TweetDatabase.SELECTION_ASC + "LIMIT 1");
        } else if (getCurrentTimelineType() == Timeline.USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_USER_DB,
                    projection, null, null, TweetDatabase.SELECTION_ASC + "LIMIT 1");
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


    private void getLastTweetFromDb() { //TODO ABSTRACT

        String[] projection = Tweets.getProjection();

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        switch (getCurrentTimelineType()) {
            case USER_TIMELINE:
                cursor = resolver.query(
                        TweetDatabase.Tweets.CONTENT_URI_USER_DB,
                        projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
                break;
            case HOME_TIMELINE:
                cursor = resolver.query(
                        TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                        projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
                break;
            case FAVORITE_TIMELINE: //TODO impl
                cursor = resolver.query(
                        TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                        projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
                break;
            case ANSWERS_TIMELINE: //TODO impl
                cursor = resolver.query(
                        TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                        projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
                break;
            case IMAGES_TIMELINE: //TODO impl
                cursor = resolver.query(
                        TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                        projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 1");
                break;
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
        String[] projection = Tweets.getProjection(true);

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        if (mCurrentTimelineType == HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                    projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 30");
        } else if (mCurrentTimelineType == USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_USER_DB,
                    projection, null, null, TweetDatabase.SELECTION_DESC + "LIMIT 30");
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
        String[] projection = Tweets.getProjection(true);

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = null;
        if (mCurrentTimelineType == HOME_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_TWEET_DB,
                    projection, TweetDatabase.Tweets._ID + "<"
                            + list.get(list.size() - 1).getId(), null,
                    TweetDatabase.SELECTION_DESC + "LIMIT 100");
        } else if (mCurrentTimelineType == USER_TIMELINE) {
            cursor = resolver.query(
                    TweetDatabase.Tweets.CONTENT_URI_USER_DB,
                    projection, TweetDatabase.Tweets._ID + "<"
                            + list.get(list.size() - 1).getId(), null,
                    TweetDatabase.SELECTION_DESC + "LIMIT 100");
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
            return TwitterObjectFactory.createStatus(builder
                    .toString());
        } catch (TwitterException e1) {
            e1.printStackTrace();
            return null;
        }
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
