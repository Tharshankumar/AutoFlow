package com.autoflow.app.core

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks system state to prevent routine conflicts and support reverse actions.
 * Maintains a key-value store of current system states with timestamps.
 */
class StateManager private constructor() {

    companion object {
        private const val TAG = "StateManager"

        // State keys
        const val STATE_WIFI_ENABLED = "wifi_enabled"
        const val STATE_BLUETOOTH_ENABLED = "bluetooth_enabled"
        const val STATE_CONNECTED_WIFI = "connected_wifi"
        const val STATE_VOLUME_LEVEL = "volume_level"
        const val STATE_VOLUME_RING = "volume_ring"
        const val STATE_VOLUME_ALARM = "volume_alarm"
        const val STATE_BATTERY_LEVEL = "battery_level"
        const val STATE_BATTERY_CHARGING = "battery_charging"
        const val STATE_BATTERY_SAVER = "battery_saver"
        const val STATE_AIRPLANE_MODE = "airplane_mode"
        const val STATE_MOBILE_DATA = "mobile_data"
        const val STATE_BLUETOOTH_DEVICE = "bluetooth_device"
        const val STATE_LOCATION_LAT = "location_lat"
        const val STATE_LOCATION_LNG = "location_lng"

        @Volatile
        private var INSTANCE: StateManager? = null

        fun getInstance(): StateManager {
            return INSTANCE ?: synchronized(this) {
                val instance = StateManager()
                INSTANCE = instance
                instance
            }
        }
    }

    data class StateEntry(
        val key: String,
        val value: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val states = ConcurrentHashMap<String, StateEntry>()
    private val stateChangeListeners = ConcurrentHashMap.newKeySet<StateChangeListener>()

    fun interface StateChangeListener {
        fun onStateChanged(key: String, oldValue: String?, newValue: String)
    }

    /**
     * Get the current value of a state.
     */
    fun getState(key: String): String? {
        return states[key]?.value
    }

    /**
     * Get the full state entry including timestamp.
     */
    fun getStateEntry(key: String): StateEntry? {
        return states[key]
    }

    /**
     * Update a state value. Returns true if the state actually changed.
     */
    fun setState(key: String, value: String): Boolean {
        val oldEntry = states[key]
        val oldValue = oldEntry?.value

        if (oldValue == value) {
            return false // No change
        }

        val newEntry = StateEntry(key, value)
        states[key] = newEntry

        Log.d(TAG, "State changed: $key = $value (was: $oldValue)")

        // Notify listeners
        stateChangeListeners.forEach { listener ->
            try {
                listener.onStateChanged(key, oldValue, value)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying state change listener", e)
            }
        }

        return true
    }

    /**
     * Get all current states.
     */
    fun getAllStates(): Map<String, StateEntry> {
        return states.toMap()
    }

    /**
     * Check if a state has changed from a previous value.
     */
    fun hasStateChanged(key: String, previousValue: String): Boolean {
        val currentValue = getState(key) ?: return true
        return currentValue != previousValue
    }

    /**
     * Register a listener for state changes.
     */
    fun addStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.add(listener)
    }

    /**
     * Remove a state change listener.
     */
    fun removeStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.remove(listener)
    }

    /**
     * Save a snapshot of current states for reverse action support.
     */
    fun saveSnapshot(keys: List<String>): Map<String, String> {
        val snapshot = mutableMapOf<String, String>()
        keys.forEach { key ->
            getState(key)?.let { value ->
                snapshot[key] = value
            }
        }
        Log.d(TAG, "State snapshot saved: $snapshot")
        return snapshot
    }

    /**
     * Clear all states.
     */
    fun clear() {
        states.clear()
        stateChangeListeners.clear()
        Log.d(TAG, "All states cleared")
    }
}
