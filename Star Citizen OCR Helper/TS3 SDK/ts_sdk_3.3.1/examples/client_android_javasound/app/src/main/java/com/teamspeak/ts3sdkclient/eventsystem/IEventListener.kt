package com.teamspeak.ts3sdkclient.eventsystem

/**
 * TeamSpeak SDK client sample
 *
 * Copyright (c) 2007-2018 TeamSpeak-Systems
 *
 * Eventmethod Interface for ts3clientlibrary events
 * All classes that want to receive ts3client library events have to implement this Interface
 */
interface IEventListener {
    fun onTS3Event(event: IEvent)
}
