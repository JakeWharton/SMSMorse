package com.jakewharton.smsmorse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuItem;

public class SMSMorse extends PreferenceActivity {
	private static final int MENU_RESTORE_DEFAULTS = 0;
	private static final int MENU_ABOUT            = 1;
	
	private CheckBoxPreference      mCheckBoxEnabled;
	private ListPreference          mVibratePart;
	private CheckBoxPreference      mVibrateNumberCounts;
	private BetterSeekBarPreference mDotLength;
	private EditTextPreference      mTestText;
	private CheckBoxPreference      mCheckBoxInputEnabled;
	
	private OnPreferenceChangeListener mVibratePartListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) { 
			preference.setSummary(getString(R.string.preference_vibrate_parts_summary) + " " + getResources().getStringArray(R.array.preference_vibrate_parts_entries)[Integer.parseInt((String)newValue)]);
			return true;
		}
	};
	private OnPreferenceChangeListener mTestTextListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent intent = new Intent(SMSMorse.this, SMSMorseReceiver.class);
			intent.setAction(SMSMorseReceiver.VIBRATE_IN_MORSE);
			intent.putExtra(SMSMorseReceiver.VIBRATE_IN_MORSE_KEY, (String)newValue);
			sendBroadcast(intent);
			return true;
		}
	};
	private OnPreferenceChangeListener mEnabledListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			boolean state = (Boolean)arg1;
			
			mVibratePart.setEnabled(state);
			mTestText.setEnabled(state);
			mVibrateNumberCounts.setEnabled(state);
			mDotLength.setEnabled(state);
			mCheckBoxInputEnabled.setEnabled(state);
			
			if (!state || mCheckBoxEnabled.isChecked())
				mInputEnabledListener.onPreferenceChange(arg0, arg1);
			
			return true;
		}
	};
	private OnPreferenceChangeListener mInputEnabledListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			boolean state = (Boolean)arg1;
			
			//
			
			return true;
		}
	};	

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		loadPreferences();
	}
	private void loadPreferences() {
		addPreferencesFromResource(R.xml.preferences);
		final PreferenceScreen screen = getPreferenceScreen();
		
		mCheckBoxEnabled = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_enabled));
		mCheckBoxEnabled.setOnPreferenceChangeListener(mEnabledListener);
		
		mVibratePart = (ListPreference)screen.findPreference(getString(R.string.preference_vibrate_parts));
		mVibratePart.setOnPreferenceChangeListener(mVibratePartListener);
		//Trigger summary update
		mVibratePartListener.onPreferenceChange(mVibratePart, mVibratePart.getValue());
		
		mTestText = (EditTextPreference)screen.findPreference(getString(R.string.preference_test));
		mTestText.setOnPreferenceChangeListener(mTestTextListener);
		
		mVibrateNumberCounts = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_vibrate_counts));
		
		mDotLength = (BetterSeekBarPreference)screen.findPreference(getString(R.string.preference_dot_length));

		mCheckBoxInputEnabled = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_input_enabled));
		mCheckBoxInputEnabled.setOnPreferenceChangeListener(mInputEnabledListener);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.clear();
		menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.menu_restore_defaults);
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_RESTORE_DEFAULTS:
				restoreDefaultPreferences();
				return true;
			case MENU_ABOUT:
				showAbout();
				return true;
		}
		return false;
	}
	private void restoreDefaultPreferences() {
		PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
		setPreferenceScreen(null);
		loadPreferences();
	}
	private void showAbout() {
		Intent about = new Intent(this, About.class);
		startActivity(about);
	}
}