package com.teamspeak.ts3sdkclient.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.teamspeak.ts3sdkclient.R;
import com.teamspeak.ts3sdkclient.SettingsActivity;

/**
 * Custom preference for the vad (voice activation detection) settings.
 */
public class VadPreference extends DialogPreference {

    private TextView mTextView;
    private int mValue;

    public VadPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // configure the dialog
        setDialogLayoutResource(R.layout.vad_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDefaultValue(SettingsActivity.DEFAULT_VAD);
        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mTextView = view.findViewById(R.id.textview_vad);
        SeekBar seekBar = view.findViewById(R.id.seekbar_vad);

        // set actual value
        mTextView.setText(String.valueOf(mValue));
        seekBar.setProgress(mValue + 50);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValue = progress - 50;
                if (mTextView != null) mTextView.setText(String.valueOf(mValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit()
                    .putInt(SettingsActivity.PREF_VAD, mValue).apply();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mValue = PreferenceManager.getDefaultSharedPreferences(this.getContext())
                    .getInt(SettingsActivity.PREF_VAD, SettingsActivity.DEFAULT_VAD);
        } else {
            // Set default value
            mValue = SettingsActivity.DEFAULT_VAD;
        }
    }
}
