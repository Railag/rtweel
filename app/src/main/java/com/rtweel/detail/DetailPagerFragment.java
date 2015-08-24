package com.rtweel.detail;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.fragments.BaseFragment;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by firrael on 05.08.2015.
 */
public class DetailPagerFragment extends BaseFragment implements Hide {
    private View v;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private ArrayList<Status> items;
    private int selectedTweet;

    @Nullable
    @Override
    protected String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_detail_pager, null, false);
        mPager = (ViewPager) v.findViewById(R.id.detail_pager);
        setRetainInstance(true);
        return v;
    }

    private void initPager() {
        mPagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                Status tweet = items.get(position);

                DetailFragment fragment = new DetailFragment();
                Bundle args = new Bundle();
                args.putSerializable(Const.TWEET, tweet);
                args.putInt(Const.TWEET_POSITION, position);
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return items.get(position).getUser().getName();
            }

            @Override
            public Parcelable saveState() {
                return null;
            }
        };

        mPager.setAdapter(mPagerAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int position) {
                getArguments().putInt(Const.TWEET_POSITION, position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mPager.setCurrentItem(selectedTweet);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            items = (ArrayList<Status>) args.getSerializable(Const.TWEET_LIST);
            selectedTweet = args.getInt(Const.TWEET_POSITION);
            initPager();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public DetailFragment getItem(int position) {
        return (DetailFragment) mPagerAdapter.instantiateItem(mPager, position);
    }
}
