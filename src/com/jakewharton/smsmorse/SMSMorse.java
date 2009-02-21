package com.jakewharton.smsmorse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class SMSMorse extends Activity {
    private OnClickListener testListener = new OnClickListener() {
    	public void onClick(View v) {
    		String text = ((EditText)findViewById(R.id.test_text)).getText().toString();
    		SMSMorseService.vibrateMessage(text);
    	}
    };
    
    private OnClickListener enabledListener = new OnClickListener() {
    	public void onClick(View v) {
    		SMSMorseService.ENABLED = ((ToggleButton)findViewById(R.id.enabled)).isChecked();
    	}
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button test = (Button)findViewById(R.id.test);
        test.setOnClickListener(testListener);
        ToggleButton enabled = (ToggleButton)findViewById(R.id.enabled);
        enabled.setOnClickListener(enabledListener);
        startService(new Intent(this, SMSMorseService.class));
    }
}