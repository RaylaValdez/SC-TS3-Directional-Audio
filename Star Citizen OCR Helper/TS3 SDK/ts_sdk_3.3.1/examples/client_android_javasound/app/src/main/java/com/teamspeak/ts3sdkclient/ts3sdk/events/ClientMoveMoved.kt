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
 *   moverID                   - Defines the ID of the client who initiated the move
 *   moverName                 - Defines the nickname of the client who initiated the move
 *   moverUniqueIdentifier     - The unique ID of the client who initiated the move
 *   moveMessage               - Contains a string giving the reason for the move
 */
data class ClientMoveMoved(
        val serverConnectionHandlerID: Long = 0,
        val clientID: Int = 0,
        val oldChannelID: Long = 0,
        val newChannelID: Long = 0,
        @property:Visibility val visibility: Int = 0,
        val moverID: Int = 0,
        val moverName: String = "",
        val moverUniqueIdentifier: String = "",
        val moveMessage: String = "")
    : TsEvent()
