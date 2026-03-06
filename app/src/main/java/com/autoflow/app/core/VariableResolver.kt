package com.autoflow.app.core

import android.util.Log

/**
 * Resolves automation variables at runtime.
 * Supports system variables like battery_level, wifi_ssid, time, location.
 */
class VariableResolver(private val stateManager: StateManager) {

    companion object {
        private const val TAG = "VariableResolver"

        // Variable names
        const val VAR_BATTERY_LEVEL = "battery_level"
        const val VAR_WIFI_SSID = "wifi_ssid"
        const val VAR_TIME = "time"
        const val VAR_LOCATION = "location"
        const val VAR_BLUETOOTH_DEVICE = "bluetooth_device"
        const val VAR_VOLUME_LEVEL = "volume_level"
        const val VAR_IS_CHARGING = "is_charging"
    }

    /**
     * Resolve a variable name to its current value.
     */
    fun resolve(variableName: String): String {
        val value = when (variableName) {
            VAR_BATTERY_LEVEL -> stateManager.getState(StateManager.STATE_BATTERY_LEVEL) ?: "0"
            VAR_WIFI_SSID -> stateManager.getState(StateManager.STATE_CONNECTED_WIFI) ?: ""
            VAR_BLUETOOTH_DEVICE -> stateManager.getState(StateManager.STATE_BLUETOOTH_DEVICE) ?: ""
            VAR_VOLUME_LEVEL -> stateManager.getState(StateManager.STATE_VOLUME_LEVEL) ?: "0"
            VAR_IS_CHARGING -> stateManager.getState(StateManager.STATE_BATTERY_CHARGING) ?: "false"
            VAR_TIME -> {
                val now = java.util.Calendar.getInstance()
                String.format("%02d:%02d", now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE))
            }
            VAR_LOCATION -> {
                val lat = stateManager.getState(StateManager.STATE_LOCATION_LAT) ?: "0"
                val lng = stateManager.getState(StateManager.STATE_LOCATION_LNG) ?: "0"
                "$lat,$lng"
            }
            else -> {
                // Try to resolve from state manager directly
                stateManager.getState(variableName) ?: ""
            }
        }
        Log.d(TAG, "Resolved variable '$variableName' = '$value'")
        return value
    }

    /**
     * Replace all variable references in a string with their current values.
     * Variables are referenced as {{variable_name}}.
     */
    fun resolveString(template: String): String {
        val regex = Regex("\\{\\{(\\w+)\\}\\}")
        return regex.replace(template) { matchResult ->
            val variableName = matchResult.groupValues[1]
            resolve(variableName)
        }
    }

    /**
     * Get all available variable names and their current values.
     */
    fun getAllVariables(): Map<String, String> {
        return mapOf(
            VAR_BATTERY_LEVEL to resolve(VAR_BATTERY_LEVEL),
            VAR_WIFI_SSID to resolve(VAR_WIFI_SSID),
            VAR_TIME to resolve(VAR_TIME),
            VAR_LOCATION to resolve(VAR_LOCATION),
            VAR_BLUETOOTH_DEVICE to resolve(VAR_BLUETOOTH_DEVICE),
            VAR_VOLUME_LEVEL to resolve(VAR_VOLUME_LEVEL),
            VAR_IS_CHARGING to resolve(VAR_IS_CHARGING)
        )
    }
}
