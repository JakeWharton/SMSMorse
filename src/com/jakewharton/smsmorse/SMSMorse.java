package com.jakewharton.smsmorse;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuItem;

public class SMSMorse extends PreferenceActivity {
	public static final boolean DEFAULT_AUTO_START = true;
	
	private static final int MENU_RESTORE_DEFAULTS = 0;
	
	private ListPreference mVibratePart;
	private EditTextPreference mTestText;
	
	private OnPreferenceChangeListener mVibratePartListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) { 
			preference.setSummary(getString(R.string.preference_vibrate_parts_summary) + " " + getResources().getStringArray(R.array.preference_vibrate_parts_entries)[Integer.parseInt((String)newValue)]);
			return true;
		}
	};
	private OnPreferenceChangeListener mTestTextListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent intent = new Intent(SMSMorse.this, SMSMorseReceiver.class);
			intent.setAction(SMSMorseReceiver.VIBRATE_MORSE);
			intent.putExtra(SMSMorseReceiver.VIBRATE_MORSE_KEY, (String)newValue);
			sendBroadcast(intent);
			return true;
		}
	};
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preferences);
		setupExtra();
	}

	private void setupExtra() {
		mVibratePart = (ListPreference)getPreferenceScreen().findPreference(getString(R.string.preference_vibrate_parts));
		mVibratePart.setOnPreferenceChangeListener(mVibratePartListener);
		//Trigger summary update
		mVibratePartListener.onPreferenceChange(mVibratePart, mVibratePart.getValue());
		
		mTestText = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.preference_test));
		mTestText.setOnPreferenceChangeListener(mTestTextListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.clear();
		menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.menu_restore_defaults);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_RESTORE_DEFAULTS:
				restoreDefaultPreferences();
				return true;
		}
		return false;
	}
	
	private void restoreDefaultPreferences() {
		PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
		setPreferenceScreen(null);
		addPreferencesFromResource(R.xml.preferences);
		setupExtra();
	}
}