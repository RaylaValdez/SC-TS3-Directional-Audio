package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

/**
 * Callback for current channels being announced to the client after connecting to a server.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   channelID                 - ID of the announced channel
 *   channelParentID           - ID of the parent channel
 */
data class NewChannel(
        val serverConnectionHandlerID: Long = 0,
        val channelID: Long = 0,
        val channelParentID: Long = 0)
    : TsEvent()
