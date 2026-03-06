package com.autoflow.app.actions

import android.content.Context
import android.media.AudioManager
import android.util.Log

class VolumeAction {

    companion object {
        private const val TAG = "VolumeAction"
    }

    fun execute(context: Context, value: String) {
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
}
