package com.autoflow.app.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.autoflow.app.R
import com.autoflow.app.data.database.RoutineDao
import com.autoflow.app.data.database.entities.Action

class ActionExecutor(
    private val context: Context,
    private val routineDao: RoutineDao
) {
    companion object {
        private const val TAG = "ActionExecutor"
        private const val EXECUTION_CHANNEL_ID = "autoflow_execution"
        private var notificationId = 1000
    }

    suspend fun execute(routineId: Long) {
        val actions = routineDao.getActionsForRoutine(routineId)
        actions.forEach { action ->
            executeAction(action)
        }
    }

    fun executeAction(action: Action) {
        try {
            when (action.type) {
                Action.TYPE_SET_VOLUME -> setVolume(action.value)
                Action.TYPE_TOGGLE_WIFI -> toggleWifi(action.value)
                Action.TYPE_TOGGLE_BLUETOOTH -> toggleBluetooth(action.value)
                Action.TYPE_OPEN_APP -> openApp(action.value)
                Action.TYPE_SHOW_NOTIFICATION -> showNotification(action.value)
                else -> Log.w(TAG, "Unknown action type: ${action.type}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action: ${action.type}", e)
        }
    }

    private fun setVolume(value: String) {
        // value format: "streamType:level" (e.g., "music:7" or just "7" for media)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        val parts = value.split(":")
        val streamType: Int
        val level: Int

        if (parts.size == 2) {
            streamType = when (parts[0].lowercase()) {
                "music", "media" -> AudioManager.STREAM_MUSIC
                "ring", "ringtone" -> AudioManager.STREAM_RING
                "alarm" -> AudioManager.STREAM_ALARM
                "notification" -> AudioManager.STREAM_NOTIFICATION
                else -> AudioManager.STREAM_MUSIC
            }
            level = parts[1].toIntOrNull() ?: return
        } else {
            streamType = AudioManager.STREAM_MUSIC
            level = value.toIntOrNull() ?: return
        }

        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val adjustedLevel = level.coerceIn(0, maxVolume)
        audioManager.setStreamVolume(streamType, adjustedLevel, 0)
        Log.d(TAG, "Volume set to $adjustedLevel for stream $streamType")
    }

    @Suppress("DEPRECATION")
    private fun toggleWifi(value: String) {
        // value: "on" or "off"
        // Note: Direct WiFi toggle is restricted on Android 10+
        // Users need to use Settings panel instead
        val intent = Intent(android.provider.Settings.Panel.ACTION_WIFI).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "WiFi settings panel opened (toggle: $value)")
    }

    @Suppress("MissingPermission")
    private fun toggleBluetooth(value: String) {
        // value: "on" or "off"
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter ?: return

        when (value.lowercase()) {
            "on" -> {
                if (!bluetoothAdapter.isEnabled) {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            "off" -> {
                if (bluetoothAdapter.isEnabled) {
                    // On Android 13+, apps cannot programmatically disable Bluetooth
                    // Open settings panel instead
                    val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }
        Log.d(TAG, "Bluetooth toggle: $value")
    }

    private fun openApp(value: String) {
        // value: package name (e.g., "com.spotify.music")
        val launchIntent = context.packageManager.getLaunchIntentForPackage(value)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Log.d(TAG, "Opened app: $value")
        } else {
            Log.w(TAG, "App not found: $value")
        }
    }

    private fun showNotification(value: String) {
        // value format: "title|message" (e.g., "Battery Low|Battery is below 20%")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        val channel = NotificationChannel(
            EXECUTION_CHANNEL_ID,
            context.getString(R.string.execution_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.execution_channel_description)
        }
        notificationManager.createNotificationChannel(channel)

        val parts = value.split("|", limit = 2)
        val title = parts.getOrElse(0) { "AutoFlow" }
        val message = parts.getOrElse(1) { value }

        val notification = NotificationCompat.Builder(context, EXECUTION_CHANNEL_ID)
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
