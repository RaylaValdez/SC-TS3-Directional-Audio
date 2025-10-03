
package com.teamspeak.ts3sdkclient.ts3sdk.states;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Alexej
 * Creation date: 08.02.17
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({ConnectStatus.STATUS_DISCONNECTED, ConnectStatus.STATUS_CONNECTING, ConnectStatus.STATUS_CONNECTED, ConnectStatus.STATUS_CONNECTION_ESTABLISHING,  ConnectStatus.STATUS_CONNECTION_ESTABLISHED})
public @interface ConnectStatus {
    int STATUS_DISCONNECTED = 0;            // There is no activity to the server, this is the default value
    int STATUS_CONNECTING = 1;              // We are trying to connect, we haven't got a clientID yet, we haven't been accepted by the server
    int STATUS_CONNECTED = 2;               // The server has accepted us, we can talk and hear and we got a clientID, but we don't have the
    // channels and clients yet, we can get server infos (welcome msg etc.)
    int STATUS_CONNECTION_ESTABLISHING = 3; // we are CONNECTED and we are visible
    int STATUS_CONNECTION_ESTABLISHED = 4;  // we are CONNECTED and we have the client and channels available
}