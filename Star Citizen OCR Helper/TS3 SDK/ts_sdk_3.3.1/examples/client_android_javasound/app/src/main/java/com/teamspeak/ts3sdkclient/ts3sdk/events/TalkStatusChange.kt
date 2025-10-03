package com.teamspeak.ts3sdkclient.ts3sdk.events

import com.teamspeak.ts3sdkclient.eventsystem.TsEvent
import com.teamspeak.ts3sdkclient.ts3sdk.states.TalkStatus

/**
 * This event is called when a client starts or stops talking.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   status                    - 1 if client starts talking, 0 if client stops talking
 *   isReceivedWhisper         - 1 if this event was caused by whispering, 0 if caused by normal talking
 *   clientID                  - ID of the client who announced the talk status change
 */
data class TalkStatusChange(
        val serverConnectionHandlerID: Long = 0,
        val status: Int = 0,
        val isReceivedWhisper: Int = 0,
        val clientID: Int = 0)
    : TsEvent() {

    override fun toString(): String {
        var status = ""

        when (this.status) {
            TalkStatus.STATUS_NOT_TALKING -> status = "STATUS_NOT_TALKING"
            TalkStatus.STATUS_TALKING -> status = "STATUS_TALKING"
            TalkStatus.STATUS_TALKING_WHILE_DISABLED -> status = "STATUS_TALKING_WHILE_DISABLED"
        }
        return "TalkStatusChange [serverConnectionHandlerID=$serverConnectionHandlerID, status=$status, isReceivedWhisper=$isReceivedWhisper, clientID=$clientID]"
    }
}
