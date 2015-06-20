package com.rtweel.fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.listeners.HideHeaderOnScrollListener;
import com.rtweel.storage.AppUser;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by firrael on 5.5.15.
 */
public abstract class ProfileFragment extends BaseFragment {

    HideHeaderOnScrollListener mListener;

    private static final String TIMELINE_POSITION = "timelinePosition";

    protected enum Scroll {SCROLL_DOWN, SCROLL_UP, UPDATE_DOWN, UPDATE_UP}

    protected static final long ANIM_TIME = 200; //TODO fix with eternal loading when 400ms

    protected RecyclerView.Adapter adapter;

    protected RecyclerView list;

    private int mLastFirstVisibleItem = 0;

    private LinearLayoutManager mLayoutManager;

    protected Long mUserId = -1L;

    private static Handler mHandler;

    private final static int MESSAGE_TIMELINE_STATE = 1;
    private final static int MESSAGE_STOP_ANIM = 2;
    private final static int MESSAGE_ANIM_LOADING = 3;

    private SmoothProgressBar mProgressBar;

    private Bundle state = new Bundle();

    protected abstract RecyclerView.Adapter createAdapter();

    protected abstract void instantiateListData(String username, String userScreenName, long userId);

    protected abstract void listDataLoading();

    protected abstract void startAnim();

    protected abstract void stopAnim();

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

    public void setHideHeaderListener(HideHeaderOnScrollListener listener) {
        mListener = listener;
    }

    protected void updateDown(Scroll scroll) {
        if (!scroll.equals(Scroll.SCROLL_DOWN))
            return;

        if (mListener != null && !mListener.isHidden()) {
            mListener.onScrollDown();
        }
    }

    protected void updateUp(Scroll scroll) {
        if (!scroll.equals(Scroll.SCROLL_UP))
            return;

        if (mListener != null && mListener.isHidden()) {
            mListener.onTop();
        }
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
        mHandler.sendEmptyMessage(MESSAGE_ANIM_LOADING);
    }

    public void stopLoadingAnim() {
        mHandler.sendEmptyMessage(MESSAGE_STOP_ANIM);
    }


    private void initHandler() {
        mHandler = new RtHandler();
    }


    @Override
    public void onResume() {
        super.onResume();
        initHandler();
        if (mLayoutManager != null) {
            Message message = mHandler.obtainMessage(MESSAGE_TIMELINE_STATE);
            message.arg1 = state.getInt(TIMELINE_POSITION);
            mHandler.sendMessageDelayed(message, ANIM_TIME);
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeMessages(MESSAGE_TIMELINE_STATE);
        mHandler.removeMessages(MESSAGE_STOP_ANIM);
        mHandler.removeMessages(MESSAGE_ANIM_LOADING);

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

    private class RtHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_TIMELINE_STATE:
                    int position = msg.arg1;
                    Log.i("Handler", "MESSAGE_TIMELINE_STATE" + " Position = " + position);


                    if (position > mLayoutManager.getItemCount()) {
                        updateDown(Scroll.UPDATE_DOWN);
                        Message message = mHandler.obtainMessage(MESSAGE_TIMELINE_STATE);
                        message.arg1 = position;
                        sendMessageDelayed(message, ANIM_TIME);
                        return;
                    }


                    mLayoutManager.scrollToPosition(position);
                    break;
                case MESSAGE_STOP_ANIM:
                    stopAnim();
                    break;
                case MESSAGE_ANIM_LOADING:
                    startAnim();
                    break;
            }
        }
    }
}
