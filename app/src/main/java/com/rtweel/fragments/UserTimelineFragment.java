package com.rtweel.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.rtweel.R;
import com.rtweel.sqlite.TweetDatabase;
import com.rtweel.timelines.UserTimeline;
import com.rtweel.asynctasks.timeline.LoadTimelineTask;
import com.rtweel.asynctasks.timeline.TimelineDownTask;
import com.rtweel.asynctasks.timeline.TimelineUpTask;
import com.rtweel.cache.App;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterUtil;

/**
 * Created by root on 5.4.15.
 */
public class UserTimelineFragment extends TimelineFragment {

    private TimelineUpTask mUpTask;
    private TimelineDownTask mDownTask;

    @Override
    protected void loadTweets() {
        new LoadTimelineTask(this).execute(mTimeline);
    }

    @Override
    protected void instantiateTimeline() {
        mTimeline = new UserTimeline(getActivity().getApplicationContext());

    //    Timeline.setDefaultTimeline(mTimeline);
    }

    protected void updateUp() {

        blink();
        if (!App.isOnline(getActivity())) {
            Log.i("DEBUG", "Up swipe NO NETWORK");
            Toast.makeText(
                    getActivity(),
                    "No network connection, couldn't load tweets!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("DEBUG", "SWIPE UP");
        if (mUpTask != null)
            if (!mUpTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mUpTask = new TimelineUpTask(UserTimelineFragment.this);
        mUpTask.execute(mTimeline);

    }

    protected void updateDown() {
        blink();
        if (!App.isOnline(getActivity())) {
            Log.i("DEBUG", "Down swipe NO NETWORK");
            Toast.makeText(
                    getActivity(),
                    "No network connection, couldn't load tweets!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("DEBUG", "SWIPE DOWN");

        if (mDownTask != null)
            if (!mDownTask.getStatus().equals(AsyncTask.Status.FINISHED))
                return;
        mDownTask = new TimelineDownTask(UserTimelineFragment.this);
        mDownTask.execute(mTimeline);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tweet_send_open: {
                getMainActivity().setMainFragment(new SendTweetFragment());
                break;
            }
            case R.id.logout_button: {
                App app = (App) getActivity().getApplication();

                boolean dbDeleted = getActivity().deleteDatabase(TweetDatabase
                        .getDbName());
                Log.i("DEBUG", "DB DELETED = " + dbDeleted);

                app.createDb();

                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                editor.putString(
                        ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
                editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,
                        false);
                editor.commit();

                TwitterUtil.getInstance().reset();
                getMainActivity().finish();
                break;
            }
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }
}
