package com.androidaiagent.screen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat

/**
 * Foreground host for a user-approved MediaProjection session.
 * The actual projection token must come from ActivityResultContracts; this service never
 * attempts to capture without consent and does not claim that capture is active by default.
 */
class ScreenCaptureService : Service() {
    override fun onCreate() { super.onCreate(); createChannel() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Android AI Agent")
            .setContentText("Screen capture is active by user approval")
            .setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION) else startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, "Agent screen capture", NotificationManager.IMPORTANCE_LOW))
    }
    companion object { private const val CHANNEL = "agent_screen_capture"; private const val NOTIFICATION_ID = 1001 }
}
