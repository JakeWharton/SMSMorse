package com.jakewharton.smsmorse;

import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

public class SMSMorseService extends Service {
	private static Vibrator vib;
	
	final static int SHOW_NONE = 0;
	final static int SHOW_BEFORE = 1;
	final static int SHOW_AFTER = 2;
	static int SHOW_FROM = SHOW_NONE;
	
	static int DOT = 100;
	static int DASH = DOT * 3;
	static int GAP = DOT;
	static int LETTER_GAP = DOT * 3;
	static int WORD_GAP = DOT * 7;
	
	public static void updateDot(int length) {
		DOT = length;
		DASH = DOT * 3;
		GAP = DOT;
		LETTER_GAP = DOT * 3;
		WORD_GAP = DOT * 7;
	}
	
	final static String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,?'!/()&:;=+-_\"$@";
	final static int[][] MORSE = {
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
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        IntentFilter filter = new IntentFilter(SMSMorseReceiver.SMS_RECEIVED);
        BroadcastReceiver receiver = new SMSMorseReceiver();
        registerReceiver(receiver, filter);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
    public static void vibrateMessage(String message) {
		message = message.toUpperCase();
    	String[] words = message.split(" ");
    	String word;
    	int[] letter_ints;
    	ArrayList<Integer> ints = new ArrayList<Integer>();
    	ints.add(500);
    	for (int i = 0; i < words.length; i++) {
    		word = words[i];
    		for (int j = 0; j < word.length(); j++) {
    			int index = LETTERS.indexOf(String.valueOf(word.charAt(j)));
    			if (index >= 0) {
    				letter_ints = MORSE[index];
    				for (int k = 0; k < letter_ints.length; k++) {
    					ints.add(letter_ints[k]);
    					if (k < letter_ints.length - 1)
    						ints.add(GAP);
    				}
    				if (j < word.length() - 1)
    					ints.add(LETTER_GAP);
    			}
    		}
    		if (i < words.length - 1)
    			ints.add(WORD_GAP);
    	}
    	long[] longs = new long[ints.size()];
    	String v = "";
    	boolean b = false;
    	for (int i = 0; i < ints.size(); i++) {
    		longs[i] = ints.get(i);
    		v += (b?"+":"-") + ints.get(i).toString();
    		b = !b;
    		if (i < ints.size() - 1)
    			v += ' ';
    	}
    	vib.vibrate(longs, -1);
    	Log.i("SMSMorseService", "Vibrating Morse: " + v);
    }
    
    public class SMSMorseReceiver extends BroadcastReceiver {
    	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    	
    	public void onReceive(Context context, Intent intent) {
    		if (intent.getAction().equals(SMS_RECEIVED)) {
    			Bundle bundle = intent.getExtras();
    			if (bundle != null) {
    				Object[] pdus = (Object[])bundle.get("pdus");
    				SmsMessage[] messages = new SmsMessage[pdus.length];
    				for (int i = 0; i < pdus.length; i++)
    					messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
    				for (SmsMessage message : messages) {
    					String body = message.getMessageBody();
    					if (SHOW_FROM == SHOW_BEFORE)
    						body = message.getOriginatingAddress() + ' ' + body;
    					else if (SHOW_FROM == SHOW_AFTER)
    						body += ' ' + message.getDisplayOriginatingAddress();
    					vibrateMessage(body);
    					Log.i("SMSMorseService", "Vibrated: " + body);
    				}
    			}
    		}
    	}
    }
}