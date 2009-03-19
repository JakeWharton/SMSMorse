package com.jakewharton.smsmorse;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

public class SMSMorseReceiver extends BroadcastReceiver {
	private static final String TAG = "SMSMorseReceiver";
	
	public static final String VIBRATE_MORSE = "com.jakewharton.smsmorse.VIBRATE_MORSE";
	public static final String VIBRATE_MORSE_KEY = "test_message";
	
	//Preferences
	private static final int VIBRATE_CONTENT_MESSAGE = 0;
	private static final int VIBRATE_CONTENT_MESSAGE_SENDER = 1;
	private static final int VIBRATE_CONTENT_SENDER_MESSAGE = 2;
	private static final int VIBRATE_CONTENT_SENDER = 3;
	private static final int DEFAULT_DOT_LENGTH = 100;
	private static final String DEFAULT_VIBRATE_PARTS = Integer.toString(VIBRATE_CONTENT_MESSAGE);
	private static final boolean DEFAULT_ENABLED = true;
	private static final boolean DEFAULT_VIBRATE_COUNTS = false;
	private static final long DEFAULT_INITIAL_PAUSE = 500L;
	
	//Morse code
	private final static boolean DOT = true;
	private final static boolean DASH = false;
	private final static int DOTS_IN_DASH = 3;
	private final static int DOTS_IN_GAP = 1;
	private final static int DOTS_IN_LETTER_GAP = 3;
	private final static int DOTS_IN_WORD_GAP = 7;
	
	//Character sets
	private final static String CHARSET_MORSE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,?'!/()&:;=+-_\"$@";
	private final static boolean[][] MORSE = {
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
	private final static String CHARSET_COUNTS = "0123456789";
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
	
	private Vibrator vibrator;
	
	public void onReceive(Context context, Intent intent) {
		final SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(context);
		
		final boolean IsEnabled = Settings.getBoolean(context.getString(R.string.preference_enabled), DEFAULT_ENABLED);
		final Bundle bundle = intent.getExtras();
		
		if (IsEnabled && bundle != null) {
			final int vibrateParts = Integer.parseInt(Settings.getString(context.getString(R.string.preference_vibrate_parts), DEFAULT_VIBRATE_PARTS));
			final boolean vibrateCounts = Settings.getBoolean(context.getString(R.string.preference_vibrate_counts), DEFAULT_VIBRATE_COUNTS);
			final int dotLength = Settings.getInt(context.getString(R.string.preference_dot_length), DEFAULT_DOT_LENGTH);
			
			final ArrayList<Long> VibrationObjects = new ArrayList<Long>();
			
			final String TestMessage = bundle.getString(VIBRATE_MORSE_KEY);
			if (TestMessage != null) {
				VibrationObjects.addAll(toVibrations(TestMessage, dotLength));
			}
			else {
				//Create SMSMessages from PDUs in the Bundle
				final Object[] pdus = (Object[])bundle.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++)
					messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				
				//Convert message into vibrations
			
				for (SmsMessage message : messages) {
					if ((vibrateParts == VIBRATE_CONTENT_SENDER) || (vibrateParts == VIBRATE_CONTENT_SENDER_MESSAGE))
						VibrationObjects.addAll(toVibrations(message.getOriginatingAddress(), dotLength, vibrateCounts));
					if (vibrateParts != VIBRATE_CONTENT_SENDER)
						VibrationObjects.addAll(toVibrations(message.getMessageBody(), dotLength));
					if (vibrateParts == VIBRATE_CONTENT_MESSAGE_SENDER)
						VibrationObjects.addAll(toVibrations(message.getOriginatingAddress(), dotLength, vibrateCounts));
				}
			}
		   	
			//Unbox the array and generate a log line simultaneously
			final long[] Vibrations = new long[VibrationObjects.size()];
			final StringBuffer MorseVibrations = new StringBuffer("Vibrating Morse: ");
			boolean VibrationOn = false;
			for (int i = 0; i < VibrationObjects.size(); i++) {
				Vibrations[i] = VibrationObjects.get(i);
				MorseVibrations.append(VibrationOn ? '+' : '-');
				MorseVibrations.append(VibrationObjects.get(i));
				VibrationOn = !VibrationOn;
			}
			
			if (vibrator == null) {
				vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
			}
			vibrator.vibrate(Vibrations, -1);
			Log.i(TAG, MorseVibrations.toString());
		}
	}

	private ArrayList<Long> toVibrations(final String message, final int dotLength) {
		return toVibrations(message, dotLength, false);
	}
	
    private ArrayList<Long> toVibrations(final String message, final int dotLength, final boolean countCharSet) {
    	//Establish all lengths
    	final long Dot = (long)dotLength;
		final long Dash = Dot * DOTS_IN_DASH;
		final long Gap = Dot * DOTS_IN_GAP;
		final long LetterGap = Dot * DOTS_IN_LETTER_GAP;
		final long WordGap = Dot * DOTS_IN_WORD_GAP;
    	
    	final String[] Words = message.toUpperCase().split(" ");
    	final ArrayList<Long> VibrationObjects = new ArrayList<Long>();
    	final String Charset = countCharSet ? CHARSET_COUNTS : CHARSET_MORSE;
    	final boolean[][] Lookups = countCharSet ? COUNTS : MORSE;
    	
    	//Add initial pause
    	VibrationObjects.add(DEFAULT_INITIAL_PAUSE);
    	
    	String Word;
    	boolean[] LetterBooleans;
    	int LetterIndex;
    	for (int i = 0; i < Words.length; i++) {
    		Word = Words[i];
    		
    		for (int j = 0; j < Word.length(); j++) {
    			LetterIndex = Charset.indexOf(String.valueOf(Word.charAt(j)));
    			
    			if (LetterIndex >= 0) {
    				LetterBooleans = Lookups[LetterIndex];
    				
    				for (int k = 0; k < LetterBooleans.length; k++) {
    					VibrationObjects.add(LetterBooleans[k] ? Dot : Dash);
    					
    					if (k < LetterBooleans.length - 1)
    						VibrationObjects.add(Gap);
    				}
    				if (j < Word.length() - 1)
    					VibrationObjects.add(LetterGap);
    			}
    		}
    		if (i < Words.length - 1)
    			VibrationObjects.add(WordGap);
    	}
    	
    	return VibrationObjects;
    }
}