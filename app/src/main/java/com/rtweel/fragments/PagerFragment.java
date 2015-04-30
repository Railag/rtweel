package com.rtweel.fragments;

import com.rtweel.listeners.HideHeaderOnScrollListener;

/**
 * Created by root on 30.4.15.
 */
public abstract class PagerFragment extends BaseFragment {
    HideHeaderOnScrollListener mListener;

    public void setHideHeaderListener(HideHeaderOnScrollListener listener) {
        mListener = listener;
    }

    public abstract long getUserId();
}
