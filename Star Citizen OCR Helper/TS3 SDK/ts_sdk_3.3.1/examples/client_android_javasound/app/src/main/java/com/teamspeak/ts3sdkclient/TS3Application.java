package com.teamspeak.ts3sdkclient;

import android.app.Application;
import android.util.Log;

import com.teamspeak.ts3sdkclient.ts3sdk.Native;

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Alexej
 * Creation date: 08.02.17
 */
public class TS3Application extends Application {

    public static final String TAG = TS3Application.class.getSimpleName();

    private boolean cpuSupported;
    private Native nativeInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        //load the ts3library to check if the device CPU is supported.
        // Store the result in a variable and use it later in die Activity/Fragments for checks
        cpuSupported = doCPUCheck();
        if (cpuSupported) {
            nativeInstance = new Native(this.getApplicationContext());
            if (!nativeInstance.isInitialized()) {
                Log.e(TAG, "Native instance state error");
                nativeInstance = null;
            }
        }
    }

    private boolean doCPUCheck() {
        try {
            Native.Companion.loadLibrary();
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "CPU Check, no library to this device architecture found, missing CPU support! ", e);
            return false;
        }

        return true;
    }

    public boolean isCpuSupported() {
        return cpuSupported;
    }
    public Native getNativeInstance() { return nativeInstance; }
}
