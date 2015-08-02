package com.rtweel.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by firrael on 17.4.15.
 */
public class AppUser {

    public static final String APP_USER_NAME = "app_user_name";
    public static final String APP_SCREEN_NAME = "app_screen_name";
    public static final String APP_USER_ID = "app_user_id";
    public static final String APP_USER_LAST_TWEET_ID = "app_user_last_tweet_id";
    public static final String APP_USER_LAST_TWEET_TIME = "app_user_last_tweet_time";

    private static String sUserName;
    private static String sScreenUserName;
    private static long sUserId = 0l;
    private static long sUserLastTweetId = 0l;
    private static long sUserLastTweetTime = 0l;

    public static String getUserName(Context context) {
        if (sUserName != null)
            return sUserName;
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(APP_USER_NAME, "");
        }
    }

    public static long getUserId(Context context) {
        if (sUserId != 0l)
            return sUserId;
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getLong(APP_USER_ID, 0l);
        }
    }

    public static String getScreenUserName(Context context) {
        if (sScreenUserName != null)
            return sScreenUserName;
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(APP_SCREEN_NAME, "");
        }
    }

    public static long getLastUserTweetId(Context context) {
        if (sUserLastTweetId != 0l)
            return sUserLastTweetId;
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getLong(APP_USER_LAST_TWEET_ID, 0l);
        }
    }

    public static long getLastUserTweetTime(Context context) {
        if (sUserLastTweetTime != 0l)
            return sUserLastTweetTime;
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getLong(APP_USER_LAST_TWEET_TIME, 0l);
        }
    }

    public static void setUserName(String name) {
        sUserName = name;
    }

    public static void setScreenUserName(String screenName) {
        sScreenUserName = screenName;
    }

    public static void setUserId(long id) {
        sUserId = id;
    }

    public static void setLastTweetId(long id) {
        sUserLastTweetId = id;
    }

    public static void setLastTweetTime(long time) {
        sUserLastTweetTime = time;
    }

    public static boolean showLastTweet(Context context) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(getLastUserTweetTime(context));
        Calendar currentTime = Calendar.getInstance(Locale.getDefault());
        currentTime.setTime(new Date());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int currentHours = currentTime.get(Calendar.HOUR_OF_DAY);

        return Math.abs(hours - currentHours) < 2;
    }
}