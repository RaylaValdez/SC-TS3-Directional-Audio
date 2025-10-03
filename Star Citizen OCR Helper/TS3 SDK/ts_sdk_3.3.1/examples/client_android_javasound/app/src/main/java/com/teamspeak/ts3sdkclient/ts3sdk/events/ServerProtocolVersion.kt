package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class ServerProtocolVersion(val serverConnectionHandlerID: Long = 0, val version : Int = 0)
    : TsEvent()