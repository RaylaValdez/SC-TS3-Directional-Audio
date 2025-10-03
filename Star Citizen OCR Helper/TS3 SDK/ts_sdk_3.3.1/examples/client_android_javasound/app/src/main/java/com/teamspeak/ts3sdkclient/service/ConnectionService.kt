package com.teamspeak.ts3sdkclient.service

//Events


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import com.teamspeak.ts3sdkclient.R
import com.teamspeak.ts3sdkclient.TS3Application
import com.teamspeak.ts3sdkclient.connection.ConnectionHandler
import com.teamspeak.ts3sdkclient.eventsystem.Callbacks
import com.teamspeak.ts3sdkclient.eventsystem.IEvent
import com.teamspeak.ts3sdkclient.eventsystem.IEventListener
import com.teamspeak.ts3sdkclient.helper.Info
import com.teamspeak.ts3sdkclient.ts3sdk.events.ConnectStatusChange
import com.teamspeak.ts3sdkclient.ts3sdk.states.ConnectStatus

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 08.02.17
 *
 * Service class to ensure the server connection can be maintained if the app is in the background
 * The Service will run in the foreground while a server connection is active
 *
 * mConnectionHandler       - ConnectionHandler variable with the current connection information
 * mNotificationManager     - NotificationManager to display connection status notifications
 */
class ConnectionService : Service(), IEventListener {

    var connectionHandler: ConnectionHandler? = null
        private set

    private var mInfo: Info? = null

    private var mNotificationManager: NotificationManager? = null

    private val mBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: ConnectionService
            get() = this@ConnectionService
    }

    override fun onCreate() {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val app = application as TS3Application
        connectionHandler = ConnectionHandler(app)
        mInfo = Info.getInstance()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "Received start id $startId: $intent")
        Callbacks.getInstance().registerCallbacks(this)
        mInfo!!.addMessage(this.javaClass.simpleName + ": onStartCommand", Color.parseColor("#228B22"))
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        mInfo!!.addMessage(this.javaClass.simpleName + ": onBind", Color.parseColor("#FF8C00"))
        return mBinder
    }

    override fun onRebind(intent: Intent) {
        mInfo!!.addMessage(this.javaClass.simpleName + ": onRebind", Color.parseColor("#FF8C00"))
    }

    override fun onUnbind(intent: Intent): Boolean {
        mInfo!!.addMessage(this.javaClass.simpleName + ": onUnbind", Color.parseColor("#FF8C00"))
        return true
    }

    override fun onDestroy() {
        Callbacks.getInstance().unregisterCallbacks(this)
        //connectionHandler?.destroy()
        mInfo!!.addMessage(this.javaClass.simpleName + ": onDestroy", Color.parseColor("#DC143C"))
    }

    override fun onTS3Event(event: IEvent) {
        Log.d(TAG, event.toString())
        mInfo!!.addMessage(event.toString(), Color.parseColor("#4169E1"))
        // TODO Filter global events
        connectionHandler!!.onTeamSpeakEvent(event)

        if (event is ConnectStatusChange) {
            val (_, connectStatus) = event
            when (connectStatus) {
                ConnectStatus.STATUS_DISCONNECTED -> {
                    stopForeground(true)
                    stopSelf()
                }
                ConnectStatus.STATUS_CONNECTING -> startForeground(SERVICE_FOREGROUND_NOTIFICATION_ID,
                        createNotification(getString(R.string.button_server_connecting)))
                ConnectStatus.STATUS_CONNECTED -> mNotificationManager!!.notify(SERVICE_FOREGROUND_NOTIFICATION_ID,
                        createNotification(getString(R.string.server_connected)))
                ConnectStatus.STATUS_CONNECTION_ESTABLISHING, ConnectStatus.STATUS_CONNECTION_ESTABLISHED -> {
                }
                else -> {
                }
            }
            return
        }
    }

    /**
     * Create a foreground notification to inform the user about current
     * connection state
     * @param text - the text you want to display
     * @return the notification
     */
    private fun createNotification(text: String): Notification {
        // For Android 8.1+ a notification channel must be created
        val channelId: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = NOTIFICATION_CHANNEL_ID
            val channelName = getString(R.string.app_name)
            val chan = NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(chan)
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId = ""
        }

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setTicker(text)
                .setOngoing(true)
        return builder.build()
    }

    companion object {

        val TAG = ConnectionService::class.java.simpleName
        const val SERVICE_FOREGROUND_NOTIFICATION_ID = 8648
        const val NOTIFICATION_CHANNEL_ID = "ts3_notification_channel"
    }
}
