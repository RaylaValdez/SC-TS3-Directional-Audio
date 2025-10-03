
package com.teamspeak.ts3sdkclient.service;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 08.02.17
 *
 * Simple class that wraps Service calls and functions (start/stop, bind/unbind)
 */
public class ServiceUtils {

    /**
     * start the {@link ConnectionService}
     * @param context - the activity
     */
    public static void startService(Context context){
        context.startService(new Intent(context, ConnectionService.class));
    }

    /**
     * stop the {@link ConnectionService}
     * @param context - the activity
     */
    public static void stopService(Context context){
        context.stopService(new Intent(context, ConnectionService.class));
    }

    /**
     * bind the {@link ConnectionService}
     * @param context - the activity
     * @param serviceConnection - {@link ServiceConnection} callback
     * @return If you have successfully bound to the service, {@code true} is returned;
     *         {@code false} is returned if the connection is not made so you will not
     *         receive the service object.
     */
    public static boolean bindService(Context context, ServiceConnection serviceConnection){
        return context.bindService(new Intent(context, ConnectionService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * unbind the {@link ServiceConnection}
     * @param context - the activity
     * @param serviceConnection - {@link ServiceConnection} callback
     */
    public static void unBindService(Context context, ServiceConnection serviceConnection){
        context.unbindService(serviceConnection);
    }

    /**
     * tells you if the {@link ConnectionService} is running
     * @param context - the activity
     * @return {@code true} if service is running, {@code false} if not
     */
    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ConnectionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
