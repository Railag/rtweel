package com.rtweel.profile;

import com.rtweel.fragments.BaseListFragment;
import com.rtweel.listeners.HideHeaderOnScrollListener;

/**
 * Created by root on 30.4.15.
 */
public abstract class PagerFragment extends BaseListFragment {
    HideHeaderOnScrollListener mListener;

    public void setHideHeaderListener(HideHeaderOnScrollListener listener) {
        mListener = listener;
    }

    @Override
    protected void updateDown(Scroll scroll) {
        if (!scroll.equals(Scroll.SCROLL_DOWN))
            return;

        if (mListener != null && !mListener.isHidden()) {
            mListener.onScrollDown();
        }
    }

    @Override
    protected void updateUp(Scroll scroll) {
        if (!scroll.equals(Scroll.SCROLL_UP))
            return;

        if (mListener != null && mListener.isHidden()) {
            mListener.onTop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isProgressBarShown())
            hideProgressBar();
    }
}
