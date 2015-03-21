package com.rtweel.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.support.v7.app.ActionBar;
import android.widget.ProgressBar;

import com.rtweel.R;
import com.rtweel.activities.MainActivity;

/**
 * Created by root on 21.3.15.
 */
public class BaseFragment extends Fragment {
    private ProgressDialog mLoadingDialog;

    protected void startLoading(String loadingText) {
        mLoadingDialog = ProgressDialog.show(getActivity(), loadingText, getResources().getString(R.string.loading));
    }

    public void stopLoading() {
        mLoadingDialog.dismiss();
    }

    public boolean isLoading() {
        return mLoadingDialog != null && mLoadingDialog.isShowing();
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    protected ActionBar getActionBar() {
        return getMainActivity().getSupportActionBar();
    }

    protected ProgressBar getLoadingBar() {
        return getMainActivity().getLoadingBar();
    }

    protected void back() {
        getMainActivity().onBackPressed();
    }
}
