package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

// TODO ConnectStatus
/**
 * Callback for connection status change.
 * Connection status switches through the states STATUS_DISCONNECTED, STATUS_CONNECTING, STATUS_CONNECTED and STATUS_CONNECTION_ESTABLISHED.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   newStatus                 - New connection status, see the enum ConnectStatus in clientlib_publicdefinitions.h
 *   errorNumber               - Error code. Should be zero when connecting or actively disconnection.
 *                               Contains error state when losing connection.
 */
data class ConnectStatusChange(
        val serverConnectionHandlerID: Long = 0,
        val newStatus: Int = 0,
        val errorNumber: Int = 0)
    : TsEvent()
