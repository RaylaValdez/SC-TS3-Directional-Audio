package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class TextMessage (
        val serverConnectionHandlerID : Long = 0,
        val targetMode : Int = 0,
        val toID : Int = 0,
        val fromID : Int = 0,
        val fromName : String = "",
        val fromUniqueIdentifier : String = "",
        val message : String = "")
    : TsEvent()