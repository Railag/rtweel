package com.rtweel;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rtweel.detail.DetailFragment;
import com.rtweel.direct.DirectMessagesMainFragment;
import com.rtweel.fragments.HomeTweetFragment;
import com.rtweel.fragments.LoginFragment;
import com.rtweel.profile.MainProfileFragment;
import com.rtweel.fragments.SendTweetFragment;
import com.rtweel.fragments.SettingsFragment;
import com.rtweel.fragments.WebViewFragment;
import com.rtweel.services.TweetService;
import com.rtweel.storage.AppUser;
import com.rtweel.tasks.tweet.SearchTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private FragmentManager mFragmentManager;

    private Fragment mCurrentFragment;

    private ProgressBar mLoadingBar;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private ListView mDrawerList;

    private ArrayList<NavItem> mDrawerItems;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mFragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initDrawer();

        initToolbar();

        initToggle();

        initTweetService();

        mLoadingBar = (ProgressBar) findViewById(R.id.loading);

        setMainFragment(new LoginFragment());

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            if (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
                loadUrl(uri.toString()); //todo fix some uris not loaded
        }
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
        ArrayList<String> drawerTitles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.drawer_items)));
        mDrawerItems = new ArrayList<>();
        for (String s : drawerTitles)
            mDrawerItems.add(new NavItem(s, getResources().getDrawable(R.drawable.rtweel)));

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
//                args.putString(Const.SCREEN_USERNAME, AppUser.getScreenUserName(MainActivity.this));
//                args.putLong(Const.USER_ID, AppUser.getUserId(MainActivity.this));
                fragment.setArguments(args);
                setMainFragment(fragment);
            }
        });

        final AutoCompleteTextView mactv = (AutoCompleteTextView) footer.findViewById(R.id.search_field);
    //    mactv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        mactv.setAdapter(adapter);

        mactv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchTask task = new SearchTask(MainActivity.this, adapter, mactv);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s.toString());
            }
        });

        mactv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //TODO
            }
        });

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


    private void initTweetService() {
        Intent serviceIntent = new Intent(this, TweetService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager
                .setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()
                                + AlarmManager.INTERVAL_HALF_HOUR,
                        AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
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
            Log.e("Exception", e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("Exception", e.getMessage());
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

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
