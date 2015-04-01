package com.rtweel.fragments;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.rtweel.R;
import com.rtweel.asynctasks.auth.TwitterAuthenticateTask;
import com.rtweel.twitteroauth.ConstantValues;
import com.rtweel.twitteroauth.TwitterGetAccessTokenTask;

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

        setTitle(getString(R.string.title_login));

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

    private boolean loginCheck() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        if (!sharedPreferences.getBoolean(
                ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, false)) {
            startLoading(getResources().getString(R.string.authorization));

            try {
                Uri uri = getActivity().getIntent().getData();
                if (uri != null
                        && uri.toString().startsWith(
                        ConstantValues.TWITTER_CALLBACK_URL)) {
                    String verifier = uri
                            .getQueryParameter(ConstantValues.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
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

        getMainActivity().setMainFragment(new TimelineFragment());
    }
}
