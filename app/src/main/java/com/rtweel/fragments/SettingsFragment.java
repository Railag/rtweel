package com.rtweel.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.rtweel.R;

/**
 * Created by root on 25.3.15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}

