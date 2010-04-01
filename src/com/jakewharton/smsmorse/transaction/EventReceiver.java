package com.jakewharton.smsmorse.transaction;

import java.util.ArrayList;
import com.jakewharton.smsmorse.R;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

public class EventReceiver extends BroadcastReceiver {
	private static final String TAG = "EventReceiver";
	
	//Intents
	private static final String SMS_RECEIVED         = "android.provider.Telephony.SMS_RECEIVED";
	public  static final String VIBRATE_IN_MORSE     = "com.jakewharton.smsmorse.VIBRATE_MORSE";
	public  static final String VIBRATE_IN_MORSE_KEY = "message";
	public  static final String PARSE_MORSE          = "com.jakewharton.smsmorse.CONVERT_FROM_MORSE";
	public  static final String PARSE_MORSE_KEY      = "button_presses";
	
	//Mirrors preference_vibrate_parts_entry_values from arrays.xml
	private static final int VIBRATE_CONTENT_MESSAGE        = 0;
	private static final int VIBRATE_CONTENT_MESSAGE_SENDER = 1;
	private static final int VIBRATE_CONTENT_SENDER_MESSAGE = 2;
	private static final int VIBRATE_CONTENT_SENDER         = 3;
	
	//Preference defaults
	private static final boolean DEFAULT_ENABLED         = true;
	private static final String  DEFAULT_VIBRATE_PARTS   = Integer.toString(VIBRATE_CONTENT_MESSAGE);
	private static final boolean DEFAULT_VIBRATE_COUNTS  = false;
	private static final int     DEFAULT_DOT_LENGTH      = 150;
	private static final boolean DEFAULT_SCREEN_OFF_ONLY = true;
	private static final boolean DEFAULT_VIBRATE_NORMAL  = true;
	private static final boolean DEFAULT_VIBRATE_VIBRATE = true;
	private static final boolean DEFAULT_VIBRATE_SILENT  = false;
	private static final long    DEFAULT_INITIAL_PAUSE   = 500L;
	//private static final String  DEFAULT_ERROR_CHAR      = "_";
	//private static final float   DEFAULT_PERCENT_ERROR   = 0.2F;
	
	//Morse code
	private static final int DOTS_IN_DASH       = 3;
	private static final int DOTS_IN_GAP        = 1;
	private static final int DOTS_IN_LETTER_GAP = 3;
	private static final int DOTS_IN_WORD_GAP   = 7;
	
	//Character sets
	private static final String      CHARSET_MORSE  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,?'!/()&:;=+-_\"$@";
	private static final String      CHARSET_COUNTS = "0123456789";
	private static final boolean     DOT  = true;
	private static final boolean     DASH = false;
	private static final boolean[][] MORSE  = {
        {DOT, DASH}, //A
        {DASH, DOT, DOT, DOT}, //B
        {DASH, DOT, DASH, DOT}, //C
        {DASH, DOT, DOT}, //D
        {DOT}, //E
        {DOT, DOT, DASH, DOT}, //F
        {DASH, DASH, DOT}, //G
        {DOT, DOT, DOT, DOT}, //H
        {DOT, DOT}, //I
        {DOT, DASH, DASH, DASH}, //J
        {DASH, DOT, DASH}, //K
        {DOT, DASH, DOT, DOT}, //L
        {DASH, DASH}, //M
        {DASH, DOT}, //N
        {DASH, DASH, DASH}, //O
        {DOT, DASH, DASH, DOT}, //P
        {DASH, DASH, DOT, DASH}, //Q
        {DOT, DASH, DOT}, //R
        {DOT, DOT, DOT}, //S
        {DASH}, //T
        {DOT, DOT, DASH}, //U
        {DOT, DOT, DOT, DASH}, //V
        {DOT, DASH, DASH}, //W
        {DASH, DOT, DOT, DASH}, //X
        {DASH, DOT, DASH, DASH}, //Y
        {DASH, DASH, DOT, DOT}, //Z
        {DASH, DASH, DASH, DASH, DASH}, //0
        {DOT, DASH, DASH, DASH, DASH}, //1
        {DOT, DOT, DASH, DASH, DASH}, //2
        {DOT, DOT, DOT, DASH, DASH}, //3
        {DOT, DOT, DOT, DOT, DASH}, //4
        {DOT, DOT, DOT, DOT, DOT}, //5
        {DASH, DOT, DOT, DOT, DOT}, //6
        {DASH, DASH, DOT, DOT, DOT}, //7
        {DASH, DASH, DASH, DOT, DOT}, //8
        {DASH, DASH, DASH, DASH, DOT}, //9
        {DOT, DASH, DOT, DASH, DOT, DASH}, //.
        {DASH, DASH, DOT, DOT, DASH, DASH}, //,
        {DOT, DOT, DASH, DASH, DOT, DOT}, //?
        {DOT, DASH, DASH, DASH, DASH, DOT}, //'
        {DASH, DOT, DASH, DOT, DASH, DASH}, //!
        {DASH, DOT, DOT, DASH, DOT}, ///
        {DASH, DOT, DASH, DASH, DOT}, //(
        {DASH, DOT, DASH, DASH, DOT, DASH}, //)
        {DOT, DASH, DOT, DOT, DOT}, //&
        {DASH, DASH, DASH, DOT, DOT, DOT}, //:
        {DASH, DOT, DASH, DOT, DASH, DOT}, //;
        {DASH, DOT, DOT, DOT, DASH}, //=
        {DOT, DASH, DOT, DASH, DOT}, //+
        {DASH, DOT, DOT, DOT, DOT, DASH}, //-
        {DOT, DOT, DASH, DASH, DOT, DASH}, //_
        {DOT, DASH, DOT, DOT, DASH, DOT}, //"
        {DOT, DOT, DOT, DASH, DOT, DOT, DASH}, //$
        {DOT, DASH, DASH, DOT, DASH, DOT} //@
	};
	private static final boolean[][] COUNTS = {
		{DASH}, //0
		{DOT}, //1
		{DOT, DOT}, //2
		{DOT, DOT, DOT}, //3
		{DOT, DOT, DOT, DOT}, //4
		{DOT, DOT, DOT, DOT, DOT}, //5
		{DOT, DOT, DOT, DOT, DOT, DOT}, //6
		{DOT, DOT, DOT, DOT, DOT, DOT, DOT}, //7
		{DOT, DOT, DOT, DOT, DOT, DOT, DOT, DOT}, //8
		{DOT, DOT, DOT, DOT, DOT, DOT, DOT, DOT, DOT} //9
	};

	//Static variables
	private SharedPreferences settings  = null;
	private Resources         resources = null;
	private Vibrator          vibrator  = null;
	
	/**
	 * Called when an intent is received.
	 */
	public void onReceive(Context context, Intent intent) {
		//Save context-specific objects needed in other methods
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		resources = context.getResources();
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		
		final String action = intent.getAction();
		final Bundle extras = intent.getExtras();
		
		if (action.equals(EventReceiver.VIBRATE_IN_MORSE)) {
			this.vibrateMorse(this.convertToVibrations(extras.getString(EventReceiver.VIBRATE_IN_MORSE_KEY)));
		}
		/*else if (action.equals(PARSE_MORSE)) {
			Log.i(EventReceiver.TAG, "Parsed to: " + EventReceiver.parseMorse(extras.getLongArray(PARSE_MORSE_KEY)));
		}*/
		else if (action.equals(EventReceiver.SMS_RECEIVED)) {
			final boolean smsValid      = extras != null;
			final boolean enabled       = this.settings.getBoolean(this.resources.getString(R.string.preference_enabled), EventReceiver.DEFAULT_ENABLED);
			final boolean keygaurdOn    = ((KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
			final boolean screenOffOnly = this.settings.getBoolean(this.resources.getString(R.string.preference_screen_off_only), EventReceiver.DEFAULT_SCREEN_OFF_ONLY);
			
			final int audioMode = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
			final boolean activeAudioMode = (
				((audioMode == AudioManager.RINGER_MODE_NORMAL) && this.settings.getBoolean(this.resources.getString(R.string.preference_vibrate_normal), EventReceiver.DEFAULT_VIBRATE_NORMAL)) ||
				((audioMode == AudioManager.RINGER_MODE_VIBRATE) && this.settings.getBoolean(this.resources.getString(R.string.preference_vibrate_vibrate), EventReceiver.DEFAULT_VIBRATE_VIBRATE)) ||
				((audioMode == AudioManager.RINGER_MODE_SILENT) && this.settings.getBoolean(this.resources.getString(R.string.preference_vibrate_silent), EventReceiver.DEFAULT_VIBRATE_SILENT))
			);
			
			if (smsValid && enabled && activeAudioMode && (keygaurdOn || !screenOffOnly)) {
				//Create SMSMessages from PDUs in the Bundle
				final Object[] pdus = (Object[])extras.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++)
					messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				
				//Assemble 
				final ArrayList<Long> vibrations = new ArrayList<Long>();
				final int vibrateParts = Integer.parseInt(settings.getString(resources.getString(R.string.preference_vibrate_parts), EventReceiver.DEFAULT_VIBRATE_PARTS));
				for (SmsMessage message : messages) {
					if ((vibrateParts == EventReceiver.VIBRATE_CONTENT_SENDER) || (vibrateParts == EventReceiver.VIBRATE_CONTENT_SENDER_MESSAGE))
						vibrations.addAll(this.convertSenderToVibrations(context, message.getOriginatingAddress()));
					if (vibrateParts != EventReceiver.VIBRATE_CONTENT_SENDER)
						vibrations.addAll(this.convertToVibrations(message.getMessageBody()));
					if (vibrateParts == EventReceiver.VIBRATE_CONTENT_MESSAGE_SENDER)
						vibrations.addAll(this.convertSenderToVibrations(context, message.getOriginatingAddress()));
				}
				
				this.vibrateMorse(vibrations);
			}
		}
	}
	
	/**
	 * Turns a converts the sender, either phone number of optionally the
	 * contact name, into associated Morse code timings.
	 * 
	 * (Uses deprecated APIs to support pre-2.0 devices.)
	 * @param context
	 * @param sender
	 * @return <pre>ArrayList</pre> of <pre>Long</pre>s of off/on vibration intervals
	 */
	private ArrayList<Long> convertSenderToVibrations(Context context, String sender) {
		if (this.settings.getBoolean(context.getString(R.string.preference_lookup_contact_name), true)) {
			final String[] projection = new String[] { Contacts.PeopleColumns.DISPLAY_NAME };
			final String selection = Contacts.Phones.NUMBER + " = " + sender;
			final Cursor results = context.getContentResolver().query(Contacts.Phones.CONTENT_URI, projection, selection, null, Contacts.ContactMethods.PERSON_ID);
			
			if (results.moveToFirst()) {
				return this.convertToVibrations(results.getString(results.getColumnIndex(Contacts.PeopleColumns.DISPLAY_NAME)));
			}
		}
		return this.convertToVibrations(sender, true);
	}
	
	/**
	 * Issues the vibrations contained within the <pre>vibrationLongs</pre> parameter.
	 * @param vibrationLongs
	 */
	private void vibrateMorse(final ArrayList<Long> vibrationLongs) {
		final long[] vibrations = new long[vibrationLongs.size()];
		final StringBuffer morseVibrations = new StringBuffer("Vibrating Morse: ");

		//Unbox the array and generate a log line simultaneously
		for (int i = 0; i < vibrationLongs.size(); i++) {
			vibrations[i] = vibrationLongs.get(i);
			morseVibrations.append((i % 2 == 0) ? '-' : '+');
			morseVibrations.append(vibrationLongs.get(i));
		}
		
		this.vibrator.vibrate(vibrations, -1);
		Log.i(EventReceiver.TAG, morseVibrations.toString());
	}
	
	/*private String parseMorse(final long[] buttonPresses) {
		final char  errorChar    = settings.getString(resources.getString(R.string.preference_error_char), DEFAULT_ERROR_CHAR).charAt(0);
		final float errorAllowed = settings.getFloat(resources.getString(R.string.preference_error_allowed), DEFAULT_ERROR_ALLOWED);
		final float errorAllowedAbove = 1 + errorAllowed;
		final float errorAllowedBelow = 1 - errorAllowed;
		
		int lookupPointer = 0;
		StringBuilder message = new StringBuilder();
		final ArrayList<Long> presses = new ArrayList<Long>();
		final ArrayList<Long> pauses  = new ArrayList<Long>();
		
		for (int i = 1; i < buttonPresses.length; i += 2) {
			final long press = buttonPresses[i] - buttonPresses[i - 1];
			final long pressAverage = average(presses);
			
			lookupPointer *= 2;
			if ((press >= pressAverage * errorAllowedBelow) && (press <= pressAverage * errorAllowedAbove)) {
				//DOT
				lookupPointer++;
				presses.add(press);
			}
			else {
				//DASH
				lookupPointer += 2;
				
				//Only add the press to the others if it was in the allowable range
				if ((press >= pressAverage * DOTS_IN_DASH * errorAllowedBelow) && (press <= pressAverage * DOTS_IN_DASH * errorAllowedAbove)) {
					presses.add(press / DOTS_IN_DASH);
				}
			}
			
			if (i == buttonPresses.length - 1) {
				final long pause = buttonPresses[i + 1] - buttonPresses[i];
				final long pauseAverage = average(pauses);
				
				if ((pause >= pauseAverage * errorAllowedBelow) && (pause <= pauseAverage * errorAllowedAbove)) {
					pauses.add(pause);
				}
				else if ((pause >= pauseAverage * DOTS_IN_LETTER_GAP * errorAllowedBelow) && (pause <= pauseAverage * DOTS_IN_LETTER_GAP * errorAllowedAbove)) {
					if ((lookupPointer >= MORSE_BINARY_TREE.length()) || (MORSE_BINARY_TREE.charAt(lookupPointer) == MORSE_BINARY_NULL))
						message.append(errorChar);
					else
						message.append(MORSE_BINARY_TREE.charAt(lookupPointer));
					
					lookupPointer = 0;
					pauses.add(pause / DOTS_IN_LETTER_GAP);
				}
				else {
					message.append(' ');
					lookupPointer = 0;
					
					//Only add the pause to the others if it was in the allowable range
					if ((pause >= pauseAverage * DOTS_IN_WORD_GAP * errorAllowedBelow) && (pause <= pauseAverage * DOTS_IN_WORD_GAP * errorAllowedAbove)) {
						pauses.add(pause / DOTS_IN_WORD_GAP);
					}
				}
			}
		}
		
		return message.toString();
	}
	private int average(final ArrayList<Long> numberList) {
		long sum = 0;
		for (Long number : numberList)
			sum += (Long)number;
		return (int)(sum / numberList.size());
	}*/
    
	/**
     * Converts a string to associated Morse code timings.
     * @param message Message to convert
     * @return <pre>ArrayList</pre> of <pre>Long</pre>s of off/on vibration intervals
     */
	private ArrayList<Long> convertToVibrations(final String message) {
    	return this.convertToVibrations(message, false);
    }
	
	/**
	 * Converts a string to associated Morse code timings.
	 * @param message Message to convert.
	 * @param isNumber Boolean indicating whether or not the message is a phone number.
	 * @return <pre>ArrayList</pre> of <pre>Long</pre>s of off/on vibration intervals
	 */
	private ArrayList<Long> convertToVibrations(final String message, final boolean isNumber) {
    	final boolean vibrateCounts = this.settings.getBoolean(this.resources.getString(R.string.preference_vibrate_counts), EventReceiver.DEFAULT_VIBRATE_COUNTS);
    	
    	//Establish all lengths
		final long dot       = this.settings.getInt(this.resources.getString(R.string.preference_dot_length), EventReceiver.DEFAULT_DOT_LENGTH);
		final long dash      = dot * EventReceiver.DOTS_IN_DASH;
		final long gap       = dot * EventReceiver.DOTS_IN_GAP;
		final long letterGap = dot * EventReceiver.DOTS_IN_LETTER_GAP;
		final long wordGap   = dot * EventReceiver.DOTS_IN_WORD_GAP;
    	
    	final String[] words = message.toUpperCase().trim().split(" ");
    	final ArrayList<Long> vibrationObjects = new ArrayList<Long>();
    	final String charset = isNumber && vibrateCounts ? EventReceiver.CHARSET_COUNTS : EventReceiver.CHARSET_MORSE;
    	final boolean[][] lookups = isNumber && vibrateCounts ? EventReceiver.COUNTS : EventReceiver.MORSE;
    	
    	//Add initial pause
    	vibrationObjects.add(EventReceiver.DEFAULT_INITIAL_PAUSE);
    	
    	String word;
    	boolean[] letterBooleans;
    	int letterIndex;
    	for (int i = 0; i < words.length; i++) {
    		word = words[i];
    		
    		for (int j = 0; j < word.length(); j++) {
    			letterIndex = charset.indexOf(String.valueOf(word.charAt(j)));
    			
    			if (letterIndex >= 0) {
    				letterBooleans = lookups[letterIndex];
    				
    				for (int k = 0; k < letterBooleans.length; k++) {
    					vibrationObjects.add(letterBooleans[k] ? dot : dash);
    					
    					if (k < letterBooleans.length - 1)
    						vibrationObjects.add(gap);
    				}
    				if (j < word.length() - 1)
    					vibrationObjects.add(letterGap);
    			}
    		}
    		if (i < words.length - 1)
    			vibrationObjects.add(wordGap);
    	}
    	
    	return vibrationObjects;
    }
}