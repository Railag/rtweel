package com.rtweel.tweet;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.auth.AccessToken;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.BaseAdapter;

import com.rtweel.asynctasks.db.DbWriteTask;
import com.rtweel.asynctasks.tweet.GetScreenNameTask;
import com.rtweel.cache.App;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.twitteroauth.TwitterUtil;

public class Timeline implements Iterable<Status> {

	public static final int USER_TIMELINE = 0;
	public static final int HOME_TIMELINE = 1;

	public static final int UP_TWEETS = 0;
	public static final int DOWN_TWEETS = 1;
	public static final int INITIALIZATION_TWEETS = 2;

	public static final int TWEETS_PER_PAGE = 30;

	private List<twitter4j.Status> list;

	private Twitter mTwitter;

    private BaseAdapter mAdapter;

	private int mCurrentTimelineType;
	private final Context mContext;

	private static Timeline sTimeline;

	private static String sUserName;
	private static String sScreenUserName;

	public Timeline(Context context) {
		mCurrentTimelineType = HOME_TIMELINE;
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
			new GetScreenNameTask().execute(mTwitter);
			Log.i("DEBUG", "timeline construction finished");
		}
	}

	public void loadTimeline() {
		Log.i("DEBUG", "loadtimeline started");
		Date t1 = new Date();
		preparingUpdate();
		Log.i("DEBUG",
				"preparing update time: "
						+ (new Date().getTime() - t1.getTime()));

		if (list.isEmpty()) {
			App app = (App) mContext;
			if (!app.isOnline()) {
				Log.i("DEBUG", "No network in loadTimeline()");
				return;
			}
			Log.i("DEBUG", "loading timeline..");
			t1 = new Date();
			list.addAll(downloadTimeline(Timeline.INITIALIZATION_TWEETS));
			Date t2 = new Date();
			Log.i("DEBUG",
					"downloadTimeline time: " + (t2.getTime() - t1.getTime()));
			new DbWriteTask(mContext, list, mCurrentTimelineType).execute();
			Log.i("DEBUG", "dbwritetask start time: "
					+ (new Date().getTime() - t2.getTime()));
		}
		Log.i("DEBUG", "loadTimeline finished");

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
			page.setSinceId(list.get(0).getId());
			break;
		case DOWN_TWEETS:
			page.setMaxId(list.get(list.size() - 1).getId());
			break;
		}

		switch (mCurrentTimelineType) {
		case Timeline.HOME_TIMELINE: {
			try {
				downloadedList = mTwitter.getHomeTimeline(page);
			} catch (TwitterException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			break;
		}
		case Timeline.USER_TIMELINE: {
			try {
				downloadedList = mTwitter.getUserTimeline(page);
			} catch (TwitterException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			break;
		}
		}
		return downloadedList;
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
		String[] projection = { TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR,
				TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT,
				TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE,
				TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
				TweetDatabaseOpenHelper.Tweets.COLUMN_ID,
				TweetDatabaseOpenHelper.Tweets.COLUMN_MEDIA };

		ContentResolver resolver = mContext.getContentResolver();

		Cursor cursor = null;
		if (mCurrentTimelineType == HOME_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
					projection, null, null, "LIMIT 30");
		} else if (mCurrentTimelineType == USER_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
					projection, null, null, "LIMIT 30");
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String author = cursor.getString(cursor
						.getColumnIndex(projection[0]));
				String text = cursor.getString(cursor
						.getColumnIndex(projection[1]));
				String pictureUrl = cursor.getString(cursor
						.getColumnIndex(projection[2]));
				String date = cursor.getString(cursor
						.getColumnIndex(projection[3]));
				long id = cursor.getLong(cursor.getColumnIndex(projection[4]));
				String media = cursor.getString(cursor
						.getColumnIndex(projection[5]));
				try {
					StringBuilder builder = new StringBuilder();
					builder.append("{text='");
					builder.append(text);
					builder.append("', id='");
					builder.append(id);
					builder.append("', created_at='");
					builder.append(date);
					Log.i("DEBUG", "media: " + media);
					if (!"".equals(media)) {
					//	builder.append("', hashtags=[], symbols=[], urls=[], user_mentions=[], entities={media=[{indices=[], sizes=[],media_url='");
						builder.append("',\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[],\"media\":[{\"indices\":[-1, -2],\"url\":\"\",\"expanded_url\":\"\",\"display_url\":\"\",\"media_url_https\":\"\",\"media_url\":\"");
						builder.append(media);
						builder.append("\",\"type\":\"photo\",\"sizes\":{\"large\":{\"w\":1024,\"h\":575,\"resize\":\"fit\"},\"small\":{\"w\":340,\"h\":191,\"resize\":\"fit\"},\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"medium\":{\"w\":600,\"h\":337,\"resize\":\"fit\"}}}]}");
					//	builder.append("'}]}");
					} else {
						builder.append("',\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[],\"media\":[]}");
					}
				//	"entities":{"hashtags":[],"symbols":[],"urls":[],"user_mentions":[],"media":[{"indices":[-1],"media_url":"http:\/\/pbs.twimg.com\/media\/BwOgUtuIQAAvPUJ.png","type":"photo","sizes":{"large":{"w":1024,"h":575,"resize":"fit"},"small":{"w":340,"h":191,"resize":"fit"},"thumb":{"w":150,"h":150,"resize":"crop"},"medium":{"w":600,"h":337,"resize":"fit"}}}]}
					
					
					
					builder.append(",user={name='");
					builder.append(author);
					builder.append("', profile_image_url='");
					builder.append(pictureUrl);
					builder.append("'}}");
			//		Log.i("DEBUG", "BBB: " + builder.toString());
					Status insert = TwitterObjectFactory.createStatus(builder
							.toString());
					list.add(insert);
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}

			}
			if (cursor != null) {
				cursor.close();
			}

		}

	}

	public int updateFromDb() {
		int result = 0;
		String[] projection = { TweetDatabaseOpenHelper.Tweets.COLUMN_AUTHOR,
				TweetDatabaseOpenHelper.Tweets.COLUMN_TEXT,
				TweetDatabaseOpenHelper.Tweets.COLUMN_PICTURE,
				TweetDatabaseOpenHelper.Tweets.COLUMN_DATE,
				TweetDatabaseOpenHelper.Tweets.COLUMN_ID,
				TweetDatabaseOpenHelper.Tweets.COLUMN_MEDIA };

		ContentResolver resolver = mContext.getContentResolver();

		Cursor cursor = null;
		if (mCurrentTimelineType == HOME_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
					projection, TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "<"
							+ list.get(list.size() - 1).getId(), null,
					"LIMIT 100");
		} else if (mCurrentTimelineType == USER_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
					projection, TweetDatabaseOpenHelper.Tweets.COLUMN_ID + "<"
							+ list.get(list.size() - 1).getId(), null,
					"LIMIT 100");
		}
		if (cursor != null) {
			result = cursor.getCount();
			while (cursor.moveToNext()) {
				String author = cursor.getString(cursor
						.getColumnIndex(projection[0]));
				String text = cursor.getString(cursor
						.getColumnIndex(projection[1]));
				String pictureUrl = cursor.getString(cursor
						.getColumnIndex(projection[2]));
				String date = cursor.getString(cursor
						.getColumnIndex(projection[3]));
				long id = cursor.getLong(cursor.getColumnIndex(projection[4]));
				String media = cursor.getString(cursor
						.getColumnIndex(projection[5]));

				try {
					StringBuilder builder = new StringBuilder();
					builder.append("{text='");
					builder.append(text);
					builder.append("', id='");
					builder.append(id);
					builder.append("', created_at='");
					builder.append(date);
					Log.i("DEBUG", "media: " + media);
					if (!"".equals(media)) {
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
					Status insert = TwitterObjectFactory.createStatus(builder
							.toString());
					list.add(insert);
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}

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

	public Twitter getTwitter() {
		return mTwitter;
	}

	public static void setDefaultTimeline(Timeline timeline) {
		sTimeline = timeline;
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

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(BaseAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }
}
