package com.rtweel.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.rtweel.R;
import com.rtweel.activities.MainActivity;

/**
 * Created by root on 25.3.15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.settings));

        addPreferencesFromResource(R.xml.preferences);
    }
}

