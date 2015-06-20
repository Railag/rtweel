package com.rtweel.profile;

import com.rtweel.fragments.ProfileFragment;
import com.rtweel.listeners.HideHeaderOnScrollListener;

/**
 * Created by firrael on 30.4.15.
 */
public abstract class PagerFragment extends ProfileFragment {
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
}
