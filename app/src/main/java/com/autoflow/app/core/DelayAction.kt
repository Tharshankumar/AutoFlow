package com.autoflow.app.core

import android.util.Log
import kotlinx.coroutines.delay

/**
 * Supports delay actions in automation routines.
 * wait_seconds and wait_minutes allow inserting pauses between actions.
 */
object DelayAction {

    private const val TAG = "DelayAction"

    /**
     * Wait for specified seconds.
     */
    suspend fun waitSeconds(seconds: Int) {
        Log.d(TAG, "Waiting for $seconds seconds")
        delay(seconds * 1000L)
        Log.d(TAG, "Wait complete: $seconds seconds")
    }

    /**
     * Wait for specified minutes.
     */
    suspend fun waitMinutes(minutes: Int) {
        Log.d(TAG, "Waiting for $minutes minutes")
        delay(minutes * 60 * 1000L)
        Log.d(TAG, "Wait complete: $minutes minutes")
    }

    /**
     * Parse a delay value string and execute the delay.
     * Format: "seconds:N" or "minutes:N" or just a number (treated as seconds).
     */
    suspend fun executeDelay(value: String) {
        val parts = value.split(":")
        when {
            parts.size == 2 && parts[0].equals("seconds", ignoreCase = true) -> {
                val seconds = parts[1].toIntOrNull() ?: return
                waitSeconds(seconds)
            }
            parts.size == 2 && parts[0].equals("minutes", ignoreCase = true) -> {
                val minutes = parts[1].toIntOrNull() ?: return
                waitMinutes(minutes)
            }
            else -> {
                val seconds = value.toIntOrNull() ?: return
                waitSeconds(seconds)
            }
        }
    }
}
