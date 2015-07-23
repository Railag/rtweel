package com.rtweel.fragments;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.tasks.auth.TwitterAuthenticateTask;
import com.rtweel.tasks.tweet.TwitterGetAccessTokenTask;

/**
 * Created by firrael on 21.3.15.
 */
public class LoginFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*
         * Login Check
		 */

        setRetainInstance(true);

        if (loginCheck()) {
            initialize();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getMainActivity().isLoggedIn()) {
            if (isLoadingDialogShown())
                stopLoadingDialog();
            if (loginCheck())
                initialize();
        }
    }

    private boolean loginCheck() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        if (!sharedPreferences.getBoolean(
                Const.PREFERENCE_TWITTER_IS_LOGGED_IN, false)) {
            startLoading(getResources().getString(R.string.authorization));

            try {
                Uri uri = getActivity().getIntent().getData();
                if (uri != null
                        && uri.toString().startsWith(
                        Const.TWITTER_CALLBACK_URL)) {
                    String verifier = uri
                            .getQueryParameter(Const.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
                    new TwitterGetAccessTokenTask(getActivity())
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, verifier).get();

                    initialize();
                } else {
                    new TwitterAuthenticateTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;

        } else {
            return true;
        }
    }

    private void initialize() {
        if (isLoadingDialogShown())
            stopLoadingDialog();

        getMainActivity().setMainFragment(new HomeTweetFragment());
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_login);
    }
}
