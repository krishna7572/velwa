package com.velwa.app.data.bluetooth

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.velwa.app.R
import com.velwa.app.ui.screens.MainActivity

class BluetoothService : Service() {

    private val CHANNEL_ID = "velwa_bt_channel"
    private val NOTIF_ID = 101

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_AUTO_CONNECT -> handleAutoConnect()
            ACTION_STOP -> stopSelf()
        }
        startForeground(NOTIF_ID, buildNotification("Velwa chal raha hai..."))
        return START_STICKY
    }

    private fun handleAutoConnect() {
        // Auto-connect logic runs here on boot
        updateNotification("Auto-connect kar raha hai...")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Velwa Bluetooth",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Velwa Bluetooth background service"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Velwa")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_bluetooth)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, buildNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_AUTO_CONNECT = "com.velwa.AUTO_CONNECT"
        const val ACTION_STOP = "com.velwa.STOP"
    }
}
