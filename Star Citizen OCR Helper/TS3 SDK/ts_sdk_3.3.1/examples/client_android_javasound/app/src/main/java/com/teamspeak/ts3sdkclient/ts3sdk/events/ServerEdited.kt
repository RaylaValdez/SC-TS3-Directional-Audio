package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class ServerEdited(
        val serverConnectionHandlerID : Long = 0,
        val editerID : Int = 0,
        val editerName : String = "",
        val editerUniqueIdentifier : String = "")
    : TsEvent()