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
import android.widget.RadioGroup;

import com.rtweel.R;
import com.rtweel.storage.App;
import com.rtweel.storage.TweetDatabase;
import com.rtweel.utils.TwitterUtil;

/**
 * Created by firrael on 25.3.15.
 */
public class SettingsFragment extends BaseFragment {

    public static final String IMAGES_SHOWN_PREFS = "images_shown";
    public static final String SAVE_TWEET_PREFS = "save_tweet";
    public static final String PN_ENABLED = "pn_enabled";
    public static final String PN_INTERVAL = "pn_interval";

    private CheckBox mImagesShown;
    private CheckBox mTweetSave;

    private CheckBox mPnEnabled;

    private RadioGroup mPnInterval;

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

        mPnEnabled = (CheckBox) v.findViewById(R.id.pn_enable);

        mPnInterval = (RadioGroup) v.findViewById(R.id.settings_radiogroup);

        mLogoutButton = (Button) v.findViewById(R.id.logout_button);


        setCheckbox(mImagesShown, IMAGES_SHOWN_PREFS, getString(R.string.pref_images_shown));

        setCheckbox(mTweetSave, SAVE_TWEET_PREFS, getString(R.string.pref_save_tweet));

        mPnEnabled.setText(getString(R.string.pref_pn_enabled));
        mPnEnabled.setChecked(mPrefs.getBoolean(PN_ENABLED, true));
        mPnEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(PN_ENABLED, isChecked);
                editor.commit();

                changeRadioGroup(isChecked);

                if (isChecked)
                    getMainActivity().startPNs();
                else
                    getMainActivity().stopPNs();
            }
        });

        if (!mPnEnabled.isChecked())
            changeRadioGroup(false);

        setRadioGroup();

        mPnInterval.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int minutes = 4 * 60;
                switch (checkedId) {
                    case R.id.settings_radiobutton_15m:
                        minutes = 15;
                        break;
                    case R.id.settings_radiobutton_30m:
                        minutes = 30;
                        break;
                    case R.id.settings_radiobutton_1h:
                        minutes = 60;
                        break;
                    case R.id.settings_radiobutton_2h:
                        minutes = 60 * 2;
                        break;
                    case R.id.settings_radiobutton_4h:
                        minutes = 60 * 4;
                        break;
                }

                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt(PN_INTERVAL, minutes);
                editor.commit();

                getMainActivity().startPNs();
            }
        });


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

    private void setRadioGroup() {
        int interval = mPrefs.getInt(PN_INTERVAL, 60 * 4);
        int id;
        switch (interval) {
            case 15:
                id = R.id.settings_radiobutton_15m;
                break;
            case 30:
                id = R.id.settings_radiobutton_30m;
                break;
            case 60:
                id = R.id.settings_radiobutton_1h;
                break;
            case 60 * 2:
                id = R.id.settings_radiobutton_2h;
                break;
            case 60 * 4:
                id = R.id.settings_radiobutton_4h;
                break;

            default:
                id = -1;
        }

        mPnInterval.check(id);
    }

    private void changeRadioGroup(boolean enabled) {
        for (int i = 0; i < mPnInterval.getChildCount(); i++)
             mPnInterval.getChildAt(i).setEnabled(enabled);
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

