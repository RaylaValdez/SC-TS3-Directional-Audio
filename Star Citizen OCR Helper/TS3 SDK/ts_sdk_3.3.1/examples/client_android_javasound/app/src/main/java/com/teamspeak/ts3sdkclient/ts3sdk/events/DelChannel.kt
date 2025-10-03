package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

/**
 * Callback when a channel was deleted.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   channelID                 - ID of the deleted channel
 *   invokerID                 - ID of the client who deleted the channel
 *   invokerName               - Name of the client who deleted the channel
 *   invokerUniqueIdentifier   - The unique ID of the client who requested the deletion.
 */
data class DelChannel(
        val serverConnectionHandlerID: Long = 0,
        val channelID: Long = 0,
        val invokerID: Int = 0,
        val invokerName: String = "",
        val invokerUniqueIdentifier: String = "")
    : TsEvent()
