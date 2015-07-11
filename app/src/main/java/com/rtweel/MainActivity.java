package com.rtweel;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rtweel.detail.DetailFragment;
import com.rtweel.direct.ChatFragment;
import com.rtweel.direct.DirectMessagesMainFragment;
import com.rtweel.fragments.HomeTweetFragment;
import com.rtweel.fragments.LoginFragment;
import com.rtweel.fragments.SendTweetFragment;
import com.rtweel.fragments.SettingsFragment;
import com.rtweel.fragments.WebViewFragment;
import com.rtweel.profile.MainProfileFragment;
import com.rtweel.services.TweetService;
import com.rtweel.storage.App;
import com.rtweel.storage.AppUser;
import com.rtweel.tasks.tweet.SearchTask;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;

    private Fragment mCurrentFragment;

    private ProgressBar mLoadingBar;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private ListView mDrawerList;

    private ArrayList<NavItem> mDrawerItems;

    private Toolbar mToolbar;

    private SearchTask mSearchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mFragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initDrawer();

        initToolbar();

        initToggle();

        updateTweetService();

        mLoadingBar = (ProgressBar) findViewById(R.id.loading);

        setMainFragment(new LoginFragment());

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent.hasExtra(TweetService.LOCATION)) {
            processPN (intent.getSerializableExtra(TweetService.LOCATION));
        }

        Uri uri = intent.getData();
        if (uri != null) {
            if (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
                loadUrl(uri.toString()); //todo fix some uris not loaded
        }
    }

    private void processPN(Serializable location) {
        TweetService.DESTINATION destination = (TweetService.DESTINATION) location;
        switch (destination) {
            case MESSAGES:
                setMainFragment(new DirectMessagesMainFragment());
                break;
            case MENTIONS:
                MainProfileFragment fragment = new MainProfileFragment();
                Bundle args = new Bundle();
                args.putString(Const.USERNAME, AppUser.getUserName(MainActivity.this));
                args.putString(Const.SCREEN_USERNAME, AppUser.getScreenUserName(MainActivity.this));
                args.putLong(Const.USER_ID, AppUser.getUserId(MainActivity.this));
                args.putBoolean(Const.OPEN_MENTIONS, true);
                fragment.setArguments(args);
                setMainFragment(fragment);
                break;
        }
    }

    @Override
    protected void onResume() {
        if (!App.isOnline(this)) {
            Log.i("DEBUG", "App no network");
            Toast.makeText(this, getString(R.string.bad_connection_message), Toast.LENGTH_LONG).show();
        }
        super.onResume();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setTitle(getString(R.string.title_home));
        mToolbar.setBackgroundColor(getResources().getColor(R.color.celadon));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        View footer = LayoutInflater.from(this).inflate(R.layout.drawer_footer, null, false);
        mDrawerList.addFooterView(footer);
        final EditText footerEdit = (EditText) footer.findViewById(R.id.goToProfile_input);
        Button footerButton = (Button) footer.findViewById(R.id.goToProfile_button);
        footerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(footerEdit.getText()))
                    return;

                mDrawerLayout.closeDrawers();
                hideKeyboard();

                MainProfileFragment fragment = new MainProfileFragment();
                Bundle args = new Bundle();
                args.putString(Const.SCREEN_USERNAME, footerEdit.getText().toString());
                fragment.setArguments(args);
                setMainFragment(fragment);
            }
        });

        final AutoCompleteTextView actv = (AutoCompleteTextView) footer.findViewById(R.id.search_field);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;

        actv.setDropDownWidth(width);
        actv.setDropDownHeight(height / 2);

        final SearchAdapter adapter = new SearchAdapter(this);

        actv.setAdapter(adapter);

        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() < 3)
                    return;

                if (mSearchTask != null && !mSearchTask.getStatus().equals(AsyncTask.Status.FINISHED))
                    mSearchTask.cancel(true);

                mSearchTask = new SearchTask(MainActivity.this, adapter, actv);
                mSearchTask.execute(s.toString());
            }
        });

        actv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (actv.getText().length() < 3)
                    return false;

                if (mSearchTask != null && !mSearchTask.getStatus().equals(AsyncTask.Status.FINISHED))
                    mSearchTask.cancel(true);

                mSearchTask = new SearchTask(MainActivity.this, adapter, actv);
                mSearchTask.execute(actv.getText().toString());

                return false;
            }
        });

        ArrayList<String> drawerTitles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.drawer_items)));
        mDrawerItems = new ArrayList<>();
        for (String s : drawerTitles)
            mDrawerItems.add(new NavItem(s, getResources().getDrawable(R.drawable.rtweel)));

        mDrawerList.setAdapter(new NavAdapter(this, mDrawerItems));

        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        MainProfileFragment fragment = new MainProfileFragment();
                        Bundle args = new Bundle();
                        args.putString(Const.USERNAME, AppUser.getUserName(MainActivity.this));
                        args.putString(Const.SCREEN_USERNAME, AppUser.getScreenUserName(MainActivity.this));
                        args.putLong(Const.USER_ID, AppUser.getUserId(MainActivity.this));
                        fragment.setArguments(args);
                        setMainFragment(fragment);
                        break;
                    case 1:
                        setMainFragment(new HomeTweetFragment());
                        break;
                    case 2:
                        setMainFragment(new SendTweetFragment());
                        break;
                    case 3:
                        setMainFragment(new DirectMessagesMainFragment());
                        break;
                    case 4:
                        setMainFragment(new com.rtweel.trends.TrendsFragment());
                        break;
                    case 5:
                        setMainFragment(new SettingsFragment());
                        break;
                }

                mDrawerLayout.closeDrawers();
            }
        });

    }


    private void initToggle() {
        mToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_closed
        ) {

        };

        mDrawerLayout.setDrawerListener(mToggle);

        getSupportActionBar().setDisplayShowTitleEnabled(true);


        setTitleClickable();


        mToggle.syncState();
    }


    public void updateTweetService() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAlreadySet = prefs.contains(SettingsFragment.PN_INTERVAL);
        int intervalInMinutes;
        if (isAlreadySet)
            intervalInMinutes = prefs.getInt(SettingsFragment.PN_INTERVAL, 4 * 60);
        else
            intervalInMinutes = 60;

        long intervalInMillis = TimeUnit.MINUTES.toMillis(intervalInMinutes);
        Intent serviceIntent = new Intent(this, TweetService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager
                .setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()
                                + intervalInMillis,
                        intervalInMillis, alarmIntent);
    }


    public void setMainFragment(final Fragment fragment) {

        final FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        if (!(fragment instanceof LoginFragment || mCurrentFragment instanceof LoginFragment))
            fragmentTransaction.addToBackStack(null);

        fragmentTransaction.replace(R.id.main_frame, fragment).commit();

        if (mCurrentFragment instanceof DetailFragment)
            show();

        mCurrentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (mFragmentManager.getBackStackEntryCount() == 0)
            super.onBackPressed();
        else {
            if (mCurrentFragment instanceof WebViewFragment) {
                WebViewFragment wv = (WebViewFragment) mCurrentFragment;
                if (wv.isCanGoBack()) {
                    wv.goBack();
                    return;
                }
            }

            if (mCurrentFragment instanceof ChatFragment) {
                ChatFragment fragment = (ChatFragment) mCurrentFragment;
                if (!fragment.isListShown()) {
                    fragment.showList();
                    return;
                }
            }

            if (mCurrentFragment instanceof DetailFragment)
                show();

            mFragmentManager.popBackStackImmediate();

            List<Fragment> fragments = mFragmentManager.getFragments();
            if (!fragments.isEmpty())
                mCurrentFragment = fragments.get(fragments.size() - 2);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    private void setTitleClickable() {
        try {
            Field titleField = Toolbar.class.getDeclaredField("mTitleTextView");
            titleField.setAccessible(true);
            final TextView barTitleView = (TextView) titleField.get(mToolbar);
            barTitleView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int color = getResources().getColor(R.color.eucalyptus);
                    final ObjectAnimator animator = ObjectAnimator.ofInt(barTitleView, "textColor", color, Color.BLACK);
                    animator.setDuration(300);
                    animator.setEvaluator(new ArgbEvaluator());
                    animator.start();

                    if (mDrawerLayout.isDrawerOpen(mDrawerList))
                        mDrawerLayout.closeDrawers();
                    else
                        mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void loadUrl(String url) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(Const.URL, url);
        fragment.setArguments(args);
        setMainFragment(fragment);
    }

    public ProgressBar getLoadingBar() {
        return mLoadingBar;
    }

    public boolean isLoggedIn() {
        return mCurrentFragment != null;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void hide() {
        getSupportActionBar().hide();
    }

    public void show() {
        getSupportActionBar().show();
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void hideNav() {
        mDrawerLayout.closeDrawers();
    }

    public void showLoadingBar() {
        mLoadingBar.setVisibility(View.VISIBLE);
    }

    public void hideLoadingBar() {
        mLoadingBar.setVisibility(View.GONE);
    }
}
