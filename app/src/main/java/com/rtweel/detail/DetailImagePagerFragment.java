package com.rtweel.detail;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

/**
 * Created by firrael on 7.5.15.
 */
public class DetailImagePagerFragment extends BaseFragment {
    private View v;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private ArrayList<String> mediaList;
    private int selectedMedia;
    private Rect startRect;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_detail_image_pager, null, false);
        mPager = (ViewPager) v.findViewById(R.id.detail_image_pager);
        setRetainInstance(true);
        return v;
    }

    private void initPager() {
        mPagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                DetailImageFragment fragment = new DetailImageFragment();
                Bundle args = new Bundle();
                args.putString(Const.MEDIA_URL, mediaList.get(position));
                ArrayList<Integer> points = new ArrayList<>();
                points.add(startRect.left);
                points.add(startRect.top);
                points.add(startRect.right);
                points.add(startRect.bottom);
                args.putIntegerArrayList(Const.IMAGE_RECT, points);
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public int getCount() {
                return mediaList.size();
            }
        };

        mPager.setAdapter(mPagerAdapter);

        mPager.setCurrentItem(selectedMedia);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            mediaList = args.getStringArrayList(Const.MEDIA_LIST);
            selectedMedia = args.getInt(Const.SELECTED_MEDIA);
            ArrayList<Integer> points = args.getIntegerArrayList(Const.IMAGE_RECT);
            startRect = new Rect(points.get(0), points.get(1), points.get(2), points.get(3));
            initPager();
        }
    }

    @Override
    protected String getTitle() {
        return null;
    }
}
