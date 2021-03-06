package com.rtweel.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.rtweel.R;
import com.rtweel.MainActivity;

/**
 * Created by firrael on 21.3.15.
 */
public abstract class BaseFragment extends Fragment {
    private ProgressDialog mLoadingDialog;

    protected void startLoading(String loadingText) {
        mLoadingDialog = ProgressDialog.show(getActivity(), loadingText, getResources().getString(R.string.loading), true, false, null);
    }

    public void stopLoadingDialog() {
        mLoadingDialog.dismiss();
    }

    public boolean isLoadingDialogShown() {
        return mLoadingDialog != null && mLoadingDialog.isShowing();
    }

    public MainActivity getMainActivity() {
        return  (MainActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        String title = getTitle();
        if (!TextUtils.isEmpty(title))
            setTitle(getTitle());
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

    protected void setTitle(String title) {
        if (TextUtils.isEmpty(title))
            return;

        if (getMainActivity() != null)
            getMainActivity().getSupportActionBar().setTitle(title);
    }

    @Nullable
    protected abstract String getTitle();
}
