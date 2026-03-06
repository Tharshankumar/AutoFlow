package com.autoflow.app.core

import android.content.Context
import android.util.Log

/**
 * Plugin interface that all AutoFlow plugins must implement.
 * Plugins provide triggers, conditions, and actions to the automation system.
 */
interface AutomationPlugin {
    /** Unique identifier for this plugin. */
    val pluginId: String

    /** Human-readable name for this plugin. */
    val pluginName: String

    /** Initialize the plugin and register its components. */
    fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager)

    /** Register triggers provided by this plugin. */
    fun registerTriggers(): List<TriggerDefinition>

    /** Register conditions provided by this plugin. */
    fun registerConditions(): List<ConditionDefinition>

    /** Register actions provided by this plugin. */
    fun registerActions(): List<ActionDefinition>

    /** Shutdown the plugin and release resources. */
    fun shutdown()
}

/**
 * Defines a trigger type that a plugin provides.
 */
data class TriggerDefinition(
    val type: String,
    val displayName: String,
    val description: String,
    val valueHint: String = "",
    val pluginId: String = ""
)

/**
 * Defines a condition type that a plugin provides.
 */
data class ConditionDefinition(
    val type: String,
    val displayName: String,
    val description: String,
    val valueHint: String = "",
    val pluginId: String = ""
)

/**
 * Defines an action type that a plugin provides.
 */
data class ActionDefinition(
    val type: String,
    val displayName: String,
    val description: String,
    val valueHint: String = "",
    val supportsReverse: Boolean = false,
    val pluginId: String = ""
)

/**
 * Registry for managing automation plugins.
 * Plugins auto-register at startup and provide triggers, conditions, and actions.
 */
class PluginRegistry private constructor() {

    companion object {
        private const val TAG = "PluginRegistry"

        @Volatile
        private var INSTANCE: PluginRegistry? = null

        fun getInstance(): PluginRegistry {
            return INSTANCE ?: synchronized(this) {
                val instance = PluginRegistry()
                INSTANCE = instance
                instance
            }
        }
    }

    private val plugins = mutableListOf<AutomationPlugin>()
    private val triggerDefinitions = mutableListOf<TriggerDefinition>()
    private val conditionDefinitions = mutableListOf<ConditionDefinition>()
    private val actionDefinitions = mutableListOf<ActionDefinition>()

    /**
     * Register a plugin with the registry.
     */
    fun registerPlugin(plugin: AutomationPlugin) {
        plugins.add(plugin)
        Log.d(TAG, "Plugin registered: ${plugin.pluginName} (${plugin.pluginId})")
    }

    /**
     * Initialize all registered plugins.
     */
    fun initializeAll(context: Context) {
        val eventBus = EventBus.getInstance()
        val stateManager = StateManager.getInstance()

        plugins.forEach { plugin ->
            try {
                plugin.initialize(context, eventBus, stateManager)

                // Collect trigger definitions
                val triggers = plugin.registerTriggers()
                triggerDefinitions.addAll(triggers.map { it.copy(pluginId = plugin.pluginId) })

                // Collect condition definitions
                val conditions = plugin.registerConditions()
                conditionDefinitions.addAll(conditions.map { it.copy(pluginId = plugin.pluginId) })

                // Collect action definitions
                val actions = plugin.registerActions()
                actionDefinitions.addAll(actions.map { it.copy(pluginId = plugin.pluginId) })

                Log.d(TAG, "Plugin initialized: ${plugin.pluginName} " +
                    "(${triggers.size} triggers, ${conditions.size} conditions, ${actions.size} actions)")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing plugin: ${plugin.pluginName}", e)
            }
        }
    }

    /**
     * Shutdown all plugins.
     */
    fun shutdownAll() {
        plugins.forEach { plugin ->
            try {
                plugin.shutdown()
            } catch (e: Exception) {
                Log.e(TAG, "Error shutting down plugin: ${plugin.pluginName}", e)
            }
        }
        triggerDefinitions.clear()
        conditionDefinitions.clear()
        actionDefinitions.clear()
        Log.d(TAG, "All plugins shut down")
    }

    /**
     * Get all registered trigger definitions.
     */
    fun getAllTriggerDefinitions(): List<TriggerDefinition> = triggerDefinitions.toList()

    /**
     * Get all registered condition definitions.
     */
    fun getAllConditionDefinitions(): List<ConditionDefinition> = conditionDefinitions.toList()

    /**
     * Get all registered action definitions.
     */
    fun getAllActionDefinitions(): List<ActionDefinition> = actionDefinitions.toList()

    /**
     * Get all registered plugins.
     */
    fun getAllPlugins(): List<AutomationPlugin> = plugins.toList()

    /**
     * Get a plugin by its ID.
     */
    fun getPlugin(pluginId: String): AutomationPlugin? {
        return plugins.find { it.pluginId == pluginId }
    }
}
