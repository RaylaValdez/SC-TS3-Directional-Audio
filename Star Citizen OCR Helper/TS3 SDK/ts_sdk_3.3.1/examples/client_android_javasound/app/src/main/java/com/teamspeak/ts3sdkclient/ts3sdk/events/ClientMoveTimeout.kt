package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent
import com.teamspeak.ts3sdkclient.ts3sdk.states.Visibility

/**
 * Called when a client drops his connection.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   clientID                  - ID of the moved client
 *   oldChannelID              - ID of the channel the leaving client was previously member of
 *   newChannelID              - 0, as client is leaving
 *   visibility                - Always LEAVE_VISIBILITY
 *   timeoutMessage            - Optional message giving the reason for the timeout
 */
data class ClientMoveTimeout(
        val serverConnectionHandlerID: Long = 0,
        val clientID: Int = 0,
        val oldChannelID: Long = 0,
        val newChannelID: Long = 0,
        @property:Visibility val visibility: Int = 0,
        val timeoutMessage: String = "")
    : TsEvent()
