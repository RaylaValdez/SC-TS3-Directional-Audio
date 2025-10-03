package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent
import com.teamspeak.ts3sdkclient.ts3sdk.states.Visibility

data class ClientKickFromChannel(
        val serverConnectionHandlerID : Long = 0,
        val clientID : Int = 0,
        val oldChannelID : Long = 0,
        val newChannelID : Long = 0,
        @property:Visibility val visibility: Int = 0,
        val kickerID : Int = 0,
        val kickerName : String = "",
        val kickerUniqueIdentifier : String = "",
        val kickMessage : String = "")
    : TsEvent()