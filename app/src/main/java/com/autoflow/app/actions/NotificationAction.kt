package com.autoflow.app.actions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationAction {

    companion object {
        private const val TAG = "NotificationAction"
        private const val CHANNEL_ID = "autoflow_routine_notifications"
        private var notificationId = 2000
    }

    fun execute(context: Context, value: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Routine Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications from AutoFlow routines"
        }
        notificationManager.createNotificationChannel(channel)

        val parts = value.split("|", limit = 2)
        val title = parts.getOrElse(0) { "AutoFlow" }
        val message = parts.getOrElse(1) { value }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId++, notification)
        Log.d(TAG, "Notification shown: $title - $message")
    }
}
