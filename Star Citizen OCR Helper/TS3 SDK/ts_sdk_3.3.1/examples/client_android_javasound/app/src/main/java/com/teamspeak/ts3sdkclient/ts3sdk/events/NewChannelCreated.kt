package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

/**
 * Callback for just created channels.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   channelID                 - ID of the announced channel
 *   channelParentID           - ID of the parent channel
 *   invokerID                 - ID of the client who created the channel
 *   invokerName               - Name of the client who created the channel
 *   invokerUniqueIdentifier   - Unique ID of the client who requested the creation.
 */
data class NewChannelCreated(
        val serverConnectionHandlerID: Long = 0,
        val channelID: Long = 0,
        val channelParentID: Long = 0,
        val invokerID: Int = 0,
        val invokerName: String = "",
        val invokerUniqueIdentifier: String = "")
    : TsEvent()
