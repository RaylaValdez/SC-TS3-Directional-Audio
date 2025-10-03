package com.teamspeak.ts3sdkclient;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import com.teamspeak.ts3sdkclient.connection.ConnectionHandler;
import com.teamspeak.ts3sdkclient.service.ConnectionService;
import com.teamspeak.ts3sdkclient.service.ServiceUtils;

import static com.teamspeak.ts3sdkclient.Constants.PRE_PROCESSOR_VALUE_AGC;
import static com.teamspeak.ts3sdkclient.Constants.PRE_PROCESSOR_VALUE_DENOISE;
import static com.teamspeak.ts3sdkclient.Constants.PRE_PROCESSOR_VALUE_VOICE_ACTIVATION_LEVEL_DB;

/**
 * This activity displays available settings. Changes in the settings will be applied immediately.
 */
public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Preference tag for denoise setting
     */
    public static final String PREF_DENOISE = "pref_denoise";
    /**
     * Preference tag for vad value setting
     */
    public static final String PREF_VAD = "pref_vad";
    /**
     * Preference tag for automatic gain control value setting
     */
    public static final String PREF_AGC = "pref_agc";
    /**
     * Default value for VAD (voice activation detection)
     */
    public static final int DEFAULT_VAD = -35;
    /**
     * Default value for AGC (automatic gain control)
     */
    public static final boolean DEFAULT_AGC = true;
    /**
     * Default value for denoise
     */
    public static final boolean DEFAULT_DENOISE = true;

    private ConnectionHandler mConnectionHandler;
    private boolean mConnectionBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService connectionService = ((ConnectionService.LocalBinder) service).getService();
            mConnectionHandler = connectionService.getConnectionHandler();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // do nothing
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // bind to services
        mConnectionBound = ServiceUtils.bindService(this, mServiceConnection);

        // register for preference changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unbind from services
        ServiceUtils.unBindService(this, mServiceConnection);

        // unregister callbacks
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // check if we are connected to service then react to preference changes
        if (mConnectionBound && mConnectionHandler != null) {
            switch (key) {
                case SettingsActivity.PREF_AGC: {
                    // setting entered value for agc
                    String value = String.valueOf(sharedPreferences.getBoolean(
                            SettingsActivity.PREF_AGC, false));
                    mConnectionHandler.setPreProcessorConfigValue(PRE_PROCESSOR_VALUE_AGC, value);
                    break;
                }
                case SettingsActivity.PREF_DENOISE: {
                    // setting entered value for denoise
                    String value = String.valueOf(sharedPreferences.getBoolean(
                            SettingsActivity.PREF_DENOISE, false));
                    mConnectionHandler.setPreProcessorConfigValue(PRE_PROCESSOR_VALUE_DENOISE, value);
                    break;
                }
                case SettingsActivity.PREF_VAD: {
                    // setting entered value for vad level
                    String value = String.valueOf(sharedPreferences.getInt(
                            SettingsActivity.PREF_VAD, SettingsActivity.DEFAULT_VAD));
                    mConnectionHandler.setPreProcessorConfigValue(
                            PRE_PROCESSOR_VALUE_VOICE_ACTIVATION_LEVEL_DB, value);
                    break;
                }
            }
        }
    }
}
