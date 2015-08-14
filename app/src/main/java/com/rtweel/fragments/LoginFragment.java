package com.rtweel.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.tasks.auth.TwitterAuthenticateTask;
import com.rtweel.tasks.tweet.TwitterGetAccessTokenTask;
import com.rtweel.utils.TwitterUtil;

import java.util.concurrent.ExecutionException;

import twitter4j.auth.RequestToken;

/**
 * Created by firrael on 21.3.15.
 */
public class LoginFragment extends BaseFragment {

    private boolean isCalled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

        boolean isLogged = sharedPreferences.getBoolean(Const.PREFERENCE_TWITTER_IS_LOGGED_IN, false);

        if (isLogged)
            return true;
        else {
            startLoading(getString(R.string.authorization));

            Uri uri = getActivity().getIntent().getData();

            if (uri == null && isCalled) {
                stopLoadingDialog();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(false);

                AlertDialog dialog = builder.create();
                dialog.setTitle(getString(R.string.authorization));
                dialog.setMessage(getString(R.string.login_dialog_message));
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.login_dialog_try_again), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tryWebView();
                    }
                });

                dialog.show();

                return false;
            }

            if (uri != null && uri.toString().startsWith(Const.TWITTER_CALLBACK_URL))
                processCallback(uri);
            else {
                isCalled = true;
                new TwitterAuthenticateTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }

            return false;
        }
    }

    private void processCallback(Uri uri) {
        String verifier = uri
                .getQueryParameter(Const.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
        new TwitterGetAccessTokenTask(getMainActivity())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, verifier);

        if (isLoadingDialogShown())
            stopLoadingDialog();
    }

    private void tryWebView() {
        RequestToken token = TwitterUtil.getInstance().getRequestToken();
        if (token != null)
            getMainActivity().loadUrl(token.getAuthenticationURL());
    }

    private void initialize() {
        if (isLoadingDialogShown())
            stopLoadingDialog();

        getMainActivity().setMainFragment(new HomeTweetFragment());
        getMainActivity().getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getMainActivity().getToggle().setDrawerIndicatorEnabled(true);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_login);
    }
}
