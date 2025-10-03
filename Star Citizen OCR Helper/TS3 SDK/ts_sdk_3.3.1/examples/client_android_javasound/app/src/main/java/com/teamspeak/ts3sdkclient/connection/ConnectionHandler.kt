package com.teamspeak.ts3sdkclient.connection

//events
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.util.Log
import com.teamspeak.soundbackend.CustomSoundbackend
import com.teamspeak.soundbackend.Playback
import com.teamspeak.soundbackend.Record
import com.teamspeak.ts3sdkclient.Constants.*
import com.teamspeak.ts3sdkclient.TS3Application
import com.teamspeak.ts3sdkclient.eventsystem.IEvent
import com.teamspeak.ts3sdkclient.helper.Info
import com.teamspeak.ts3sdkclient.ts3sdk.Native
import com.teamspeak.ts3sdkclient.ts3sdk.events.*
import com.teamspeak.ts3sdkclient.ts3sdk.states.ChannelProperties.CHANNEL_NAME
import com.teamspeak.ts3sdkclient.ts3sdk.states.ClientProperties.CLIENT_NICKNAME
import com.teamspeak.ts3sdkclient.ts3sdk.states.ConnectStatus
import com.teamspeak.ts3sdkclient.ts3sdk.states.PublicError.ERROR_currently_not_possible
import com.teamspeak.ts3sdkclient.ts3sdk.states.PublicError.ERROR_ok
import com.teamspeak.ts3sdkclient.ts3sdk.states.TalkStatus


/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 08.02.17
 *
 * Class that holds all data for one server connection
 */
class ConnectionHandler(private val application: TS3Application) {

    private val serverConnectionHandlerId: Long
    private var mParams: ConnectionParams? = null
    private val mInfo: Info = Info.getInstance()
    private val nativeInterface: Native = application.nativeInstance

    private var captureDeviceOpen: Boolean = false
    private var playbackDeviceOpen: Boolean = false
    private var clientMuted: Boolean = false
    private var captureMuted: Boolean = false
    private var mDeviceId: String = ""
    private var mMode: String = ""

    private var customSoundbackend: CustomSoundbackend? = null

    val connectionStatus: Int
        @ConnectStatus
        get() = nativeInterface.ts3client_getConnectionStatus(serverConnectionHandlerId)

    init {
        serverConnectionHandlerId = nativeInterface.ts3client_spawnNewServerConnectionHandler()
    }

    fun destroy()
    {
        nativeInterface.ts3client_destroyServerConnectionHandler(serverConnectionHandlerId)
    }

    fun startConnection(params: ConnectionParams): Int {
        if (connectionStatus != ConnectStatus.STATUS_DISCONNECTED)
            return ERROR_currently_not_possible

        mInfo.addMessage("startConnection $serverConnectionHandlerId", Color.GREEN)
        mParams = params

        prepareAudio()
        val ip = params.address
        val port = if (params.port != 0) params.port else TS3_DEFAULT_SERVERPORT
        val defaultChannelPassword = ""
        val defaultChannelArray = arrayOf("")

        //Connect to server on the given ip address with the given nickname, no default channel, no default channel password and server password "secret"
        return nativeInterface.ts3client_startConnection(serverConnectionHandlerId, params.identity, ip, port, params.nickname,
                defaultChannelArray, defaultChannelPassword, params.serverPassword)
    }

    /**
     * Stop the actual server connection and destroys the server connection handler in the TeamSpeak library
     */
    fun disconnect() {
        val connectionStatus = nativeInterface.ts3client_getConnectionStatus(serverConnectionHandlerId)
        if (connectionStatus != -1 && connectionStatus != ConnectStatus.STATUS_DISCONNECTED) {
            //Disconnect from server
            nativeInterface.ts3client_stopConnection(serverConnectionHandlerId, "leaving")
        }
    }


    /**
     * Set the speech preprocessor values
     */
    private fun configurePreProcessor() {
        // reapply settings, after capture devices is unmuted
        nativeInterface.ts3client_setPreProcessorConfigValue(serverConnectionHandlerId, PRE_PROCESSOR_VALUE_VAD, "true")
        nativeInterface.ts3client_setPreProcessorConfigValue(serverConnectionHandlerId, PRE_PROCESSOR_VALUE_VAD_MODE, "1")
        nativeInterface.ts3client_setPreProcessorConfigValue(serverConnectionHandlerId, PRE_PROCESSOR_VALUE_VOICE_ACTIVATION_LEVEL_DB, "-30")
        nativeInterface.ts3client_setPreProcessorConfigValue(serverConnectionHandlerId, PRE_PROCESSOR_VALUE_DENOISE, "true")
        nativeInterface.ts3client_setPreProcessorConfigValue(serverConnectionHandlerId, PRE_PROCESSOR_VALUE_AGC, "true")
    }

    /**
     * Sets a pre processor config.
     * e.g. PRE_PROCESSOR_VALUE_VOICE_ACTIVATION_LEVEL_DB, PRE_PROCESSOR_VALUE_DENOISE etc.
     *
     * @param ident the identity of the value to change
     * @param value the actual value
     */
    fun setPreProcessorConfigValue(ident: String, value: String) {
        nativeInterface.ts3client_setPreProcessorConfigValue(serverConnectionHandlerId, ident, value)
    }

    /**
     * Routes the audio to the earpiece speaker and sets the audio mode MODE_IN_COMMUNICATION for voice communication and
     * echo cancellation if supported by the device
     */
    private fun prepareAudio() {
        val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        customSoundbackend = CustomSoundbackend.prepareAudio(nativeInterface, nativeInterface, nativeInterface)
    }

    private fun shutdownAudio() {
        val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    /**
     * registers an audio device if no audio device has been registered before and opens the capture and playback device.
     */
    private fun openAudioDevices() {
        val useCustom = true
        mMode = if (useCustom) "custom" else ""
        mDeviceId = if(useCustom) "Java" else ""

        setCaptureDevice(Record.START)
        setPlaybackDevice(Playback.START)
    }

    /**
     * closes the capture and playback device and unregisters the audio device if a audio device has been registered before.
     * Destroys the native audio Engine, audio player and audio recorder
     */
    private fun closeAudioDevices() {
        setCaptureDevice(Record.SHUTDOWN)
        setPlaybackDevice(Playback.SHUTDOWN)
    }

    /**
     * closes playback device
     */
    private fun setPlaybackDevice(mode: Int) {
        when(mode) {
            Playback.START -> {
                // start playback
                if (!playbackDeviceOpen) {
                    Log.d(TAG, "Opening playback device for server connection handler $serverConnectionHandlerId")
                    playbackDeviceOpen = nativeInterface.ts3client_openPlaybackDevice(
                            serverConnectionHandlerId, mMode, mDeviceId) == ERROR_ok
                    customSoundbackend?.playback?.setPlayback(mode)
                }
            }
            Playback.PAUSE, Playback.SHUTDOWN -> {
                // pause/shutdown playback
                if (playbackDeviceOpen) {
                    Log.d(TAG, "Closing playback device for server connection handler $serverConnectionHandlerId")
                    if (nativeInterface.ts3client_closePlaybackDevice(serverConnectionHandlerId) == ERROR_ok) {
                        playbackDeviceOpen = false
                        customSoundbackend?.playback?.setPlayback(mode)
                    }
                }
            }
        }
    }

    /**
     * Returns if the playback device is open or closed
     * @return true, if the playback device is open, false otherwise
     */
    fun isPlaybackDeviceOpen(): Boolean {
        return playbackDeviceOpen
    }

    /**
     * Closes the capture device if the capture device is open
     */
    private fun setCaptureDevice(mode: Int) {
        when (mode) {
            Record.START -> {
                // start capture
                if (!captureDeviceOpen) {
                    Log.d(TAG, "Opening capture device for server connection handler $serverConnectionHandlerId")
                    captureDeviceOpen = nativeInterface.ts3client_openCaptureDevice(
                            serverConnectionHandlerId, mMode, mDeviceId) == ERROR_ok
                    customSoundbackend?.record?.setRecording(mode)
                }
            }
            Record.PAUSE, Record.SHUTDOWN -> {
                // pause/shutdown capture
                if (captureDeviceOpen) {
                    Log.d(TAG, "Closing capture device for server connection handler $serverConnectionHandlerId")
                    if (nativeInterface.ts3client_closeCaptureDevice(serverConnectionHandlerId) == ERROR_ok) {
                        captureDeviceOpen = false
                        customSoundbackend?.record?.setRecording(mode)
                    }
                }
            }
        }
    }

    /**
     * Returns if the capture device is open or closed
     * @return true, if the capture device is open, false otherwise
     */
    fun isCaptureDeviceOpen(): Boolean {
        return captureDeviceOpen
    }

    /**
     * Mutes the capture device. Setting the record to state PAUSE
     */
    fun muteCaptureDevice() {
        captureMuted = true
        setCaptureDevice(Record.PAUSE)
    }

    /**
     * Unmutes the capture device. Setting the record to state START
     */
    fun unmuteCaptureDevice() {
        captureMuted = false
        setCaptureDevice(Record.START)
    }

    /**
     * Returns if  capture is muted or unmuted
     * @return true, if the capture is muted, false otherwise
     */
    fun isCaptureMuted(): Boolean {
        return captureMuted
    }

    /**
     * This call mutes the playback and capture using client properties
     */
    fun muteClient() {
        nativeInterface.ts3client_setClientSelfVariableAsInt(serverConnectionHandlerId,
                Native.ClientProperties.CLIENT_OUTPUT_MUTED, 1)
        nativeInterface.ts3client_flushClientSelfUpdates(serverConnectionHandlerId, "")
        clientMuted = true
    }

    /**
     * This call unmutes the playback and capture using client properties
     */
    fun unmuteClient() {
        nativeInterface.ts3client_setClientSelfVariableAsInt(serverConnectionHandlerId,
                Native.ClientProperties.CLIENT_OUTPUT_MUTED, 0)
        nativeInterface.ts3client_flushClientSelfUpdates(serverConnectionHandlerId,"")
        clientMuted = false
    }

    /**
     * Returns if the client is muted (capture and playback)
     * @return true, if the client is muted, false otherwise
     */
    fun isClientMuted() : Boolean {
        return clientMuted
    }

    fun getClientId() : Int {
        return nativeInterface.ts3client_getClientID(serverConnectionHandlerId)
    }

    fun printConnectionInfo() {
        val myID = nativeInterface.ts3client_getClientID(serverConnectionHandlerId)
        if (myID == -1)
            return

        val ping = nativeInterface.ts3client_getConnectionVariableAsDouble(serverConnectionHandlerId, myID, Native.ConnectionProperties.CONNECTION_PING)
        val pingDeviation = nativeInterface.ts3client_getConnectionVariableAsDouble(serverConnectionHandlerId, myID, Native.ConnectionProperties.CONNECTION_PING_DEVIATION)
        val totalServerToClientPacketLoss = nativeInterface.ts3client_getConnectionVariableAsDouble(serverConnectionHandlerId, myID, Native.ConnectionProperties.CONNECTION_SERVER2CLIENT_PACKETLOSS_TOTAL)
        var message = "Connection Info -"
        message += " Ping: " + ping.toString()
        message += " +/- " + pingDeviation.toString()
        message += " Total Server To Client Packetloss: " + totalServerToClientPacketLoss.toString()
        mInfo.addMessage(message, Color.parseColor("#4169E1"))
    }

    companion object {
        val TAG: String = ConnectionHandler::class.java.simpleName
    }


    // region Events
    fun onTeamSpeakEvent(event : IEvent) {
        when (event) {
            is ConnectStatusChange -> {
                when (event.newStatus) {
                    ConnectStatus.STATUS_DISCONNECTED -> {
                        Log.d(TAG, "Connect status: DISCONNECTED")
                        closeAudioDevices()
                        customSoundbackend?.unregister()
                        customSoundbackend = null
                        shutdownAudio()
                        //val errorCode = event.errorNumber;
                    }
                    ConnectStatus.STATUS_CONNECTING -> {
                        Log.d(TAG, "Connect status: CONNECTING")
                    }
                    ConnectStatus.STATUS_CONNECTED -> {
                        Log.d(TAG, "Connect status: CONNECTED")
                        openAudioDevices()
                        configurePreProcessor()
                    }
                    ConnectStatus.STATUS_CONNECTION_ESTABLISHING -> {
                        Log.d(TAG, "Connect status: CONNECTION_ESTABLISHING")
                    }
                    ConnectStatus.STATUS_CONNECTION_ESTABLISHED -> {
                        Log.d(TAG, "Connect status: CONNECTION_ESTABLISHED")
                    }
                }
            }
            is NewChannel -> {
                /* Query channel name from channel ID */
                val channelName = nativeInterface.ts3client_getChannelVariableAsString(serverConnectionHandlerId, event.channelID, CHANNEL_NAME)
                Log.d(TAG, "New channel: $channelName")
            }
            is NewChannelCreated -> {
                /* Query channel name from channel ID */
                val channelName = nativeInterface.ts3client_getChannelVariableAsString(serverConnectionHandlerId, event.channelID, CHANNEL_NAME)
                Log.d(TAG, "New channel created: $channelName")
            }
            is DelChannel -> {
                val (_, channelID, invokerID, invokerName) = event
                Log.d(TAG, "Channel ID $channelID deleted by $invokerName ($invokerID)")
            }
            is ClientMove -> {
                Log.d(TAG, "ClientID " + event.clientID + " moves from channel " + event.oldChannelID +
                        " to " + event.newChannelID + " with message " + event.moveMessage)
            }
            is ClientMoveSubscription -> {
                val (serverConnectionHandlerID, clientID) = event

                /* Query channel name from channel ID */
                val clientName = nativeInterface.ts3client_getClientVariableAsString(
                        serverConnectionHandlerID, clientID, CLIENT_NICKNAME)

                Log.d(TAG, "ClientMoveSubscription Event, ClientName: $clientName")
            }
            is ClientMoveTimeout -> {
                val (_, clientID, _, _, _, timeoutMessage) = event
                Log.d(TAG, "ClientID $clientID timeouts with message $timeoutMessage")
            }
            is TalkStatusChange -> {
                val (serverConnectionHandlerID, status, _, clientID) = event

                /* Query client nickname from ID */
                val clientName = nativeInterface.ts3client_getClientVariableAsString(
                        serverConnectionHandlerID, clientID, CLIENT_NICKNAME)
                if (status == TalkStatus.STATUS_TALKING) {
                    Log.d(TAG, "Client \"$clientName\" starts talking")
                } else {
                    Log.d(TAG, "Client \"$clientName\" stops talking")
                }
            }
            is ServerError -> {
                val (serverConnectionHandlerID, errorMessage, _, _, extraMessage) = event
                Log.d(TAG, "Error for server " + serverConnectionHandlerID + ": "
                        + errorMessage + " " + extraMessage)
            }
        }
    }
    // endregion
}
