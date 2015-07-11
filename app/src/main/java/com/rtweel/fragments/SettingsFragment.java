package com.rtweel.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.rtweel.R;
import com.rtweel.storage.App;
import com.rtweel.storage.TweetDatabase;
import com.rtweel.storage.Tweets;
import com.rtweel.utils.TwitterUtil;

/**
 * Created by firrael on 25.3.15.
 */
public class SettingsFragment extends BaseFragment {

    public static final String IMAGES_SHOWN_PREFS = "images_shown";
    public static final String SAVE_TWEET_PREFS = "save_tweet";

    private CheckBox mImagesShown;
    private CheckBox mTweetSave;
    private Button mLogoutButton;

    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        mImagesShown = (CheckBox) v.findViewById(R.id.images_shown);
        mTweetSave = (CheckBox) v.findViewById(R.id.save_tweet);
        mLogoutButton = (Button) v.findViewById(R.id.logout_button);


        setCheckbox(mImagesShown, IMAGES_SHOWN_PREFS, getString(R.string.pref_images_shown));

        setCheckbox(mTweetSave, SAVE_TWEET_PREFS, getString(R.string.pref_save_tweet));

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.logout_message));
                builder.setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });

        return v;
    }

    private void setCheckbox(CheckBox checkbox, final String prefsKey, String text) {
        checkbox.setText(text);
        checkbox.setChecked(mPrefs.getBoolean(prefsKey, true));
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(prefsKey, isChecked);
                editor.commit();
            }
        });
    }

    private void logout() {
        App app = (App) getActivity().getApplication();

        boolean dbDeleted = getActivity().deleteDatabase(TweetDatabase
                .getDbName());
        Log.i("DEBUG", "DB DELETED = " + dbDeleted);

        app.createDb();

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        TwitterUtil.getInstance().reset();
        getMainActivity().finish();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_settings);
    }
}

