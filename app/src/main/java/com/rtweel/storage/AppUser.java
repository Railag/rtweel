package com.rtweel.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by root on 17.4.15.
 */
public class AppUser {

    public static final String APP_USER_NAME = "app_user_name";
    public static final String APP_SCREEN_NAME = "app_screen_name";
    public static final String APP_USER_ID = "app_user_id";

    private static String sUserName;
    private static String sScreenUserName;
    private static long sUserId = 0l;

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

    public static void setUserName(String name) {
        sUserName = name;
    }

    public static void setScreenUserName(String screenName) {
        sScreenUserName = screenName;
    }

    public static void setUserId(long id) {
        sUserId = id;
    }

}
