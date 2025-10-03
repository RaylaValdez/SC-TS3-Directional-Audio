package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent

/**
 * This event is called when a the sever sends an error message to the client.
 *
 * Parameters:
 *   serverConnectionHandlerID - The connection handler ID of the server who sent the error event
 *   errorMessage              - String containing a verbose error message, encoded in UTF-8 format
 *   error                     - Error code as defined in public_errors.h
 *   returnCode                - String containing the return code if it has been set by the Client Lib function call which caused this error event. See return code documentation in the sdk description document
 *   extraMessage              - Can contain additional information about the occured error. If no additional information is available, this parameter is an empty string
 */
data class ServerError(
        val serverConnectionHandlerID: Long = 0,
        val errorMessage: String = "",
        val error: Int = 0,
        val returnCode: String? = null,
        val extraMessage: String? = null)
    : TsEvent()
