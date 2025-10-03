package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class IgnoredWhisper(val serverConnectionHandlerID : Long = 0, val clientID : Int = 0)
    : TsEvent()