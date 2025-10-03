package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class FileList(
        val serverConnectionHandlerID : Long = 0,
        val channelID : Long = 0,
        val path : String = "",
        val name : String = "",
        val size : Long = 0,
        val datetime : Long = 0,
        val type : Int = 0,
        val incompleteSize : Long = 0,
        val returnCode : String = "")
    : TsEvent()
