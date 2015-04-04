package com.rtweel.activities;

import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rtweel.R;
import com.rtweel.fragments.LoginFragment;
import com.rtweel.fragments.ProfileFragment;
import com.rtweel.fragments.SendTweetFragment;
import com.rtweel.fragments.SettingsFragment;
import com.rtweel.fragments.TimelineFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends ActionBarActivity {

    private FragmentManager mFragmentManager;

    private Fragment mCurrentFragment;

    private ProgressBar mLoadingBar;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private ListView mDrawerList;

    private ArrayList<String> mDrawerItems;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mFragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        initDrawer();

        initToolbar();

        initToggle();

        mLoadingBar = (ProgressBar) findViewById(R.id.loading);

        setMainFragment(new LoginFragment());

    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setTitle("Home");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initDrawer() {
        mDrawerItems = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.drawer_items)));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_item, mDrawerItems));

        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setMainFragment(new ProfileFragment());
                        break;
                    case 1:
                        setMainFragment(new TimelineFragment());
                        break;
                    case 2:
                        setMainFragment(new SendTweetFragment());
                        break;
                    case 3:
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


    public void setMainFragment(final Fragment fragment) {

        final FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        if (!(fragment instanceof LoginFragment))
            fragmentTransaction.addToBackStack(null);

        fragmentTransaction.replace(R.id.main_frame, fragment).commit();

        mCurrentFragment = fragment;

    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 1)
            super.onBackPressed();
        else
            mFragmentManager.popBackStackImmediate();
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
                    ObjectAnimator animator = new ObjectAnimator();
                    animator.setFloatValues(1.0f, 0.5f, 1.0f);
                    animator.setDuration(600);
                    animator.setPropertyName("alpha");
                    animator.setTarget(barTitleView);
                    animator.start();

                    if (mDrawerLayout.isDrawerOpen(mDrawerList))
                        mDrawerLayout.closeDrawers();
                    else
                        mDrawerLayout.openDrawer(Gravity.START);
                }
            });

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
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
}
