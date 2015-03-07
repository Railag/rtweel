package com.rtweel.settings;

import com.rtweel.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@SuppressLint("NewApi")
public class SettingFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}
}
