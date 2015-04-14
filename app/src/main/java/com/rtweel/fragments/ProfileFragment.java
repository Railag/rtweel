package com.rtweel.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rtweel.R;
import com.rtweel.asynctasks.db.Tweets;
import com.rtweel.asynctasks.tweet.GetUserDetailsTask;
import com.rtweel.listeners.HideHeaderOnScrollListener;
import com.rtweel.timelines.Timeline;

/**
 * Created by root on 25.3.15.
 */
public class ProfileFragment extends BaseFragment {

    private final static int PAGER_SIZE = 4;
    private final static int ANIMATION_TIME = 2000;

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

        GetUserDetailsTask task = new GetUserDetailsTask(getActivity(), mBackground, mLogo, mProfileNameNormal, mProfileNameLink, mDescription);
        task.execute(Tweets.getTwitter(getActivity()));



        initPagerAdapter();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitle(getString(R.string.title_profile));
        mView = inflater.inflate(R.layout.fragment_profile, container, false);

        mPager = (ViewPager) mView.findViewById(R.id.pager);

        mPager.setPageTransformer(false, new DepthPageTransformer());

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
                        return instantiateFragment(new UserTimelineFragment());
                    case 1:
                        return instantiateFragment(new AnswersTimelineFragment());
                    case 2:
                        return instantiateFragment(new FavoriteTimelineFragment());
                    case 3:
                        return instantiateFragment(new ImagesTimelineFragment());
                    default:
                        return null;
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
                        title = getString(R.string.timeline_home);
                        break;
                    case 1:
                        title = getString(R.string.timeline_answers);
                        break;
                    case 2:
                        title = getString(R.string.timeline_favorite);
                        break;
                    case 3:
                        title = getString(R.string.timeline_images);
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
        mHandler.postDelayed(mRunnable, ANIMATION_TIME);
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
        hideHeader.setDuration(ANIMATION_TIME);
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
        hideDesc.setDuration(ANIMATION_TIME);
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
        liftPager.setDuration(ANIMATION_TIME);
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
        increasePager.setDuration(ANIMATION_TIME);
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
        showHeader.setDuration(ANIMATION_TIME);
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
        showDesc.setDuration(ANIMATION_TIME);
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
        downPager.setDuration(ANIMATION_TIME);
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
        decreasePager.setDuration(ANIMATION_TIME);
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

    private class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
