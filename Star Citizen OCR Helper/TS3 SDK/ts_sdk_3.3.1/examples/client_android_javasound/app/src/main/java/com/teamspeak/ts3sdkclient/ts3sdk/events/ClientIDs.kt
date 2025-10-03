package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class ClientIDs(
        val serverConnectionHandlerID : Long = 0,
        val uniqueClientIdentifier : String = "",
        val clientID : Int = 0,
        val clientName : String = "")
    : TsEvent()