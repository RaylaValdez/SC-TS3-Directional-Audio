package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class FileTransferStatus(
        val transferID : Int = 0,
        val status : Int = 0,
        val statusMessage : String = "",
        val remoteFileSize : Long = 0,
        val serverConnectionHandlerID : Long = 0)
    : TsEvent()