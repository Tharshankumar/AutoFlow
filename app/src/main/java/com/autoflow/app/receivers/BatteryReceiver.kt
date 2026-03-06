package com.autoflow.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.autoflow.app.core.Event
import com.autoflow.app.core.EventBus
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine

class BatteryReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BatteryReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            if (level >= 0 && scale > 0) {
                val batteryPct = (level * 100) / scale
                Log.d(TAG, "Battery changed: $batteryPct%, charging: $isCharging")

                // Publish event through EventBus (new architecture)
                val event = Event(
                    type = Event.TYPE_BATTERY_LEVEL_CHANGED,
                    payload = mapOf(
                        Event.KEY_BATTERY_LEVEL to batteryPct,
                        Event.KEY_BATTERY_IS_CHARGING to isCharging
                    )
                )
                EventBus.getInstance().publish(event)

                // Also publish charging/discharging events
                if (isCharging) {
                    EventBus.getInstance().publish(
                        Event(
                            type = Event.TYPE_BATTERY_CHARGING,
                            payload = mapOf(Event.KEY_BATTERY_LEVEL to batteryPct)
                        )
                    )
                } else {
                    EventBus.getInstance().publish(
                        Event(
                            type = Event.TYPE_BATTERY_DISCHARGING,
                            payload = mapOf(Event.KEY_BATTERY_LEVEL to batteryPct)
                        )
                    )
                }

                // Legacy: still notify RuleEngine directly for backward compatibility
                RuleEngine.getInstance(context)
                    .onTrigger(Trigger.TYPE_BATTERY_LEVEL, batteryPct.toString())
            }
        }
    }
}
