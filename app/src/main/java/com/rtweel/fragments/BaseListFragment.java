package com.rtweel.fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.storage.AppUser;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by root on 5.5.15.
 */
public abstract class BaseListFragment extends BaseFragment {

    private static final String TIMELINE_POSITION = "timelinePosition";

    protected enum Scroll {SCROLL_DOWN, SCROLL_UP, UPDATE_DOWN, UPDATE_UP}

    protected static final long ANIM_TIME = 200; //TODO fix with eternal loading when 400ms

    protected RecyclerView.Adapter adapter;

    protected RecyclerView list;

    private int mLastFirstVisibleItem = 0;

    private LinearLayoutManager mLayoutManager;

    protected Long mUserId = -1L;

    private boolean isAnimLocked;

    private Handler mHandler;
    private Runnable mAnimLockRunnable;
    private Runnable mRetryAnim;
    private Runnable mRestoreTimelineState;

    private SmoothProgressBar mProgressBar;

    private Bundle state = new Bundle();

    protected abstract RecyclerView.Adapter createAdapter();

    protected abstract void updateUp(Scroll scroll);

    protected abstract void updateDown(Scroll scroll);

    protected abstract void instantiateListData(String username, String userScreenName, long userId);

    protected abstract void listDataLoading();

    protected abstract void loadingAnim();

    protected abstract long getUserId();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timeline, null);

        if (isLoadingDialogShown())
            stopLoadingDialog();

        Bundle args = getArguments();
        if (args != null) {
            String username = args.getString(Const.USERNAME);
            String userScreenName = args.getString(Const.SCREEN_USERNAME);
            long userId = args.getLong(Const.USER_ID);
            instantiateListData(username, userScreenName, userId);
        } else {
            String userName = AppUser.getUserName(getActivity());
            String screenUserName = AppUser.getScreenUserName(getActivity());
            long userId = AppUser.getUserId(getActivity());
            instantiateListData(userName, screenUserName, userId);
        }

        mProgressBar = (SmoothProgressBar) v.findViewById(R.id.smooth_progress_bar);

        initHandler();

        initList(v);

        listDataLoading();

        initFloatingButton(v);

        return v;
    }

    private void initList(View v) {

        list = (RecyclerView) v.findViewById(R.id.list);


        list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int firstVisibleItem = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                int historySize = event.getHistorySize();
                int action = event.getAction();


                //only swipes
                if (action == MotionEvent.ACTION_MOVE && historySize > 0) {
                    float historicalY = event.getHistoricalY(historySize - 1);
                    float y = event.getY();

                    //showing profile header swipe
                    if (firstVisibleItem <= mLastFirstVisibleItem && firstVisibleItem <= 0 && detectSwipeUp(y, historicalY)) {
                        updateUp(Scroll.SCROLL_UP);
                    }


                    //hiding profile header swipe
                    if (firstVisibleItem > 0 && detectSwipeDown(y, historicalY)) {
                        updateDown(Scroll.SCROLL_DOWN);
                    }

                    //basic swipe up updating at top
                    if (firstVisibleItem < 1 && mLastFirstVisibleItem == 0) {
                        if (detectSwipeUp(y, historicalY)) {
                            updateUp(Scroll.UPDATE_UP);
                            mLastFirstVisibleItem = firstVisibleItem;
                            return true;
                        }
                    }

                    //basic swipe down updating
                    if (firstVisibleItem > mLastFirstVisibleItem && (adapter.getItemCount() - firstVisibleItem) < 4) {
                        updateDown(Scroll.UPDATE_DOWN);
                        mLastFirstVisibleItem = firstVisibleItem;
                        return true;
                    }

                    //pre-basic swipe down updating
                    int count = adapter.getItemCount();
                    int last = mLayoutManager.findLastVisibleItemPosition();

                    if (count - last < 3 && detectSwipeDown(y, historicalY)) {
                        updateDown(Scroll.UPDATE_DOWN);
                        return true;
                    }
                }

                mLastFirstVisibleItem = firstVisibleItem;
                return false;
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());

        startLoadingAnim();

        adapter = createAdapter();

        list.setAdapter(adapter);
    }


    private void initFloatingButton(View v) {
        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setType(1);
        fab.setColorNormal(R.color.green);
        fab.setColorPressed(R.color.blue);
        fab.setColorRipple(R.color.red);
        fab.attachToRecyclerView(list);
        fab.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        fab.setImageDrawable(getResources().getDrawable(R.drawable.up_arrow));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.scrollToPosition(0);
                updateUp(Scroll.UPDATE_UP);
                fab.hide(true);
            }
        });
    }

    public void startLoadingAnim() {
        if (isAnimLocked)
            mHandler.postDelayed(mRetryAnim, ANIM_TIME / 2);
        else {
            isAnimLocked = true;
            loadingAnim();
            mHandler.postDelayed(mAnimLockRunnable, ANIM_TIME);
        }
    }


    private void initHandler() {
        mHandler = new Handler();
        mAnimLockRunnable = new Runnable() {
            @Override
            public void run() {
                isAnimLocked = false;
            }
        };
        mRetryAnim = new Runnable() {
            @Override
            public void run() {
                startLoadingAnim();
            }
        };
        mRestoreTimelineState = new Runnable() {
            @Override
            public void run() {
                mLayoutManager.scrollToPosition(state.getInt(TIMELINE_POSITION));
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();
        initHandler();
        if (mLayoutManager != null)
            mHandler.postDelayed(mRestoreTimelineState, ANIM_TIME);
    }


    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mAnimLockRunnable);
        mHandler.removeCallbacks(mRetryAnim);
        mHandler.removeCallbacks(mRestoreTimelineState);
        if (mLayoutManager != null)
            state.putInt(TIMELINE_POSITION, mLayoutManager.findFirstCompletelyVisibleItemPosition() + 1);
    }

    public void blink() {

        ValueAnimator fade = new ValueAnimator();
        fade.setFloatValues(1, 0.6f, 1);
        fade.setDuration(1400);
        fade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                list.setAlpha(value);
            }
        });

        fade.start();
    }


    protected boolean detectSwipeDown(float y, float historicalY) {
        return y < historicalY;
    }

    protected boolean detectSwipeUp(float y, float historicalY) {
        return y > historicalY;
    }

    public RecyclerView getList() {
        return list;
    }

    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    protected boolean isProgressBarShown() {
        return mProgressBar.isShown();
    }
}
