package com.example.drawpen;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class PenPrefActivity extends PreferenceActivity implements OnPreferenceClickListener{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
		Preference prefBack = findPreference("back_key");
		prefBack.setOnPreferenceClickListener(this);

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		finish();

		return true;
	}

}

