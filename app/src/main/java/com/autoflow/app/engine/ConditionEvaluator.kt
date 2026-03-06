package com.autoflow.app.engine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.util.Log
import com.autoflow.app.data.database.RoutineDao
import com.autoflow.app.data.database.entities.Condition

class ConditionEvaluator(
    private val context: Context,
    private val routineDao: RoutineDao
) {
    companion object {
        private const val TAG = "ConditionEvaluator"
    }

    suspend fun evaluate(routineId: Long): Boolean {
        val conditions = routineDao.getConditionsForRoutine(routineId)
        if (conditions.isEmpty()) return true

        return conditions.all { condition ->
            evaluateCondition(condition)
        }
    }

    private fun evaluateCondition(condition: Condition): Boolean {
        return try {
            when (condition.type) {
                Condition.TYPE_TIME_RANGE -> evaluateTimeRange(condition.value)
                Condition.TYPE_BATTERY_RANGE -> evaluateBatteryRange(condition.value)
                Condition.TYPE_WIFI_NETWORK -> evaluateWifiNetwork(condition.value)
                Condition.TYPE_LOCATION_RADIUS -> evaluateLocationRadius(condition.value)
                else -> {
                    Log.w(TAG, "Unknown condition type: ${condition.type}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating condition: ${condition.type}", e)
            false
        }
    }

    private fun evaluateTimeRange(value: String): Boolean {
        // value format: "HH:mm-HH:mm" (e.g., "09:00-17:00")
        val parts = value.split("-")
        if (parts.size != 2) return false

        val now = java.util.Calendar.getInstance()
        val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)

        val startParts = parts[0].split(":")
        val endParts = parts[1].split(":")
        if (startParts.size != 2 || endParts.size != 2) return false

        val startMinutes = startParts[0].toIntOrNull()?.times(60)?.plus(startParts[1].toIntOrNull() ?: return false) ?: return false
        val endMinutes = endParts[0].toIntOrNull()?.times(60)?.plus(endParts[1].toIntOrNull() ?: return false) ?: return false

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Overnight range (e.g., 22:00-06:00)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    private fun evaluateBatteryRange(value: String): Boolean {
        // value format: "min-max" (e.g., "20-80")
        val parts = value.split("-")
        if (parts.size != 2) return false

        val min = parts[0].toIntOrNull() ?: return false
        val max = parts[1].toIntOrNull() ?: return false

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: return false
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (scale <= 0) return false

        val batteryPct = (level * 100) / scale
        return batteryPct in min..max
    }

    @Suppress("DEPRECATION")
    private fun evaluateWifiNetwork(value: String): Boolean {
        // value format: network SSID name
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return false

        val connectionInfo = wifiManager.connectionInfo
        val currentSsid = connectionInfo.ssid?.removeSurrounding("\"") ?: return false
        return currentSsid.equals(value, ignoreCase = true)
    }

    private fun evaluateLocationRadius(value: String): Boolean {
        // value format: "lat,lng,radiusMeters" (e.g., "37.7749,-122.4194,500")
        // Location-based condition evaluation requires more complex setup with FusedLocationProvider
        // For now, return true as a placeholder - full implementation would need last known location
        Log.d(TAG, "Location radius condition not fully implemented: $value")
        return true
    }
}
