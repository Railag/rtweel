package com.rtweel.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.rtweel.fragments.BaseFragment;
import com.rtweel.fragments.LoginFragment;
import com.rtweel.fragments.SendTweetFragment;
import com.rtweel.fragments.TimelineFragment;
import com.rtweel.services.TweetService;
import com.rtweel.settings.SettingActivity;
import com.rtweel.sqlite.TweetDatabaseOpenHelper;
import com.rtweel.tweet.Timeline;
import com.rtweel.tweet.TweetAdapter;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterGetAccessTokenTask;
import com.rtweel.twitteroauth.TwitterUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import twitter4j.Status;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public class MainActivity extends ActionBarActivity {

    private FragmentManager mFragmentManager;

    private BaseFragment mCurrentFragment;

    private ProgressBar mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mFragmentManager = getFragmentManager();

        mLoadingBar = (ProgressBar) findViewById(R.id.loading);

        setMainFragment(new LoginFragment());

        getSupportActionBar().hide();
    }

    public void setMainFragment(final BaseFragment fragment) {

        final FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        if( !(fragment instanceof LoginFragment) )
            fragmentTransaction.addToBackStack(null);

        fragmentTransaction.replace(R.id.main_frame, fragment).commit();

        mCurrentFragment = fragment;

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

//        else if (requestCode == REQUEST_FILE_SELECT) {
//            if (resultCode == RESULT_OK) {
//                Uri file = Uri.fromFile(new File(data
//                        .getStringExtra(Extras.FILE_URI)));
//                Log.i("DEBUG", file.toString());
//                Bitmap bitmap = BitmapFactory.decodeFile(data
//                        .getStringExtra(Extras.FILE_URI));
//                Log.i("DEBUG", data.getStringExtra(Extras.FILE_URI));
//                mTweetPicture.setImageBitmap(bitmap);
//            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(getApplicationContext(), "File choosing failed",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    public void onBackPressed() {
        if(mCurrentFragment.isLoading())
            mCurrentFragment.stopLoading();
        else {
            if(mFragmentManager.getBackStackEntryCount() == 1)
                super.onBackPressed();
            else
                mFragmentManager.popBackStackImmediate();
        }
    }

    public ProgressBar getLoadingBar() {
        return mLoadingBar;
    }

    public boolean isLoggedIn() {
        return mCurrentFragment != null;
    }
}
