package com.rtweel.fragments;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rtweel.R;
import com.rtweel.tasks.auth.TwitterAuthenticateTask;
import com.rtweel.Const;
import com.rtweel.tasks.tweet.TwitterGetAccessTokenTask;

/**
 * Created by root on 21.3.15.
 */
public class LoginFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*
         * Login Check
		 */

        if (loginCheck()) {
            initialize();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getMainActivity().isLoggedIn()) {
            if (isLoading())
                stopLoading();
            if (loginCheck())
                initialize();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(getString(R.string.title_login));
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
                    Log.i("DEBUG", "Verification..");
                        new TwitterGetAccessTokenTask(getActivity())
                                .execute(verifier).get();

                        initialize();
                } else {
                    Log.i("DEBUG", "Browser authentification...");
                    new TwitterAuthenticateTask(getActivity()).execute();
                }
            } catch (Exception e) {
                Log.i("DEBUG", e.toString());
                e.printStackTrace();
            }
            return false;

        } else {
            return true;
        }
    }

    private void initialize() {
        if (isLoading())
            stopLoading();

        getMainActivity().setMainFragment(new HomeTweetFragment());
    }
}
