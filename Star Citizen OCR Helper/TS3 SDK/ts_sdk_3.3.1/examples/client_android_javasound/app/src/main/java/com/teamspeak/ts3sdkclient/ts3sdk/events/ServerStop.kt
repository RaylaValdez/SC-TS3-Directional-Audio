package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class ServerStop(val serverConnectionHandlerID : Long = 0, val shutdownMessage : String = "")
    : TsEvent()