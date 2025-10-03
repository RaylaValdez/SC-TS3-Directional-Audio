package com.teamspeak.ts3sdkclient.connection

/**
 * TeamSpeak SDK client sample
 *
 * Copyright (c) 2007-2018 TeamSpeak Systems
 *
 * Required parameters to establish a server connection
 */
data class ConnectionParams(
        var identity: String,
        var address: String = "127.0.0.1",
        var port: Int = 9987,
        var nickname: String = "TeamSpeakUser",
        var serverPassword: String = "")
