package com.autoflow.app.plugins.music

import android.content.Context
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class MusicPlugin : AutomationPlugin {

    companion object {
        private const val TAG = "MusicPlugin"
    }

    override val pluginId = "music"
    override val pluginName = "Music"

    private var context: Context? = null

    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        Log.d(TAG, "Music plugin initialized")
    }

    override fun registerTriggers(): List<TriggerDefinition> = emptyList()

    override fun registerConditions(): List<ConditionDefinition> = emptyList()

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "play_music",
            displayName = "Play Music",
            description = "Play/resume music playback",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "pause_music",
            displayName = "Pause Music",
            description = "Pause music playback",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "next_track",
            displayName = "Next Track",
            description = "Skip to next track"
        ),
        ActionDefinition(
            type = "previous_track",
            displayName = "Previous Track",
            description = "Go to previous track"
        ),
        ActionDefinition(
            type = "play_playlist",
            displayName = "Play Playlist",
            description = "Play a specific playlist",
            valueHint = "playlist name or URI"
        )
    )

    fun executeAction(context: Context, actionType: String, value: String) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (actionType) {
            "play_music" -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_PLAY)
                Log.d(TAG, "Music play command sent")
            }
            "pause_music" -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_PAUSE)
                Log.d(TAG, "Music pause command sent")
            }
            "next_track" -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_NEXT)
                Log.d(TAG, "Next track command sent")
            }
            "previous_track" -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                Log.d(TAG, "Previous track command sent")
            }
            "play_playlist" -> {
                // Try to launch music app with the playlist
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_PLAY)
                Log.d(TAG, "Play playlist command sent: $value")
            }
        }
    }

    private fun sendMediaKeyEvent(audioManager: AudioManager, keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    override fun shutdown() {
        Log.d(TAG, "Music plugin shut down")
    }
}
