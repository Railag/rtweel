package com.rtweel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rtweel.R;

/**
 * Created by root on 25.3.15.
 */
public class ProfileFragment extends BaseFragment {
    //TODO

    private final static int PAGER_SIZE = 3;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private PagerTabStrip mPagerTabStrip;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitle(getString(R.string.title_profile));
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        mPager = (ViewPager) v.findViewById(R.id.pager);

        mPagerTabStrip = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);


        mPagerAdapter = new FragmentStatePagerAdapter(getMainActivity().getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new TimelineFragment();
                    case 1:
                        return new SettingsFragment();
                    case 2:
                        return new SendTweetFragment();
                    default:
                        return new TimelineFragment();
                }
            }

            @Override
            public int getCount() {
                return PAGER_SIZE;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "Fragment" + position;
            }
        };

        mPager.setAdapter(mPagerAdapter);

        return v;
    }




}
