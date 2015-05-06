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
import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.listeners.HideHeaderOnScrollListener;
import com.rtweel.storage.Tweets;
import com.rtweel.tasks.tweet.GetUserDetailsTask;

/**
 * Created by root on 25.3.15.
 */
public class ProfileFragment extends BaseFragment {

    private final static int PAGER_SIZE = 5;
    private final static int ANIMATION_TIME = 2000;

    private View mView;

    private ViewPager mPager;

    private FragmentCollection mCollection;

    private PagerAdapter mPagerAdapter;

    private TextView mProfileNameNormal;
    private TextView mProfileNameLink;
    private Long mProfileId = 0L;

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

        Bundle args = getArguments();
        if (args != null) {
            mProfileNameNormal.setText(args.getString(Const.USERNAME));
            mProfileNameLink.setText(args.getString(Const.SCREEN_USERNAME));
            mProfileId = args.getLong(Const.USER_ID);
        }

        GetUserDetailsTask task = new GetUserDetailsTask(getActivity(), mBackground, mLogo, mProfileNameNormal, mProfileNameLink, mDescription, mProfileId);
        task.execute(Tweets.getTwitter(getActivity()));


        initPagerAdapter();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitle(getString(R.string.title_profile));
        mView = inflater.inflate(R.layout.fragment_profile, container, false);

        mPager = (ViewPager) mView.findViewById(R.id.pager);

//        mPager.setPageTransformer(false, new DepthPageTransformer());

        mBackground = (ImageView) mView.findViewById(R.id.profile_background);
        mLogo = (RoundedImageView) mView.findViewById(R.id.profile_picture);

        mProfileNameNormal = (TextView) mView.findViewById(R.id.profile_name_normal);
        mProfileNameLink = (TextView) mView.findViewById(R.id.profile_name_link);

        mDescription = (TextView) mView.findViewById(R.id.profile_description);

        mHeaderLayout = mView.findViewById(R.id.header_layout);

        mCollection = new FragmentCollection();

        return mView;
    }


    private void initPagerAdapter() {
        mPagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return mCollection.getUser();
                    case 1:
                        return mCollection.getAnswers();
                    case 2:
                        return mCollection.getFav();
                    case 3:
                        return mCollection.getImages();
                    case 4:
                        return mCollection.getFollowers();
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
                        title = getString(R.string.timeline_user);
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
                    case 4:
                        title = getString(R.string.followers);
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

    private Fragment instantiateFragment(PagerFragment pagerFragment) {

        Bundle args = new Bundle();
        args.putString(Const.USERNAME, mProfileNameNormal.getText().toString());
        args.putString(Const.SCREEN_USERNAME, mProfileNameLink.getText().toString());
        args.putLong(Const.USER_ID, mProfileId);
        pagerFragment.setArguments(args);

        mListener = new HideHeaderOnScrollListener() {
            @Override
            public void onScrollDown() {
                if (!mIsBlocked)
                    hideHeader();
            }

            @Override
            public void onTop() {
                if (!mIsBlocked)
                    showHeader();
            }

            @Override
            public boolean isHidden() {
                return mIsHidden;
            }
        };

        pagerFragment.setHideHeaderListener(mListener);

        return pagerFragment;
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
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
//        mIsHidden = false;
//        mIsBlocked = false;
    }

//    private class DepthPageTransformer implements ViewPager.PageTransformer {
//        private static final float MIN_SCALE = 0.75f;
//
//        public void transformPage(View view, float position) {
//            int pageWidth = view.getWidth();
//
//            if (position < -1) { // [-Infinity,-1)
//                // This page is way off-screen to the left.
//                view.setAlpha(0);
//
//            } else if (position <= 0) { // [-1,0]
//                // Use the default slide transition when moving to the left page
//                view.setAlpha(1);
//                view.setTranslationX(0);
//                view.setScaleX(1);
//                view.setScaleY(1);
//
//            } else if (position <= 1) { // (0,1]
//                // Fade the page out.
//                view.setAlpha(1 - position);
//
//                // Counteract the default slide transition
//                view.setTranslationX(pageWidth * -position);
//
//                // Scale the page down (between MIN_SCALE and 1)
//                float scaleFactor = MIN_SCALE
//                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
//                view.setScaleX(scaleFactor);
//                view.setScaleY(scaleFactor);
//
//            } else { // (1,+Infinity]
//                // This page is way off-screen to the right.
//                view.setAlpha(0);
//            }
//        }
//    }

    private class FragmentCollection {
        UserTweetFragment user;
        AnswersTweetFragment answers;
        FavoriteTweetFragment fav;
        ImagesTweetFragment images;
        FollowersFragment followers;


        public void saveFragment(PagerFragment fragment) {
            if (fragment instanceof UserTweetFragment) {
                if (user == null)
                    user = (UserTweetFragment) fragment;
                else {
                    Bundle args = fragment.getArguments();
                    if (args.getLong(Const.USER_ID) != user.getTimeline().getUserId())
                        user = (UserTweetFragment) fragment;
                }
            } else if (fragment instanceof AnswersTweetFragment) {
                if (answers == null)
                    answers = (AnswersTweetFragment) fragment;
                else {
                    Bundle args = fragment.getArguments();
                    if (args.getLong(Const.USER_ID) != answers.getTimeline().getUserId())
                        answers = (AnswersTweetFragment) fragment;
                }
            } else if (fragment instanceof FavoriteTweetFragment) {
                if (fav == null)
                    fav = (FavoriteTweetFragment) fragment;
                else {
                    Bundle args = fragment.getArguments();
                    if (args.getLong(Const.USER_ID) != fav.getTimeline().getUserId())
                        fav = (FavoriteTweetFragment) fragment;
                }
            } else if (fragment instanceof ImagesTweetFragment) {
                if (images == null)
                    images = (ImagesTweetFragment) fragment;
                else {
                    Bundle args = fragment.getArguments();
                    if (args.getLong(Const.USER_ID) != images.getTimeline().getUserId())
                        images = (ImagesTweetFragment) fragment;
                }
            } else if (fragment instanceof FollowersFragment) {
                if (followers == null)
                    followers = (FollowersFragment) fragment;
                else {
                    Bundle args = fragment.getArguments();
                    if (args.getLong(Const.USER_ID) != followers.getUserId())
                        followers = (FollowersFragment) fragment;
                }
            }
        }

        public Fragment getUser() {
            if (user != null)
                return user;
            else {
                Fragment fragment = instantiateFragment(new UserTweetFragment());
                saveFragment((PagerFragment) fragment);
                return fragment;
            }
        }

        public Fragment getAnswers() {
            if (answers != null)
                return answers;
            else {
                Fragment fragment = instantiateFragment(new AnswersTweetFragment());
                saveFragment((PagerFragment) fragment);
                return fragment;
            }
        }

        public Fragment getFav() {
            if (fav != null)
                return fav;
            else {
                Fragment fragment = instantiateFragment(new FavoriteTweetFragment());
                saveFragment((PagerFragment) fragment);
                return fragment;
            }
        }

        public Fragment getImages() {
            if (images != null)
                return images;
            else {
                Fragment fragment = instantiateFragment(new ImagesTweetFragment());
                saveFragment((PagerFragment) fragment);
                return fragment;
            }
        }

        public Fragment getFollowers() {
            if (followers != null)
                return followers;
            else {
                Fragment fragment = instantiateFragment(new FollowersFragment());
                saveFragment((PagerFragment) fragment);
                return fragment;
            }
        }
    }

}
