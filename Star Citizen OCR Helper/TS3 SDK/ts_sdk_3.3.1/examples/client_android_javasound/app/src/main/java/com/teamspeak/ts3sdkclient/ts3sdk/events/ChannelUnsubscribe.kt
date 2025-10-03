package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class ChannelUnsubscribe(val serverConnectionHandlerID : Long = 0, val channelID : Int = 0)
    : TsEvent()