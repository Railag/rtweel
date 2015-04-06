package com.rtweel.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rtweel.R;
import com.rtweel.asynctasks.tweet.GetUserDetailsTask;
import com.rtweel.listeners.HideHeaderOnScrollListener;
import com.rtweel.tweet.Timeline;

/**
 * Created by root on 25.3.15.
 */
public class ProfileFragment extends BaseFragment {
    //TODO

    private final static int PAGER_SIZE = 3;

    private View mView;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private TextView mProfileNameNormal;
    private TextView mProfileNameLink;

    private ImageView mBackground;
    private RoundedImageView mLogo;

    private TextView mDescription;

    private HideHeaderOnScrollListener mListener;

    private View mHeaderLayout;

    private boolean mIsHidden;

    private boolean mIsBlocked;

    private Handler mHandler;
    private Runnable mRunnable;

    private float headerY;
    private float descriptionY;
    private float pagerY;
    private int pagerHeight;
    private int fullHeight;


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
        mView = inflater.inflate(R.layout.fragment_profile, container, false);

        mPager = (ViewPager) mView.findViewById(R.id.pager);

        mBackground = (ImageView) mView.findViewById(R.id.profile_background);
        mLogo = (RoundedImageView) mView.findViewById(R.id.profile_picture);

        mProfileNameNormal = (TextView) mView.findViewById(R.id.profile_name_normal);
        mProfileNameLink = (TextView) mView.findViewById(R.id.profile_name_link);

        mDescription = (TextView) mView.findViewById(R.id.profile_description);

        mHeaderLayout = mView.findViewById(R.id.header_layout);

        return mView;
    }


    private void initPagerAdapter() {
        mPagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return instantiateFragment(new HomeTimelineFragment());
                    case 1:
                        return instantiateFragment(new UserTimelineFragment());
                    case 2:
                        return instantiateFragment(new HomeTimelineFragment());
                    default:
                        return instantiateFragment(new HomeTimelineFragment());
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

    private Fragment instantiateFragment(TimelineFragment timelineFragment) {

        mListener = new HideHeaderOnScrollListener() {
            @Override
            public void onScrollDown() {
                if(!mIsBlocked)
                    hideHeader();
            }

            @Override
            public void onTop() {
                if(!mIsBlocked)
                    showHeader();
            }

            @Override
            public boolean isHidden() {
                return mIsHidden;
            }
        };

        timelineFragment.setHideHeaderListener(mListener);

        return timelineFragment;
    }



    private void blockHiding() {
        mIsBlocked = true;
        mHandler.postDelayed(mRunnable, 2000);
    }

    private void hideHeader() {

        headerY = mHeaderLayout.getY();
        descriptionY = mDescription.getY();
        pagerY = mPager.getY();
        pagerHeight = mPager.getHeight();
        fullHeight = mPager.getHeight() + mHeaderLayout.getHeight() + mDescription.getHeight();

        mIsHidden = true;

        blockHiding();

        ValueAnimator hideHeader = new ValueAnimator();
        hideHeader.setFloatValues(headerY, -mHeaderLayout.getHeight());
        hideHeader.setDuration(2000);
        hideHeader.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mHeaderLayout.setY(value);
            }
        });
        hideHeader.start();

        hideHeader.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHeaderLayout.setVisibility(View.GONE);
                mDescription.setVisibility(View.GONE);
            }
        });

        ValueAnimator hideDesc = new ValueAnimator();
        hideDesc.setFloatValues(descriptionY, -mDescription.getHeight());
        hideDesc.setDuration(2000);
        hideDesc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mDescription.setY(value);
            }
        });
        hideDesc.start();

        ValueAnimator liftPager = new ValueAnimator();
        liftPager.setFloatValues(mPager.getY(), 0f);
        liftPager.setDuration(2000);
        liftPager.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPager.setY(value);
            }
        });
        liftPager.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mPager.setY(pagerY);
            }
        });
        liftPager.start();

        ValueAnimator increasePager = new ValueAnimator();
        increasePager.setIntValues(pagerHeight, fullHeight);
        increasePager.setDuration(2000);
        increasePager.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mPager.getLayoutParams().width, value);
                mPager.setLayoutParams(params);
            }
        });
        increasePager.start();
    }

    private void showHeader() {
        mHeaderLayout.setVisibility(View.VISIBLE);
        mDescription.setVisibility(View.VISIBLE);

        mIsHidden = false;

        blockHiding();

        ValueAnimator showHeader = new ValueAnimator();
        showHeader.setFloatValues(-mHeaderLayout.getHeight(), headerY);
        showHeader.setDuration(2000);
        showHeader.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mHeaderLayout.setY(value);
            }
        });
        showHeader.start();

        ValueAnimator showDesc = new ValueAnimator();
        showDesc.setFloatValues(-mDescription.getHeight(), descriptionY);
        showDesc.setDuration(2000);
        showDesc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mDescription.setY(value);
            }
        });
        showDesc.start();

        ValueAnimator downPager = new ValueAnimator();
        downPager.setFloatValues(0f, pagerY);
        downPager.setDuration(2000);
        downPager.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPager.setY(value);
            }
        });
        downPager.start();

        ValueAnimator decreasePager = new ValueAnimator();
        decreasePager.setIntValues(fullHeight, pagerHeight);
        decreasePager.setDuration(2000);
        decreasePager.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = mPager.getLayoutParams();
                params.height = value;
                mPager.setLayoutParams(params);
            }
        });
        decreasePager.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mIsBlocked = false;
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }
}
