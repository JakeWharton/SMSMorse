package com.jakewharton.smsmorse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SMSMorse extends Activity {
	private static Intent SERVICE = null;
	private static boolean SERVICE_RUNNING = false;
	
    private OnClickListener testListener = new OnClickListener() {
    	public void onClick(View v) {
    		SMSMorseService.vibrateMessage(((EditText)findViewById(R.id.test_text)).getText().toString());
    	}
    };
    private OnClickListener enabledListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (SERVICE_RUNNING)
    			stopService(SERVICE);
    		else
    			startService(SERVICE);
    		SERVICE_RUNNING = ((ToggleButton)findViewById(R.id.enabled)).isChecked();
    	}
    };
    private OnSeekBarChangeListener lengthListener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			arg1 += 50;
			SMSMorseService.updateDot(arg1);
			((TextView)findViewById(R.id.length_text)).setText("Dot Length: " + arg1 + "ms");
		}
		public void onStartTrackingTouch(SeekBar arg0) {}
		public void onStopTrackingTouch(SeekBar arg0) {}
    };
    private OnClickListener noneListener = new OnClickListener() {
    	public void onClick(View v) {
    		SMSMorseService.SHOW_FROM = SMSMorseService.SHOW_NONE;
    	}
    };
    private OnClickListener beforeListener = new OnClickListener() {
    	public void onClick(View v) {
    		SMSMorseService.SHOW_FROM = SMSMorseService.SHOW_BEFORE;
    	}
    };
    private OnClickListener afterListener = new OnClickListener() {
    	public void onClick(View v) {
    		SMSMorseService.SHOW_FROM = SMSMorseService.SHOW_AFTER;
    	}
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (SERVICE == null) {
        	SERVICE = new Intent(this, SMSMorseService.class);
        	startService(SERVICE);
        	SERVICE_RUNNING = true;
        }
        
        setContentView(R.layout.main);
        
        //Service enable/disable toggle button
        ToggleButton enabled = (ToggleButton)findViewById(R.id.enabled);
        enabled.setOnClickListener(enabledListener);
        enabled.setChecked(SERVICE_RUNNING);
        
        //Seek bar dot length changer
        TextView bar_text = (TextView)findViewById(R.id.length_text);
        bar_text.setText("Dot Length: " + SMSMorseService.DOT + "ms");
        SeekBar bar = (SeekBar)findViewById(R.id.length);
        bar.setOnSeekBarChangeListener(lengthListener);
        bar.setProgress(SMSMorseService.DOT - 50);
        
        //Radio buttons
        RadioButton none = (RadioButton)findViewById(R.id.from_none);
        none.setOnClickListener(noneListener);
        RadioButton before = (RadioButton)findViewById(R.id.from_before);
        before.setOnClickListener(beforeListener);
        RadioButton after = (RadioButton)findViewById(R.id.from_after);
        after.setOnClickListener(afterListener);
        if (SMSMorseService.SHOW_FROM == SMSMorseService.SHOW_BEFORE)
        	before.setChecked(true);
        else if (SMSMorseService.SHOW_FROM == SMSMorseService.SHOW_AFTER)
        	after.setChecked(true);
        else
        	none.setChecked(true);
        
        //Test button
        Button test = (Button)findViewById(R.id.test);
        test.setOnClickListener(testListener);
    }
}