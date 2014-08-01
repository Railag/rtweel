package com.rtweel.tweet;

import java.util.ArrayList;
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import com.rtweel.asynctasks.DbWriteTask;
import com.rtweel.cache.App;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

public class Timeline implements Iterable<Status> {
	public static final int USER_TIMELINE = 0;
	public static final int HOME_TIMELINE = 1;

	public static final int UP_TWEETS = 0;
	public static final int DOWN_TWEETS = 1;
	public static final int INITIALIZATION_TWEETS = 2;

	private static int mTweetsCount = 0;
	private static int mTweetsPerPage = 100;

	private List<twitter4j.Status> list;

	private int mCurrentTimelineType;
	private final Context mContext;

	public Timeline(Context context) {
		mCurrentTimelineType = HOME_TIMELINE;
		list = new ArrayList<twitter4j.Status>();
		mContext = context;
	}

	public void loadTimeline() {
		preparingUpdate();

		if (list.isEmpty()) {
			App app = (App) mContext;
			if (!app.isOnline()) {
				Log.i("DEBUG", "No network in loadTimeline()");
				return;
			}
			Log.i("DEBUG", "loading timeline");
			list.addAll(downloadTimeline(mTweetsCount, mTweetsPerPage,
					Timeline.INITIALIZATION_TWEETS));

			mTweetsCount += list.size();
			new DbWriteTask(mContext, list, mCurrentTimelineType).execute();
		}

	}

	public void updateTimelineUp(List<Status> downloadedList) {

		if (downloadedList.isEmpty()) {
			// Toast.makeText(mContext, "No new tweets",
			// Toast.LENGTH_LONG).show();
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

		int prevSize = list.size();
		list.addAll(downloadedList);
		if (list.size() - prevSize != 0) {
			mTweetsCount += list.size() - prevSize;
		} else {
			// Toast.makeText(mContext, "No old tweets", Toast.LENGTH_LONG)
			// .show();
		}
		new DbWriteTask(mContext, downloadedList, mCurrentTimelineType)
				.execute();
	}

	public List<twitter4j.Status> downloadTimeline(int tweetsCount,
			int tweetsPerPage, int flag) {
		Log.i("DEBUG", "downloading down");

		List<twitter4j.Status> downloadedList = null;

		try {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			String accessTokenString = sharedPreferences.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
			String accessTokenSecret = sharedPreferences.getString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
			Log.i("DEBUG", "1");
			if (accessTokenString != null && accessTokenSecret != null) {
				AccessToken accessToken = new AccessToken(accessTokenString,
						accessTokenSecret);
				Twitter twitter = TwitterUtil.getInstance().getTwitterFactory()
						.getInstance(accessToken);

				Paging page = new Paging();
				page.setCount(tweetsPerPage);
				if (tweetsCount <= tweetsPerPage && tweetsCount != 0) {
					tweetsCount = tweetsPerPage * 2;
				}
				if (tweetsCount == 0) {
					page.setPage(1);
				} else {
					if (tweetsCount % tweetsPerPage == 0) {
						page.setPage(tweetsCount / tweetsPerPage);
					} else {
						Float f = (float) tweetsCount / tweetsPerPage;
						page.setPage(f.intValue() + 1 + 1);
					}
				}
				switch (flag) {
				case INITIALIZATION_TWEETS:
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
					downloadedList = twitter.getHomeTimeline(page);
					break;
				}
				case Timeline.USER_TIMELINE: {
					downloadedList = twitter.getUserTimeline(page);
					break;
				}
				}
			}

		} catch (TwitterException e) {
			e.printStackTrace();
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
		mTweetsCount = 0;
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
				TweetDatabaseOpenHelper.Tweets.COLUMN_ID };

		ContentResolver resolver = mContext.getContentResolver();

		Cursor cursor = null;
		if (mCurrentTimelineType == HOME_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_HOME_DB,
					projection, null, null, null);
		} else if (mCurrentTimelineType == USER_TIMELINE) {
			cursor = resolver.query(
					TweetDatabaseOpenHelper.Tweets.CONTENT_URI_USER_DB,
					projection, null, null, null);
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

				try {
					String creation = "{text='" + text + "', id='" + id
							+ "', created_at='" + date + "',user={name='"
							+ author + "', profile_image_url='" + pictureUrl
							+ "'}}";

					Status insert = TwitterObjectFactory.createStatus(creation);
					list.add(insert);
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}

			}
			mTweetsCount += list.size();
			if (cursor != null) {
				cursor.close();
			}

		}

	}

	public static int getTweetsPerPage() {
		return mTweetsPerPage;
	}

	public static int getTweetsCount() {
		return mTweetsCount;
	}
}
