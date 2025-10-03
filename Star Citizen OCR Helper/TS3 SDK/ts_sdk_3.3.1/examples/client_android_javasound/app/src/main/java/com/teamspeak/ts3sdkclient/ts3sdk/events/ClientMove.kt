package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent
import com.teamspeak.ts3sdkclient.ts3sdk.states.Visibility

/**
 * Called when a client joins, leaves or moves to another channel.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   clientID                  - ID of the moved client
 *   oldChannelID              - ID of the old channel left by the client
 *   newChannelID              - ID of the new channel joined by the client
 *   visibility                - Visibility of the moved client. See the enum Visibility in clientlib_publicdefinitions.h
 *                               Values: ENTER_VISIBILITY, RETAIN_VISIBILITY, LEAVE_VISIBILITY
 *   moveMessage               - When a client disconnects from the server, this includes the optional message set by the disconnecting client in
 *                               ts3client_stopConnection.
 */
data class ClientMove(
        val serverConnectionHandlerID: Long = 0,
        val clientID: Int = 0,
        val oldChannelID: Long = 0,
        val newChannelID: Long = 0,
        @property:Visibility val visibility: Int = 0,
        val moveMessage: String = "")
    : TsEvent()