package com.rtweel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rtweel.R;
import com.rtweel.asynctasks.tweet.GetUserDetailsTask;
import com.rtweel.tweet.Timeline;

/**
 * Created by root on 25.3.15.
 */
public class ProfileFragment extends BaseFragment {
    //TODO

    private final static int PAGER_SIZE = 3;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private TextView mProfileNameNormal;
    private TextView mProfileNameLink;

    private ImageView mBackground;
    private RoundedImageView mLogo;

    private TextView mDescription;


    @Override
    public void onStart() {
        super.onStart();

        mProfileNameNormal.setText(Timeline.getUserName());
        mProfileNameLink.setText(Timeline.getScreenUserName());

        Timeline timeline = Timeline.getDefaultTimeline();
        GetUserDetailsTask task = new GetUserDetailsTask(getActivity(), mBackground, mLogo, mProfileNameNormal, mProfileNameLink, mDescription);
        task.execute(timeline.getTwitter());

        initPagerAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitle(getString(R.string.title_profile));
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        mPager = (ViewPager) v.findViewById(R.id.pager);

        mBackground = (ImageView) v.findViewById(R.id.profile_background);
        mLogo = (RoundedImageView) v.findViewById(R.id.profile_picture);

        mProfileNameNormal = (TextView) v.findViewById(R.id.profile_name_normal);
        mProfileNameLink = (TextView) v.findViewById(R.id.profile_name_link);

        mDescription = (TextView) v.findViewById(R.id.profile_description);

        return v;
    }


    private void initPagerAdapter() {
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
                String title;

                switch (position) {
                    case 0:
                        title = "Home";
                        break;
                    case 1:
                        title = "Settings";
                        break;
                    case 2:
                        title = "Send";
                        break;
                    default:
                        title = "";
                        break;
                }

//                Drawable myDrawable = getResources().getDrawable(R.drawable.placeholder); //TODO spannable with image
//                SpannableStringBuilder sb = new SpannableStringBuilder(title);
//                myDrawable.setBounds(0, 0, myDrawable.getIntrinsicWidth(), myDrawable.getIntrinsicHeight());
//                ImageSpan span = new ImageSpan(myDrawable, ImageSpan.ALIGN_BASELINE);
//                sb.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//
//                return sb;

                return title;
            }
        };

        mPager.setAdapter(mPagerAdapter);

    }


}
