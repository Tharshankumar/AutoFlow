package com.autoflow.app.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.autoflow.app.R
import com.autoflow.app.core.DelayAction
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

    suspend fun executeAction(action: Action) {
        try {
            when (action.type) {
                Action.TYPE_SET_VOLUME -> setVolume(action.value)
                Action.TYPE_TOGGLE_WIFI -> toggleWifi(action.value)
                Action.TYPE_TOGGLE_BLUETOOTH -> toggleBluetooth(action.value)
                Action.TYPE_OPEN_APP -> openApp(action.value)
                Action.TYPE_SHOW_NOTIFICATION -> showNotification(action.value)

                // WiFi actions
                Action.TYPE_ENABLE_WIFI,
                Action.TYPE_DISABLE_WIFI,
                Action.TYPE_CONNECT_WIFI,
                Action.TYPE_DISCONNECT_WIFI -> handleWifiAction(action.type, action.value)

                // Bluetooth actions
                Action.TYPE_ENABLE_BLUETOOTH,
                Action.TYPE_DISABLE_BLUETOOTH -> handleBluetoothAction(action.type)

                // Battery saver actions
                Action.TYPE_ENABLE_BATTERY_SAVER,
                Action.TYPE_DISABLE_BATTERY_SAVER -> handleBatterySaverAction(action.type)

                // Music actions
                Action.TYPE_PLAY_MUSIC -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                Action.TYPE_PAUSE_MUSIC -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
                Action.TYPE_NEXT_TRACK -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                Action.TYPE_PREVIOUS_TRACK -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                Action.TYPE_PLAY_PLAYLIST -> {
                    sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                    Log.d(TAG, "Play playlist: ${action.value}")
                }

                // Phone actions
                Action.TYPE_SEND_SMS -> sendSms(action.value)
                Action.TYPE_AUTO_REPLY -> Log.d(TAG, "Auto-reply configured: ${action.value}")
                Action.TYPE_REJECT_CALL -> Log.d(TAG, "Reject call action")

                // NFC actions
                Action.TYPE_EXECUTE_ROUTINE -> Log.d(TAG, "Execute routine: ${action.value}")

                // Camera actions
                Action.TYPE_OPEN_CAMERA -> openCamera()
                Action.TYPE_TAKE_PHOTO -> takePhoto()
                Action.TYPE_RECORD_VIDEO -> recordVideo()

                // Internet actions
                Action.TYPE_ENABLE_MOBILE_DATA,
                Action.TYPE_DISABLE_MOBILE_DATA -> handleMobileDataAction(action.type)
                Action.TYPE_ENABLE_AIRPLANE_MODE,
                Action.TYPE_DISABLE_AIRPLANE_MODE -> handleAirplaneModeAction(action.type)

                // Delay actions
                Action.TYPE_WAIT_SECONDS -> DelayAction.executeDelay("seconds:${action.value}")
                Action.TYPE_WAIT_MINUTES -> DelayAction.executeDelay("minutes:${action.value}")

                else -> Log.w(TAG, "Unknown action type: ${action.type}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action: ${action.type}", e)
        }
    }

    private fun setVolume(value: String) {
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

    private fun toggleWifi(value: String) {
        val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "WiFi settings panel opened (toggle: $value)")
    }

    @Suppress("MissingPermission")
    private fun toggleBluetooth(value: String) {
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
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }
        Log.d(TAG, "Bluetooth toggle: $value")
    }

    private fun openApp(value: String) {
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

    private fun handleWifiAction(type: String, value: String) {
        when (type) {
            Action.TYPE_CONNECT_WIFI -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            else -> {
                val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
        Log.d(TAG, "WiFi action: $type ($value)")
    }

    @Suppress("MissingPermission")
    private fun handleBluetoothAction(type: String) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter

        when (type) {
            Action.TYPE_ENABLE_BLUETOOTH -> {
                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            Action.TYPE_DISABLE_BLUETOOTH -> {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
        Log.d(TAG, "Bluetooth action: $type")
    }

    private fun handleBatterySaverAction(type: String) {
        val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Battery saver action: $type")
    }

    private fun sendMediaKey(keyCode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val eventTime = SystemClock.uptimeMillis()
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
        Log.d(TAG, "Media key sent: $keyCode")
    }

    private fun sendSms(value: String) {
        val parts = value.split("|", limit = 2)
        val phoneNumber = parts.getOrElse(0) { "" }
        val message = parts.getOrElse(1) { "" }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "SMS intent opened for: $phoneNumber")
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Camera opened")
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Photo capture opened")
    }

    private fun recordVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Video capture opened")
    }

    private fun handleMobileDataAction(type: String) {
        val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Mobile data action: $type")
    }

    private fun handleAirplaneModeAction(type: String) {
        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "Airplane mode action: $type")
    }
}
