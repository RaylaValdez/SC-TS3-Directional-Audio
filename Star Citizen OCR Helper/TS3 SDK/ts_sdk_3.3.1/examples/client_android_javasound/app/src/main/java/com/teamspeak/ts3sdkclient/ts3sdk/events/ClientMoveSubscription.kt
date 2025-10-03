package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent
import com.teamspeak.ts3sdkclient.ts3sdk.states.Visibility

/**
 * Will be called if other clients in current and subscribed channels being announced to the client.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   clientID                  - ID of the announced client
 *   oldChannelID              - ID of the subscribed channel where the client left visibility
 *   newChannelID              - ID of the subscribed channel where the client entered visibility
 *   visibility                - Visibility of the announced client. See the enum Visibility in clientlib_publicdefinitions.h
 *                               Values: ENTER_VISIBILITY, RETAIN_VISIBILITY, LEAVE_VISIBILITY
 */
data class ClientMoveSubscription(
        val serverConnectionHandlerID: Long = 0,
        val clientID: Int = 0,
        val oldChannelID: Long = 0,
        val newChannelID: Long = 0,
        @property:Visibility val visibility: Int = 0)
    : TsEvent()