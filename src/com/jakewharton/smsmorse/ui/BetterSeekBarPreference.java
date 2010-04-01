package com.jakewharton.smsmorse.ui;

import com.jakewharton.smsmorse.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BetterSeekBarPreference extends DialogPreference {
    private static final int DEFAULT_VALUE = 100;
    
    private Drawable mMyIcon;
    private int mMin;
    private int mMax;
    private int mValue;
    private SeekBar mSeekBar;
    private TextView mTextView;

	private OnSeekBarChangeListener changeListener = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			setTextValue(arg1);
		}
		public void onStartTrackingTouch(SeekBar arg0) {}
		public void onStopTrackingTouch(SeekBar arg0) {}
	};

	public BetterSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BetterSeekBarPreference, 0, 0);
        mMin = 50;//a.getInt(R.styleable.BetterSeekBarPreference_min, 0);
        mMax = 250;//a.getInt(R.styleable.BetterSeekBarPreference_max, 100);
        mValue = 100;//a.getInt(R.styleable.BetterSeekBarPreference_defaultValue, 0);
        a.recycle();

        setDialogLayoutResource(R.layout.betterseekbar_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        // Steal the XML dialogIcon attribute's value
        mMyIcon = getDialogIcon();
        setDialogIcon(null);
    }
	
	public void setValue(int value) {
		mValue = value;
		if (mSeekBar != null) {
			mSeekBar.setProgress(value);
		}
		setTextValue(value);
		persistInt(value);
	}
	
	public int getValue() {
		return mValue + mMin;
	}
	
	private void setTextValue(int value) {
		if (mTextView != null) {
			mTextView.setText("Value: " + (value + mMin));
		}
	}

    @Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			super.onRestoreInstanceState(state);
			return;
		}
		
		SavedState myState = (SavedState)state;
		super.onRestoreInstanceState(myState.getSuperState());
		setValue(myState.value);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			return superState;
		}
		
		final SavedState myState = new SavedState(superState);
		myState.value = mValue;
		return myState;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			setValue(mSeekBar.getProgress());
		}
	}

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        final ImageView iconView = (ImageView)view.findViewById(R.id.icon);
        if (mMyIcon != null) {
            iconView.setImageDrawable(mMyIcon);
        } else {
            iconView.setVisibility(View.GONE);
        }
        mSeekBar = (SeekBar)view.findViewById(R.id.seekbar);
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(mValue);
        mSeekBar.setOnSeekBarChangeListener(changeListener);
        mTextView = (TextView)view.findViewById(R.id.value);
        setTextValue(mValue);
    }

    @Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, DEFAULT_VALUE);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		setValue(restorePersistedValue ? getPersistedInt(mValue) : ((Integer)defaultValue).intValue());
	}

	protected static SeekBar getSeekBar(View dialogView) {
        return (SeekBar)dialogView.findViewById(R.id.seekbar);
    }
	
    private static class SavedState extends BaseSavedState {
        int value;
        
        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
