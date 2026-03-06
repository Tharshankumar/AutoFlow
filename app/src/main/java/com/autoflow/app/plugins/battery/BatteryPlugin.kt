package com.autoflow.app.plugins.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.Event
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class BatteryPlugin : AutomationPlugin {

    companion object {
        private const val TAG = "BatteryPlugin"
    }

    override val pluginId = "battery"
    override val pluginName = "Battery"

    private var context: Context? = null
    private var eventBus: EventBus? = null
    private var stateManager: StateManager? = null
    private var batteryReceiver: BroadcastReceiver? = null

    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        this.eventBus = eventBus
        this.stateManager = stateManager

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                        if (level >= 0 && scale > 0) {
                            val batteryPct = (level * 100) / scale
                            Log.d(TAG, "Battery: $batteryPct%, charging: $isCharging")

                            eventBus.publish(Event(
                                type = Event.TYPE_BATTERY_LEVEL_CHANGED,
                                payload = mapOf(
                                    Event.KEY_BATTERY_LEVEL to batteryPct,
                                    Event.KEY_BATTERY_IS_CHARGING to isCharging
                                )
                            ))

                            if (isCharging) {
                                eventBus.publish(Event(
                                    type = Event.TYPE_BATTERY_CHARGING,
                                    payload = mapOf(Event.KEY_BATTERY_LEVEL to batteryPct)
                                ))
                            } else {
                                eventBus.publish(Event(
                                    type = Event.TYPE_BATTERY_DISCHARGING,
                                    payload = mapOf(Event.KEY_BATTERY_LEVEL to batteryPct)
                                ))
                            }
                        }
                    }
                }
            }
        }

        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d(TAG, "Battery plugin initialized")
    }

    override fun registerTriggers(): List<TriggerDefinition> = listOf(
        TriggerDefinition(
            type = "battery_level",
            displayName = "Battery Level Changed",
            description = "Triggers when battery level reaches a threshold",
            valueHint = "e.g., 20 (triggers at or below this %)"
        ),
        TriggerDefinition(
            type = "battery_level_changed",
            displayName = "Battery Level Changed",
            description = "Triggers on any battery level change",
            valueHint = "e.g., 20"
        ),
        TriggerDefinition(
            type = "battery_charging",
            displayName = "Battery Charging",
            description = "Triggers when device starts charging",
            valueHint = "any"
        ),
        TriggerDefinition(
            type = "battery_discharging",
            displayName = "Battery Discharging",
            description = "Triggers when device stops charging",
            valueHint = "any"
        )
    )

    override fun registerConditions(): List<ConditionDefinition> = listOf(
        ConditionDefinition(
            type = "battery_below",
            displayName = "Battery Below",
            description = "Battery level is below a threshold",
            valueHint = "e.g., 20"
        ),
        ConditionDefinition(
            type = "battery_above",
            displayName = "Battery Above",
            description = "Battery level is above a threshold",
            valueHint = "e.g., 80"
        ),
        ConditionDefinition(
            type = "battery_range",
            displayName = "Battery Range",
            description = "Battery level is within a range",
            valueHint = "e.g., 20-80"
        )
    )

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "enable_battery_saver",
            displayName = "Enable Battery Saver",
            description = "Enable battery saver mode",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "disable_battery_saver",
            displayName = "Disable Battery Saver",
            description = "Disable battery saver mode"
        ),
        ActionDefinition(
            type = "show_notification",
            displayName = "Show Notification",
            description = "Show a notification",
            valueHint = "title|message"
        )
    )

    override fun shutdown() {
        try {
            batteryReceiver?.let { context?.unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering battery receiver", e)
        }
        batteryReceiver = null
        Log.d(TAG, "Battery plugin shut down")
    }
}
