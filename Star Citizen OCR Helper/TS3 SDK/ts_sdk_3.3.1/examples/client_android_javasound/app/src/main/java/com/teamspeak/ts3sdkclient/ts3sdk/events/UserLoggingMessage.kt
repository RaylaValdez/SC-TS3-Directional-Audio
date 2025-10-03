package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

/**
 * Called if user-defined logging was enabled when initialzing the Client Lib. Allows user to customize logging and handling of critical errors:
 *
 * Parameters:
 *   logMessage                - Actual log message text
 *   logLevel                  - Severity of log message, defined by the enum LogLevel. Note that only log messages of a level higher than the one config- ured with ts3client_setLogVerbosity will appear
 *   logChannel                - Optional custom text to categorize the message channel
 *   logID                     - Server connection handler ID identifying the current server connection when using multiple connections
 *   logTime                   - String with date and time when the log message occured
 *   completeLogString         - Provides a verbose log message including all previous parameters for convinience
 */
data class UserLoggingMessage(
        val logMessage: String = "",
        val logLevel: Int = 0,
        val logChannel: String = "",
        val logID: Long = 0,
        val logTime: String = "",
        private val completeLogString: String = "")
    : TsEvent() {

    override fun toString(): String {
        return completeLogString
    }
}
