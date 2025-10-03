package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class ChannelMove(
        val serverConnectionHandlerID : Long = 0,
        val channelID : Long = 0,
        val newChannelParentID : Long = 0,
        val invokerID : Int = 0,
        val invokerName : String = "",
        val invokerUniqueIdentifier : String = "")
    : TsEvent()