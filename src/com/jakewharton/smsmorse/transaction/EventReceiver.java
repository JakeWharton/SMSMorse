package com.jakewharton.smsmorse.transaction;

import java.util.ArrayList;

import com.jakewharton.smsmorse.R;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
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
	private static final String  DEFAULT_ERROR_CHAR      = "_";
	private static final float   DEFAULT_ERROR_ALLOWED   = 0.2F;
	
	//Morse code
	private final static int DOTS_IN_DASH       = 3;
	private final static int DOTS_IN_GAP        = 1;
	private final static int DOTS_IN_LETTER_GAP = 3;
	private final static int DOTS_IN_WORD_GAP   = 7;
	
	//Character sets
	private final static String      CHARSET_MORSE  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,?'!/()&:;=+-_\"$@";
	private final static String      CHARSET_COUNTS = "0123456789";
	private final static boolean     DOT  = true;
	private final static boolean     DASH = false;
	private final static boolean[][] MORSE  = {
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
	private final static boolean[][] COUNTS = {
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

	//Binary lookup tree. Left for every dot, right for every dash
	private final static String MORSE_BINARY_LOOKUP = 
"                                       #                                "+
"                      E                                T                "+
"             I                 A               N               M        "+
"       S          U       R        W       D       K       G       O    "+
"   H       V    F   #   L    A   P   J   B   X   C   Y   Z   Q   #   #  "+
" 5   4   #   3 # # # 2 & #  + # # # # 1 6 = / # # # ( # 7 # # # 8 # 9 0 "+
"# # # # # #  ######?_####\"##.####@###'##-########;!#)#####,####:#######"+
"#########$".replace(" ", "").replace("\n", "");
	
	//Instance variables
	private SharedPreferences settings;
	private Resources         resources;
	private Vibrator          vibrator;
	
	public void onReceive(Context context, Intent intent) {
		//Save context-specific objects needed in other methods
		settings  = PreferenceManager.getDefaultSharedPreferences(context);
		resources = context.getResources();
		vibrator  = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		
		final String action = intent.getAction();
		final Bundle extras = intent.getExtras();
		
		if (action.equals(VIBRATE_IN_MORSE)) {
			vibrateMorse(convertToVibrations(extras.getString(VIBRATE_IN_MORSE_KEY)));
		}
		else if (action.equals(PARSE_MORSE)) {
			Log.i(TAG, "Parsed to: " + parseMorse(extras.getLongArray(PARSE_MORSE_KEY)));
		}
		else if (action.equals(SMS_RECEIVED)) {
			final boolean smsValid      = extras != null;
			final boolean enabled       = settings.getBoolean(resources.getString(R.string.preference_enabled), DEFAULT_ENABLED);
			final boolean keygaurdOn    = ((KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
			final boolean screenOffOnly = settings.getBoolean(resources.getString(R.string.preference_screen_off_only), DEFAULT_SCREEN_OFF_ONLY);
			
			final int audioMode = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
			final boolean activeAudioMode = (
				((audioMode == AudioManager.RINGER_MODE_NORMAL) && settings.getBoolean(resources.getString(R.string.preference_vibrate_normal), DEFAULT_VIBRATE_NORMAL)) ||
				((audioMode == AudioManager.RINGER_MODE_VIBRATE) && settings.getBoolean(resources.getString(R.string.preference_vibrate_vibrate), DEFAULT_VIBRATE_VIBRATE)) ||
				((audioMode == AudioManager.RINGER_MODE_SILENT) && settings.getBoolean(resources.getString(R.string.preference_vibrate_silent), DEFAULT_VIBRATE_SILENT))
			);
			
			if (smsValid && enabled && activeAudioMode && (keygaurdOn || !screenOffOnly)) {
				//Create SMSMessages from PDUs in the Bundle
				final Object[] pdus = (Object[])extras.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++)
					messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				
				//Assemble 
				final ArrayList<Long> vibrations = new ArrayList<Long>();
				final int vibrateParts = Integer.parseInt(settings.getString(resources.getString(R.string.preference_vibrate_parts), DEFAULT_VIBRATE_PARTS));
				for (SmsMessage message : messages) {
					if ((vibrateParts == VIBRATE_CONTENT_SENDER) || (vibrateParts == VIBRATE_CONTENT_SENDER_MESSAGE))
						vibrations.addAll(convertToVibrations(message.getOriginatingAddress(), true));
					if (vibrateParts != VIBRATE_CONTENT_SENDER)
						vibrations.addAll(convertToVibrations(message.getMessageBody()));
					if (vibrateParts == VIBRATE_CONTENT_MESSAGE_SENDER)
						vibrations.addAll(convertToVibrations(message.getOriginatingAddress(), true));
				}
				
				vibrateMorse(vibrations);
			}
		}
	}
	private void vibrateMorse(final ArrayList<Long> vibrationLongs) {
		final long[] vibrations = new long[vibrationLongs.size()];
		final StringBuffer morseVibrations = new StringBuffer("Vibrating Morse: ");

		//Unbox the array and generate a log line simultaneously
		for (int i = 0; i < vibrationLongs.size(); i++) {
			vibrations[i] = vibrationLongs.get(i);
			morseVibrations.append((i % 2 == 0) ? '-' : '+');
			morseVibrations.append(vibrationLongs.get(i));
		}
		
		vibrator.vibrate(vibrations, -1);
		Log.i(TAG, morseVibrations.toString());
	}
	private String parseMorse(final long[] buttonPresses) {
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
					if ((lookupPointer >= MORSE_BINARY_LOOKUP.length()) || (MORSE_BINARY_LOOKUP.charAt(lookupPointer) == '#'))
						message.append(errorChar);
					else
						message.append(MORSE_BINARY_LOOKUP.charAt(lookupPointer));
					
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
	}
    private ArrayList<Long> convertToVibrations(final String message) {
    	return convertToVibrations(message, false);
    }
	private ArrayList<Long> convertToVibrations(final String message, final boolean isNumber) {
    	final boolean vibrateCounts = settings.getBoolean(resources.getString(R.string.preference_vibrate_counts), DEFAULT_VIBRATE_COUNTS);
    	
    	//Establish all lengths
		final long dot       = settings.getInt(resources.getString(R.string.preference_dot_length), DEFAULT_DOT_LENGTH);
		final long dash      = dot * DOTS_IN_DASH;
		final long gap       = dot * DOTS_IN_GAP;
		final long letterGap = dot * DOTS_IN_LETTER_GAP;
		final long wordGap   = dot * DOTS_IN_WORD_GAP;
    	
    	final String[] words = message.toUpperCase().trim().split(" ");
    	final ArrayList<Long> vibrationObjects = new ArrayList<Long>();
    	final String charset = isNumber && vibrateCounts ? CHARSET_COUNTS : CHARSET_MORSE;
    	final boolean[][] lookups = isNumber && vibrateCounts ? COUNTS : MORSE;
    	
    	//Add initial pause
    	vibrationObjects.add(DEFAULT_INITIAL_PAUSE);
    	
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