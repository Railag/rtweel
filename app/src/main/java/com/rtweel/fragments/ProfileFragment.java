package com.rtweel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rtweel.R;

/**
 * Created by root on 25.3.15.
 */
public class ProfileFragment extends BaseFragment {
    //TODO


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitle(getString(R.string.title_profile));
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
