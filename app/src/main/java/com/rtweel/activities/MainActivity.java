package com.rtweel.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.rtweel.R;
import com.rtweel.asynctasks.timeline.LoadTimelineTask;
import com.rtweel.asynctasks.timeline.TimelineDownTask;
import com.rtweel.asynctasks.timeline.TimelineUpTask;
import com.rtweel.asynctasks.tweet.DeleteTweetTask;
import com.rtweel.asynctasks.tweet.RefreshTweetTask;
import com.rtweel.cache.App;
import com.rtweel.constant.Extras;
import com.rtweel.services.TweetService;
import com.rtweel.settings.SettingActivity;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;
import com.rtweel.tweet.TweetAdapter;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterGetAccessTokenTask;
import com.rtweel.twitteroauth.TwitterUtil;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import twitter4j.Status;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public class MainActivity extends ActionBarActivity {

    public static final int EDIT_REQUEST = 0;
    public static final int DELETE_REQUEST = 1;

    private BaseAdapter adapter;

    private AdapterView<ListAdapter> mAdapter;

    private Timeline mTimeline;

    private ListView list;
    private ProgressBar mLoadingBar;

    private boolean mContentLoaded;

    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

		/*
         * Login Check
		 */
        if (loginCheck()) {
            initialize();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLoggedIn()) {
            if (isLoading())
                stopLoading();
            if (loginCheck())
                initialize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int requestResult,
                                    Intent intent) {

        if (requestCode == EDIT_REQUEST) {
            if (requestResult == RESULT_OK) {
                adapter.notifyDataSetChanged();
            }
        }
        if (requestCode == DELETE_REQUEST) {
            if (requestResult == RESULT_OK) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_home_timeline: {

                App app = (App) getApplication();
                if (!app.isOnline()) {
                    Log.i("DEBUG", "home timeline button onClick NO NETWORK");
                    Toast.makeText(getApplicationContext(),
                            "No network connection, couldn't load tweets!",
                            Toast.LENGTH_LONG).show();
                    break;
                }
                if (mTimeline.getCurrentTimelineType() != Timeline.HOME_TIMELINE) {

                    mTimeline.clear();

                    mTimeline.setTimelineType(Timeline.HOME_TIMELINE);
                    list.setVisibility(View.GONE);
                    crossfade();
                    Log.i("DEBUG", "Updating home timeline...");
                    new LoadTimelineTask(this).execute(mTimeline);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "It's your current timeline!", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            }
            case R.id.switch_user_timeline: {
                App app = (App) getApplication();
                if (!app.isOnline()) {
                    Log.i("DEBUG", "user timeline button onClick NO NETWORK");
                    Toast.makeText(getApplicationContext(),
                            "No network connection, couldn't load tweets!",
                            Toast.LENGTH_LONG).show();
                    break;
                }
                if (mTimeline.getCurrentTimelineType() != Timeline.USER_TIMELINE) {
                    mTimeline.clear();

                    mTimeline.setTimelineType(Timeline.USER_TIMELINE);
                    list.setVisibility(View.GONE);
                    crossfade();
                    Log.i("DEBUG", "Updating user timeline...");
                    new LoadTimelineTask(this).execute(mTimeline);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "It's your current timeline!", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            }
            case R.id.tweet_send_open: {
                Intent intent = new Intent(MainActivity.this,
                        SendTweetActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.logout_button: {
                App app = (App) getApplication();

                boolean dbDeleted = deleteDatabase(TweetDatabaseOpenHelper
                        .getDbName());
                Log.i("DEBUG", "DB DELETED = " + dbDeleted);

                app.createDb();

                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                editor.putString(
                        ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
                editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,
                        false);
                editor.commit();

                TwitterUtil.getInstance().reset();
                finish();
                break;
            }
            case R.id.settings_button: {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (findViewById(R.id.list) != null) {
            getMenuInflater().inflate(R.menu.main, menu);
        } else {
            getMenuInflater().inflate(R.menu.login, menu);
        }
        return true;
    }

    private void initialize() {
        if (isLoading())
            stopLoading();

        Date date = new Date();

        Log.i("DEBUG", "Initializing...");

        mTimeline = new Timeline(getApplicationContext());

        Timeline.setDefaultTimeline(mTimeline);

        Intent serviceIntent = new Intent(this, TweetService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager
                .setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()
                                + AlarmManager.INTERVAL_HALF_HOUR,
                        AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
        Date t1 = new Date();
        Log.i("DEBUG", "Before loadtimelinetask: " + 0);
        new LoadTimelineTask(this).execute(mTimeline);

        Log.i("DEBUG",
                "After loadtimelinetask started: "
                        + String.valueOf(new Date().getTime() - t1.getTime()));
        // getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


        getSupportActionBar().show();

        list = (ListView) findViewById(R.id.list);

        mLoadingBar = (ProgressBar) findViewById(R.id.loading);

        list.setVisibility(View.GONE);
        crossfade();

        list.setOnTouchListener(new OnTouchListener() {

            float x1
                    ,
                    x2;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {

                    case MotionEvent.ACTION_DOWN: {
                        x1 = event.getX();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        x2 = event.getX();

                        float distance = x2 - x1;

                        if (distance > 150) { // Right swipe
                            // AnimationSet set = new AnimationSet(false);
                            // set.
                            rotate();
                            App app = (App) getApplication();
                            if (!app.isOnline()) {
                                Log.i("DEBUG", "Right swipe NO NETWORK");
                                Toast.makeText(
                                        getApplicationContext(),
                                        "No network connection, couldn't load tweets!",
                                        Toast.LENGTH_LONG).show();
                                return true;
                            }
                            Log.i("DEBUG", "SWIPE RIGHT");

                            new TimelineUpTask(MainActivity.this)
                                    .execute(mTimeline);

                            // TODO Some scrolling up

                            // Scroller scroller = new
                            // Scroller(getApplicationContext());
                            // scroller.startScroll((int)x2, (int)event.getY(), 0,
                            // -800);
                        }
                        if (distance < -150) { // Left Swipe
                            rotate();
                            App app = (App) getApplication();
                            if (!app.isOnline()) {
                                Log.i("DEBUG", "Left swipe NO NETWORK");
                                Toast.makeText(
                                        getApplicationContext(),
                                        "No network connection, couldn't load tweets!",
                                        Toast.LENGTH_LONG).show();
                                return true;
                            }
                            Log.i("DEBUG", "SWIPE LEFT");

                            new TimelineDownTask(MainActivity.this)
                                    .execute(mTimeline);
                            // TODO Some scrolling up
                        }
                        break;
                    }
                }
                return false;
            }
        });
        t1 = new Date();
        mAdapter = list;

        adapter = new TweetAdapter(mTimeline, getApplicationContext());

        mAdapter.setAdapter(adapter);

        mTimeline.setAdapter(adapter);
        Log.i("DEBUG",
                "tweetAdapter time: " + (new Date().getTime() - t1.getTime()));

        mAdapter.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Status tweet = (twitter4j.Status) adapter.getItem(position);
                Log.i("DEBUG", "tweet retweets: " + tweet.getRetweetCount());
                new RefreshTweetTask(MainActivity.this, position).execute(tweet.getId());
                startLoading(getResources().getString(R.string.tweet_refreshing));
            }
        });
        Log.i("DEBUG",
                "initialize time: " + (new Date().getTime() - date.getTime()));
    }

    private boolean loginCheck() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (!sharedPreferences.getBoolean(
                ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, false)) {
            startLoading(getResources().getString(R.string.authorization));

            try {
                Uri uri = getIntent().getData();
                if (uri != null
                        && uri.toString().startsWith(
                        ConstantValues.TWITTER_CALLBACK_URL)) {
                    String verifier = uri
                            .getQueryParameter(ConstantValues.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
                    Log.i("DEBUG", "Verification..");
                    new TwitterGetAccessTokenTask(getApplicationContext())
                            .execute(verifier).get();

                    initialize();
                } else {
                    Log.i("DEBUG", "Browser authentification...");
                    new TwitterAuthenticateTask().execute();
                }
            } catch (Exception e) {
                Log.i("DEBUG", e.toString());
                e.printStackTrace();
            }
            return false;

        } else {
            return true;
        }
    }

    class TwitterAuthenticateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(TwitterUtil.getInstance().getRequestToken()
                            .getAuthenticationURL()));
            startActivity(intent);
            return null;
        }
    }

    public void crossfade() {
        mContentLoaded = !mContentLoaded;

        final View showView = mContentLoaded ? mLoadingBar : list;
        final View hideView = mContentLoaded ? list : mLoadingBar;

        ViewHelper.setAlpha(list, 0f);

        showView.setVisibility(View.VISIBLE);

        int mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        animate(showView).alpha(1f).setDuration(mShortAnimationDuration)
                .setListener(null);

        animate(hideView).alpha(0f).setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });
    }

    private void rotate() {
        RotateAnimation anim = new RotateAnimation(0, 360, 0, 0);
        anim.startNow();
        anim.setDuration(4000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        list.setAnimation(anim);
    }

    public void startLoading(String loadingText) {
        mLoadingDialog = ProgressDialog.show(this, loadingText, getResources().getString(R.string.loading));
    }

    public void stopLoading() {
        mLoadingDialog.dismiss();
    }

    public boolean isLoading() {
        return mLoadingDialog != null && mLoadingDialog.isShowing();
    }

    public ListView getList() {
        return list;
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    public AdapterView<ListAdapter> getAdapterView() {
        return mAdapter;
    }

    public void setAdapterView(ListView list) {
        mAdapter = list;
    }

    public Timeline getTimeline() {
        return mTimeline;
    }

    public boolean isLoggedIn() {
        return list != null;
    }
}
