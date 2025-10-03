package com.teamspeak.ts3sdkclient.ts3sdk

import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import android.content.Context
import java.nio.ByteBuffer

/**
 * Copyright (c) 2007-2018 TeamSpeak-Systems
 *
 * This class loads the ts3sdk library and the generated ts3client-wrapper-lib.
 * From here you can call the library functions defined in ts3client_wrapper.h
 */

class Native(applicationContext: Context) : com.teamspeak.soundbackend.ICustomSoundbackend, com.teamspeak.soundbackend.IPlayback, com.teamspeak.soundbackend.IRecord{

    var isInitialized = false
        private set

    init {
        val result = ts3client_startInit(applicationContext)
        if (result == 0) {
            /* Query and print client lib version */
            Log.d(TAG, "SDK Library version: " + ts3client_getClientLibVersion())
        } else {
            Log.e(TAG, "Couldn't initialize client lib.")
        }
        isInitialized = result == 0
    }

    fun Dispose() {
        if (isInitialized) {
            try {
                ts3client_destroyClientLib()
                Log.d(TAG, "ClientLib destroyed")
            } catch (e: UnsatisfiedLinkError) {
                // Lib may be gone already on shutdown
            }

            isInitialized = false
        }
    }

    private external fun ts3client_startInit(applicationContext: Context): Int
    private external fun ts3client_destroyClientLib(): Int

    external fun ts3client_getClientID(connectionID: Long): Int
    external fun ts3client_getConnectionStatus(connectionID: Long): Int

    external fun ts3client_spawnNewServerConnectionHandler(): Long
    external fun ts3client_destroyServerConnectionHandler(connectionID: Long): Int

    external fun ts3client_startConnection(connectionID: Long, identity: String, ip: String, port: Int, nickname: String, channel: Array<String>, defaultChannelPassword: String, serverPassword: String): Int
    external fun ts3client_stopConnection(connectionID: Long, message: String): Int

    external fun ts3client_createIdentity(): String
    external fun ts3client_getClientLibVersion(): String

    //region custom device
    external fun ts3client_registerCustomDevice(deviceID: String, deviceDisplayName: String, capFrequency: Int, capChannels: Int, capByteBuffer: ByteBuffer, playFrequency: Int, playChannels: Int, playByteBuffer: ByteBuffer): Int
    override fun registerCustomDevice(deviceID: String, deviceDisplayName: String, capFrequency: Int, capChannels: Int, capByteBuffer: ByteBuffer, playFrequency: Int, playChannels: Int, playByteBuffer: ByteBuffer): Int {
        return ts3client_registerCustomDevice(deviceID, deviceDisplayName, capFrequency, capChannels, capByteBuffer, playFrequency, playChannels, playByteBuffer)
    }
    external fun ts3client_unregisterCustomDevice(deviceID: String): Int
    override fun unregisterCustomDevice(deviceID: String): Int {
        return ts3client_unregisterCustomDevice(deviceID)
    }
    external fun ts3client_acquireCustomPlaybackData(deviceID: String, samples: Int): Int
    override fun acquireCustomPlaybackData(deviceID: String, samples: Int): Int {
        return ts3client_acquireCustomPlaybackData(deviceID, samples)
    }
    external fun ts3client_processCustomCaptureData(deviceID: String, samples: Int): Int
    override fun processCustomCaptureData(deviceID: String, samples: Int): Int {
        return ts3client_processCustomCaptureData(deviceID, samples)
    }
    //endregion

    external fun ts3client_openCaptureDevice(connectionID: Long, modeID: String, captureDevice: String): Int
    external fun ts3client_openPlaybackDevice(connectionID: Long, modeID: String, captureDevice: String): Int
    external fun ts3client_closeCaptureDevice(connectionID: Long): Int
    external fun ts3client_closePlaybackDevice(connectionID: Long): Int

    external fun ts3client_activateCaptureDevice(connectionID: Long): Int

    external fun ts3client_setClientSelfVariableAsInt(connectionID: Long, flag: Int, value: Int): Int
    fun ts3client_setClientSelfVariableAsInt(connectionID: Long, flag: Native.ClientProperties, value: Int): Int {
        return ts3client_setClientSelfVariableAsInt(connectionID, flag.connectionProperties, value)
    }

    external fun ts3client_flushClientSelfUpdates(connectionID: Long, returnCode: String?): Int

    external fun ts3client_setPreProcessorConfigValue(connectionID: Long, ident: String, value: String): Int
    external fun ts3client_getPreProcessorConfigValue(connectionID: Long, ident: String): String

    external fun ts3client_getPlaybackConfigValueAsFloat(connectionID: Long, ident: String): Float
    external fun ts3client_setPlaybackConfigValue(connectionID: Long, ident: String, value: String): Int

    external fun ts3client_getClientVariableAsString(connectionID: Long, clientID: Int, flag: Int): String

    external fun ts3client_getChannelVariableAsString(connectionID: Long, channelID: Long, flag: Int): String

    enum class ConnectionProperties private constructor(val connectionProperties: Int) {
        CONNECTION_PING(0), //average latency for a round trip through and back this connection
        CONNECTION_PING_DEVIATION(1), //standard deviation of the above average latency
        CONNECTION_CONNECTED_TIME(2), //how long the connection exists already
        CONNECTION_IDLE_TIME(3), //how long since the last action of this client
        CONNECTION_CLIENT_IP(4), //IP of this client (as seen from the server side)
        CONNECTION_CLIENT_PORT(5), //Port of this client (as seen from the server side)
        CONNECTION_SERVER_IP(6), //IP of the server (seen from the client side) - only available on yourself, not for remote clients, not available server side
        CONNECTION_SERVER_PORT(7), //Port of the server (seen from the client side) - only available on yourself, not for remote clients, not available server side
        CONNECTION_PACKETS_SENT_SPEECH(8), //how many Speech packets were sent through this connection
        CONNECTION_PACKETS_SENT_KEEPALIVE(9), CONNECTION_PACKETS_SENT_CONTROL(10), CONNECTION_PACKETS_SENT_TOTAL(11), //how many packets were sent totally (this is PACKETS_SENT_SPEECH + PACKETS_SENT_KEEPALIVE + PACKETS_SENT_CONTROL)
        CONNECTION_BYTES_SENT_SPEECH(12), CONNECTION_BYTES_SENT_KEEPALIVE(13), CONNECTION_BYTES_SENT_CONTROL(14), CONNECTION_BYTES_SENT_TOTAL(15), CONNECTION_PACKETS_RECEIVED_SPEECH(16), CONNECTION_PACKETS_RECEIVED_KEEPALIVE(17), CONNECTION_PACKETS_RECEIVED_CONTROL(18), CONNECTION_PACKETS_RECEIVED_TOTAL(19), CONNECTION_BYTES_RECEIVED_SPEECH(20), CONNECTION_BYTES_RECEIVED_KEEPALIVE(21), CONNECTION_BYTES_RECEIVED_CONTROL(22), CONNECTION_BYTES_RECEIVED_TOTAL(23), CONNECTION_PACKETLOSS_SPEECH(24), CONNECTION_PACKETLOSS_KEEPALIVE(25), CONNECTION_PACKETLOSS_CONTROL(26), CONNECTION_PACKETLOSS_TOTAL(27), //the probability with which a packet round trip failed because a packet was lost
        CONNECTION_SERVER2CLIENT_PACKETLOSS_SPEECH(28), //the probability with which a speech packet failed from the server to the client
        CONNECTION_SERVER2CLIENT_PACKETLOSS_KEEPALIVE(29), CONNECTION_SERVER2CLIENT_PACKETLOSS_CONTROL(30), CONNECTION_SERVER2CLIENT_PACKETLOSS_TOTAL(31), CONNECTION_CLIENT2SERVER_PACKETLOSS_SPEECH(32), CONNECTION_CLIENT2SERVER_PACKETLOSS_KEEPALIVE(33), CONNECTION_CLIENT2SERVER_PACKETLOSS_CONTROL(34), CONNECTION_CLIENT2SERVER_PACKETLOSS_TOTAL(35), CONNECTION_BANDWIDTH_SENT_LAST_SECOND_SPEECH(36), //howmany bytes of speech packets we sent during the last second
        CONNECTION_BANDWIDTH_SENT_LAST_SECOND_KEEPALIVE(37), CONNECTION_BANDWIDTH_SENT_LAST_SECOND_CONTROL(38), CONNECTION_BANDWIDTH_SENT_LAST_SECOND_TOTAL(39), CONNECTION_BANDWIDTH_SENT_LAST_MINUTE_SPEECH(40), //howmany bytes/s of speech packets we sent in average during the last minute
        CONNECTION_BANDWIDTH_SENT_LAST_MINUTE_KEEPALIVE(41), CONNECTION_BANDWIDTH_SENT_LAST_MINUTE_CONTROL(42), CONNECTION_BANDWIDTH_SENT_LAST_MINUTE_TOTAL(43), CONNECTION_BANDWIDTH_RECEIVED_LAST_SECOND_SPEECH(44), CONNECTION_BANDWIDTH_RECEIVED_LAST_SECOND_KEEPALIVE(45), CONNECTION_BANDWIDTH_RECEIVED_LAST_SECOND_CONTROL(46), CONNECTION_BANDWIDTH_RECEIVED_LAST_SECOND_TOTAL(47), CONNECTION_BANDWIDTH_RECEIVED_LAST_MINUTE_SPEECH(48), CONNECTION_BANDWIDTH_RECEIVED_LAST_MINUTE_KEEPALIVE(49), CONNECTION_BANDWIDTH_RECEIVED_LAST_MINUTE_CONTROL(50), CONNECTION_BANDWIDTH_RECEIVED_LAST_MINUTE_TOTAL(51), CONNECTION_ENDMARKER(52)
    }

    enum class ClientProperties private constructor(val connectionProperties: Int) {
        CLIENT_UNIQUE_IDENTIFIER(0), //automatically up-to-date for any client "in view", can be used to identify this particular client installation
        CLIENT_NICKNAME(1), //automatically up-to-date for any client "in view"
        CLIENT_VERSION(2), //for other clients than ourself, this needs to be requested (=> requestClientVariables)
        CLIENT_PLATFORM(3), //for other clients than ourself, this needs to be requested (=> requestClientVariables)
        CLIENT_FLAG_TALKING(4), //automatically up-to-date for any client that can be heard (in room / whisper)
        CLIENT_INPUT_MUTED(5), //automatically up-to-date for any client "in view", this clients microphone mute status
        CLIENT_OUTPUT_MUTED(6), //automatically up-to-date for any client "in view", this clients headphones/speakers/mic combined mute status
        CLIENT_OUTPUTONLY_MUTED(7), //automatically up-to-date for any client "in view", this clients headphones/speakers only mute status
        CLIENT_INPUT_HARDWARE(8), //automatically up-to-date for any client "in view", this clients microphone hardware status (is the capture device opened?)
        CLIENT_OUTPUT_HARDWARE(9), //automatically up-to-date for any client "in view", this clients headphone/speakers hardware status (is the playback device opened?)
        CLIENT_INPUT_DEACTIVATED(10), //only usable for ourself, not propagated to the network
        CLIENT_IDLE_TIME(11), //internal use
        CLIENT_DEFAULT_CHANNEL(12), //only usable for ourself, the default channel we used to connect on our last connection attempt
        CLIENT_DEFAULT_CHANNEL_PASSWORD(13), //internal use
        CLIENT_SERVER_PASSWORD(14), //internal use
        CLIENT_META_DATA(15), //automatically up-to-date for any client "in view", not used by TeamSpeak, free storage for sdk users
        CLIENT_IS_MUTED(16), //only make sense on the client side locally, "1" if this client is currently muted by us, "0" if he is not
        CLIENT_IS_RECORDING(17), //automatically up-to-date for any client "in view"
        CLIENT_VOLUME_MODIFICATOR(18), //internal use
        CLIENT_VERSION_SIGN(19), //sign
        CLIENT_SECURITY_HASH(20), //SDK use, not used by teamspeak. Hash is provided by an outside source. A channel will use the security salt + other client data to calculate a hash, which must be the same as the one provided here.
        CLIENT_ENDMARKER(21)
    }

    fun ts3client_getConnectionVariableAsDouble(connectionID: Long, clientID: Int, flag: ConnectionProperties): Double {
        return ts3client_getConnectionVariableAsDouble(connectionID, clientID, flag.connectionProperties)
    }
    external fun ts3client_getConnectionVariableAsDouble(connectionID: Long, clientID: Int, flag: Int): Double

    companion object {

        private val TAG = Native::class.java.simpleName

        /**
         * The native libraries needed to be loaded before they can be used
         */
        fun loadLibrary() {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                System.loadLibrary("c++_shared");
            }
            System.loadLibrary("ts3client")
            System.loadLibrary("ts3client-wrapper-lib")
        }
    }
}
