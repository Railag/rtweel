package com.rtweel.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.rtweel.R;

/**
 * Created by root on 25.3.15.
 */
public class SettingsFragment extends BaseFragment {

    public static final String IMAGES_SHOWN_PREFS = "images_shown";
    public static final String SAVE_TWEET_PREFS = "save_tweet";

    private CheckBox mImagesShown;
    private CheckBox mTweetSave;

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


        setCheckbox(mImagesShown, IMAGES_SHOWN_PREFS, getString(R.string.pref_images_shown));

        setCheckbox(mTweetSave, SAVE_TWEET_PREFS, getString(R.string.pref_save_tweet));

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
}

