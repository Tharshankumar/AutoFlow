package com.autoflow.app.plugins.internet

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class InternetControlPlugin : AutomationPlugin {

    companion object {
        private const val TAG = "InternetControlPlugin"
    }

    override val pluginId = "internet"
    override val pluginName = "Internet Control"

    private var context: Context? = null

    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        Log.d(TAG, "Internet Control plugin initialized")
    }

    override fun registerTriggers(): List<TriggerDefinition> = emptyList()

    override fun registerConditions(): List<ConditionDefinition> = emptyList()

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "enable_mobile_data",
            displayName = "Enable Mobile Data",
            description = "Open settings to enable mobile data",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "disable_mobile_data",
            displayName = "Disable Mobile Data",
            description = "Open settings to disable mobile data"
        ),
        ActionDefinition(
            type = "enable_airplane_mode",
            displayName = "Enable Airplane Mode",
            description = "Open settings to enable airplane mode",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "disable_airplane_mode",
            displayName = "Disable Airplane Mode",
            description = "Open settings to disable airplane mode"
        )
    )

    fun executeAction(context: Context, actionType: String, value: String) {
        when (actionType) {
            "enable_mobile_data", "disable_mobile_data" -> {
                val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Internet connectivity settings opened for: $actionType")
            }
            "enable_airplane_mode", "disable_airplane_mode" -> {
                val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Airplane mode settings opened for: $actionType")
            }
        }
    }

    override fun shutdown() {
        Log.d(TAG, "Internet Control plugin shut down")
    }
}
