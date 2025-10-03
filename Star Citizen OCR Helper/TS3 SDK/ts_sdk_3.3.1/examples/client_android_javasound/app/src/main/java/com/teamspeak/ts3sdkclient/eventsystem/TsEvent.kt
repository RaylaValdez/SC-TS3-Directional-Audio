package com.teamspeak.ts3sdkclient.eventsystem

open class TsEvent : IEvent{

    fun Post() {
        Callbacks.fireEvent(this)
    }

}