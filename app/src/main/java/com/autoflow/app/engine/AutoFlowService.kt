package com.autoflow.app.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.autoflow.app.MainActivity
import com.autoflow.app.R

class AutoFlowService : Service() {

    companion object {
        private const val TAG = "AutoFlowService"
        private const val CHANNEL_ID = "autoflow_service"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var triggerManager: TriggerManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AutoFlowService created")
        triggerManager = TriggerManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AutoFlowService started")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        triggerManager.startAllListeners()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AutoFlowService destroyed")
        triggerManager.stopAllListeners()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoFlow")
            .setContentText("Automation engine is running")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
