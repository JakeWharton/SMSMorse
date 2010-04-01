package com.jakewharton.smsmorse.ui;

import com.jakewharton.smsmorse.R;
import com.jakewharton.smsmorse.transaction.EventReceiver;

import android.content.Intent;
import android.content.pm.PackageManager;
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

public class Preferences extends PreferenceActivity {
	private static final int MENU_RESTORE_DEFAULTS = 0;
	
	private CheckBoxPreference      mCheckBoxEnabled;
	private ListPreference          mVibratePart;
	private CheckBoxPreference      mVibrateCounts;
	private BetterSeekBarPreference mDotLength;
	private EditTextPreference      mTestText;
	private CheckBoxPreference      mScreenOffOnly;
	private CheckBoxPreference      mActiveNormal;
	private CheckBoxPreference      mActiveVibrate;
	private CheckBoxPreference      mActiveSilent;
	//private CheckBoxPreference      mCheckBoxInputEnabled;
	
	private OnPreferenceChangeListener mVibratePartListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) { 
			preference.setSummary(getString(R.string.preference_vibrate_parts_summary) + " " + getResources().getStringArray(R.array.preference_vibrate_parts_entries)[Integer.parseInt((String)newValue)]);
			return true;
		}
	};
	private OnPreferenceChangeListener mTestTextListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Intent intent = new Intent(Preferences.this, EventReceiver.class);
			intent.setAction(EventReceiver.VIBRATE_IN_MORSE);
			intent.putExtra(EventReceiver.VIBRATE_IN_MORSE_KEY, (String)newValue);
			sendBroadcast(intent);
			return true;
		}
	};
	private OnPreferenceChangeListener mEnabledListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			boolean state = (Boolean)arg1;
			
			mVibratePart.setEnabled(state);
			mVibrateCounts.setEnabled(state);
			mDotLength.setEnabled(state);
			mTestText.setEnabled(state);
			mScreenOffOnly.setEnabled(state);
			mActiveNormal.setEnabled(state);
			mActiveVibrate.setEnabled(state);
			mActiveSilent.setEnabled(state);
			//mCheckBoxInputEnabled.setEnabled(state);
			
			//if (!state || mCheckBoxEnabled.isChecked())
			//	mInputEnabledListener.onPreferenceChange(arg0, arg1);
			
			return true;
		}
	};
	/*private OnPreferenceChangeListener mInputEnabledListener = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			boolean state = (Boolean)arg1;
			
			//TODO: add input preferences here
			
			return true;
		}
	};*/

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		loadPreferences();
	}
	private void loadPreferences() {
		addPreferencesFromResource(R.xml.preferences);
		final PreferenceScreen screen = getPreferenceScreen();
		
		mCheckBoxEnabled = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_enabled));
		mVibratePart     = (ListPreference)screen.findPreference(getString(R.string.preference_vibrate_parts));
		mTestText        = (EditTextPreference)screen.findPreference(getString(R.string.preference_test));
		mVibrateCounts   = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_vibrate_counts));
		mDotLength       = (BetterSeekBarPreference)screen.findPreference(getString(R.string.preference_dot_length));
		
		mCheckBoxEnabled.setOnPreferenceChangeListener(mEnabledListener);
		mVibratePart.setOnPreferenceChangeListener(mVibratePartListener);
		//Trigger summary update
		mVibratePartListener.onPreferenceChange(mVibratePart, mVibratePart.getValue());
		mTestText.setOnPreferenceChangeListener(mTestTextListener);
		
		mScreenOffOnly = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_screen_off_only));
		mActiveNormal  = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_vibrate_normal));
		mActiveVibrate = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_vibrate_vibrate));
		mActiveSilent  = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_vibrate_silent));
		
		//mCheckBoxInputEnabled = (CheckBoxPreference)screen.findPreference(getString(R.string.preference_input_enabled));
		//mCheckBoxInputEnabled.setOnPreferenceChangeListener(mInputEnabledListener);
		
		final DialogPreference about = (DialogPreference)screen.findPreference(getString(R.string.preference_about));
		about.setDialogLayoutResource(R.layout.about);
		try {
			final String version = getPackageManager().getPackageInfo("com.jakewharton.smsmorse", PackageManager.GET_META_DATA).versionName;
			about.setDialogTitle(getString(R.string.app_name) + " v" + version);
		} catch (Exception e) {}
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
				PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
				setPreferenceScreen(null);
				loadPreferences();
				return true;
		}
		return false;
	}
}