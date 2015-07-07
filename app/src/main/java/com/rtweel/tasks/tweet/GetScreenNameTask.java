package com.rtweel.tasks.tweet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.rtweel.timelines.Timeline;
import com.rtweel.storage.AppUser;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class GetScreenNameTask extends AsyncTask<Twitter, Void, Void> {

    private final Context mContext;
    private final Timeline mTimeline;
    private String mScreenUserName;

    public GetScreenNameTask(Context context, Timeline timeline, String screenUserName) {
        mTimeline = timeline;
        mContext = context;
        mScreenUserName = screenUserName;
    }

    @Override
    protected Void doInBackground(Twitter... params) {
        Twitter twitter = params[0];
        try {
            String userName;
            long userId;
            User user;

            if (mScreenUserName == null)
                mScreenUserName = twitter.getScreenName();

            user = twitter.showUser(mScreenUserName);
            userName = user.getName();
            userId = user.getId();


            if (mContext != null) {
                //for some different user
                if (mTimeline != null) {
                    mTimeline.setScreenUserName(mScreenUserName);
                    mTimeline.setUserName(userName);
                    mTimeline.setUserId(userId);
                } else {
                    //for app user
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(AppUser.APP_SCREEN_NAME, mScreenUserName);
                    editor.putString(AppUser.APP_USER_NAME, userName);
                    editor.putLong(AppUser.APP_USER_ID, userId);
                    editor.commit();

                    AppUser.setUserName(userName);
                    AppUser.setScreenUserName(mScreenUserName);
                    AppUser.setUserId(userId);
                }
            }
        } catch (IllegalStateException | TwitterException e) {
            e.printStackTrace();
        }

        return null;
    }

}
