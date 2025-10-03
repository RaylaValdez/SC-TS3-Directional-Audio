package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class FileListFinished(
        val serverConnectionHandlerID : Long = 0,
        val channelID : Long = 0,
        val path : String = "")
    : TsEvent()