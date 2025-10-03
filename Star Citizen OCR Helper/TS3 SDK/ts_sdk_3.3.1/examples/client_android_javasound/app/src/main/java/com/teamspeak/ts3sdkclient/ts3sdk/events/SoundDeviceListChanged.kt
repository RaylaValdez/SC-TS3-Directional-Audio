package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

data class SoundDeviceListChanged(val modeID : String = "", val playOrCap : Int)
    : TsEvent()