package com.autoflow.app.core

import android.content.Context
import android.util.Log
import com.autoflow.app.data.database.AppDatabase
import com.autoflow.app.data.database.entities.ExecutionLog
import com.autoflow.app.engine.ActionExecutor
import com.autoflow.app.engine.ConditionEvaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Central automation engine that connects the EventBus to the RuleEngine,
 * StateManager, and ActionExecutor in the event-driven architecture.
 *
 * Architecture flow:
 * System Sensors -> Event Collectors -> EventBus -> AutomationEngine
 * -> RuleEngine -> StateManager -> ActionExecutor -> ReverseActionEngine
 */
class AutomationEngine private constructor(private val context: Context) {

    companion object {
        private const val TAG = "AutomationEngine"

        @Volatile
        private var INSTANCE: AutomationEngine? = null

        fun getInstance(context: Context): AutomationEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = AutomationEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val supervisorJob = SupervisorJob()
    private var scope = CoroutineScope(supervisorJob + Dispatchers.IO)
    private val eventBus = EventBus.getInstance()
    private val stateManager = StateManager.getInstance()
    private val reverseActionEngine = ReverseActionEngine.getInstance()
    private val routineScheduler = RoutineScheduler.getInstance()
    private val database = AppDatabase.getInstance(context)
    private val dao = database.routineDao()
    private val conditionEvaluator = ConditionEvaluator(context, dao)
    private val actionExecutor = ActionExecutor(context, dao)
    private val pluginRegistry = PluginRegistry.getInstance()

    private var isRunning = false

    /**
     * Start the automation engine and begin listening to events.
     */
    fun start() {
        if (isRunning) {
            Log.d(TAG, "AutomationEngine already running")
            return
        }

        Log.d(TAG, "Starting AutomationEngine")
        isRunning = true

        // Create a fresh coroutine scope
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // Subscribe to all events from the EventBus
        eventBus.subscribeAll(EventBus.EventListener { event ->
            handleEvent(event)
        })

        // Note: Plugins are registered and initialized in AutoFlowApplication.onCreate()
        // No duplicate initialization here.

        Log.d(TAG, "AutomationEngine started successfully")
    }

    /**
     * Stop the automation engine.
     */
    fun stop() {
        Log.d(TAG, "Stopping AutomationEngine")
        isRunning = false

        // Cancel all in-flight coroutines
        scope.cancel()

        eventBus.clear()
        pluginRegistry.shutdownAll()
        Log.d(TAG, "AutomationEngine stopped")
    }

    /**
     * Handle an incoming event from the EventBus.
     */
    private fun handleEvent(event: Event) {
        if (!isRunning) return

        Log.d(TAG, "Handling event: type=${event.type}")

        // Update state manager based on event
        updateStateFromEvent(event)

        // Map event to trigger types and process routines
        scope.launch {
            processEventForRoutines(event)
        }
    }

    /**
     * Update the StateManager based on the incoming event.
     */
    private fun updateStateFromEvent(event: Event) {
        when (event.type) {
            Event.TYPE_BATTERY_LEVEL_CHANGED -> {
                event.getIntPayload(Event.KEY_BATTERY_LEVEL)?.let {
                    stateManager.setState(StateManager.STATE_BATTERY_LEVEL, it.toString())
                }
                event.getBooleanPayload(Event.KEY_BATTERY_IS_CHARGING)?.let {
                    stateManager.setState(StateManager.STATE_BATTERY_CHARGING, it.toString())
                }
            }
            Event.TYPE_WIFI_CONNECTED -> {
                stateManager.setState(StateManager.STATE_WIFI_ENABLED, "true")
                event.getStringPayload(Event.KEY_WIFI_SSID)?.let {
                    stateManager.setState(StateManager.STATE_CONNECTED_WIFI, it)
                }
            }
            Event.TYPE_WIFI_DISCONNECTED -> {
                stateManager.setState(StateManager.STATE_CONNECTED_WIFI, "")
            }
            Event.TYPE_BLUETOOTH_DEVICE_CONNECTED -> {
                stateManager.setState(StateManager.STATE_BLUETOOTH_ENABLED, "true")
                event.getStringPayload(Event.KEY_BLUETOOTH_DEVICE_NAME)?.let {
                    stateManager.setState(StateManager.STATE_BLUETOOTH_DEVICE, it)
                }
            }
            Event.TYPE_BLUETOOTH_DEVICE_DISCONNECTED -> {
                stateManager.setState(StateManager.STATE_BLUETOOTH_DEVICE, "")
            }
        }
    }

    /**
     * Process an event against all enabled routines.
     */
    private suspend fun processEventForRoutines(event: Event) {
        try {
            // Map event type to legacy trigger types for backward compatibility
            val triggerTypes = mapEventToTriggerTypes(event)
            val triggerValue = extractTriggerValue(event)

            for (triggerType in triggerTypes) {
                val routines = dao.getEnabledRoutinesByTriggerType(triggerType)
                Log.d(TAG, "Found ${routines.size} routines for trigger: $triggerType")

                for (routine in routines) {
                    if (!routine.enabled) continue

                    val triggers = dao.getTriggersForRoutine(routine.id)
                    val matchingTrigger = triggers.find { trigger ->
                        trigger.type == triggerType && matchesTriggerValue(
                            trigger.value, triggerValue, triggerType
                        )
                    }

                    if (matchingTrigger != null) {
                        // Check cooldown and state change
                        if (!routineScheduler.shouldExecute(routine.id, triggerValue)) {
                            Log.d(TAG, "Routine ${routine.name} skipped (cooldown/no state change)")
                            continue
                        }

                        // Evaluate conditions
                        if (conditionEvaluator.evaluate(routine.id)) {
                            Log.d(TAG, "Executing routine: ${routine.name}")

                            // Execute actions
                            actionExecutor.execute(routine.id)

                            // Record execution
                            routineScheduler.recordExecution(routine.id, triggerValue)

                            dao.insertExecutionLog(
                                ExecutionLog(
                                    routineId = routine.id,
                                    status = ExecutionLog.STATUS_SUCCESS,
                                    triggerType = triggerType
                                )
                            )
                        } else {
                            Log.d(TAG, "Conditions not met for: ${routine.name}")
                            dao.insertExecutionLog(
                                ExecutionLog(
                                    routineId = routine.id,
                                    status = ExecutionLog.STATUS_SKIPPED,
                                    triggerType = triggerType
                                )
                            )
                        }
                    }
                }

                // Handle reverse actions for disconnect/deactivation events
                handleReverseEvents(event, triggerType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing event: ${event.type}", e)
        }
    }

    /**
     * Handle reverse events (e.g., bluetooth disconnected should reverse bluetooth connected actions).
     */
    private suspend fun handleReverseEvents(event: Event, triggerType: String) {
        val isReverseEvent = when (event.type) {
            Event.TYPE_WIFI_DISCONNECTED,
            Event.TYPE_BLUETOOTH_DEVICE_DISCONNECTED,
            Event.TYPE_BATTERY_DISCHARGING -> true
            else -> false
        }

        if (isReverseEvent) {
            val routines = dao.getEnabledRoutines()
            for (routine in routines) {
                if (reverseActionEngine.hasReverseActions(routine.id)) {
                    Log.d(TAG, "Executing reverse actions for routine: ${routine.name}")
                    reverseActionEngine.executeReverseActions(context, routine.id, stateManager)
                }
            }
        }
    }

    /**
     * Map new event types to legacy trigger types for backward compatibility.
     */
    private fun mapEventToTriggerTypes(event: Event): List<String> {
        return when (event.type) {
            Event.TYPE_BATTERY_LEVEL_CHANGED -> listOf(
                com.autoflow.app.data.database.entities.Trigger.TYPE_BATTERY_LEVEL,
                "battery_level_changed"
            )
            Event.TYPE_BATTERY_CHARGING -> listOf("battery_charging")
            Event.TYPE_BATTERY_DISCHARGING -> listOf("battery_discharging")
            Event.TYPE_WIFI_CONNECTED -> listOf(
                com.autoflow.app.data.database.entities.Trigger.TYPE_WIFI_CONNECTED,
                "wifi_connected"
            )
            Event.TYPE_WIFI_DISCONNECTED -> listOf("wifi_disconnected")
            Event.TYPE_WIFI_NETWORK_CHANGED -> listOf("wifi_network_changed")
            Event.TYPE_BLUETOOTH_CONNECTED -> listOf(
                com.autoflow.app.data.database.entities.Trigger.TYPE_BLUETOOTH_CONNECTED,
                "bluetooth_connected"
            )
            Event.TYPE_BLUETOOTH_DEVICE_CONNECTED -> listOf(
                com.autoflow.app.data.database.entities.Trigger.TYPE_BLUETOOTH_CONNECTED,
                "device_connected"
            )
            Event.TYPE_BLUETOOTH_DEVICE_DISCONNECTED -> listOf("device_disconnected")
            Event.TYPE_INCOMING_CALL -> listOf("incoming_call")
            Event.TYPE_MISSED_CALL -> listOf("missed_call")
            Event.TYPE_CALL_ENDED -> listOf("call_ended")
            Event.TYPE_NFC_TAG_SCANNED -> listOf("nfc_tag_scanned")
            Event.TYPE_TIME_REACHED -> listOf(
                com.autoflow.app.data.database.entities.Trigger.TYPE_TIME
            )
            Event.TYPE_LOCATION_CHANGED -> listOf(
                com.autoflow.app.data.database.entities.Trigger.TYPE_LOCATION
            )
            Event.TYPE_APP_OPENED -> listOf(
                com.autoflow.app.data.database.entities.Trigger.TYPE_APP_OPENED
            )
            else -> listOf(event.type)
        }
    }

    /**
     * Extract the trigger value from an event for matching.
     */
    private fun extractTriggerValue(event: Event): String {
        return when (event.type) {
            Event.TYPE_BATTERY_LEVEL_CHANGED ->
                event.getIntPayload(Event.KEY_BATTERY_LEVEL)?.toString() ?: ""
            Event.TYPE_WIFI_CONNECTED ->
                event.getStringPayload(Event.KEY_WIFI_SSID) ?: ""
            Event.TYPE_BLUETOOTH_DEVICE_CONNECTED,
            Event.TYPE_BLUETOOTH_DEVICE_DISCONNECTED ->
                event.getStringPayload(Event.KEY_BLUETOOTH_DEVICE_NAME) ?: ""
            Event.TYPE_TIME_REACHED ->
                event.getStringPayload(Event.KEY_TIME) ?: ""
            Event.TYPE_LOCATION_CHANGED ->
                "${event.getDoublePayload(Event.KEY_LATITUDE)},${event.getDoublePayload(Event.KEY_LONGITUDE)}"
            Event.TYPE_INCOMING_CALL, Event.TYPE_MISSED_CALL ->
                event.getStringPayload(Event.KEY_PHONE_NUMBER) ?: ""
            Event.TYPE_NFC_TAG_SCANNED ->
                event.getStringPayload(Event.KEY_NFC_TAG_ID) ?: ""
            Event.TYPE_APP_OPENED ->
                event.getStringPayload(Event.KEY_PACKAGE_NAME) ?: ""
            else -> ""
        }
    }

    /**
     * Match trigger value against stored value (backward compatible with RuleEngine logic).
     */
    private fun matchesTriggerValue(
        storedValue: String,
        currentValue: String,
        triggerType: String
    ): Boolean {
        return when (triggerType) {
            com.autoflow.app.data.database.entities.Trigger.TYPE_BATTERY_LEVEL,
            "battery_level_changed" -> {
                val threshold = storedValue.toIntOrNull() ?: return false
                val current = currentValue.toIntOrNull() ?: return false
                current <= threshold
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_WIFI_CONNECTED,
            "wifi_connected", "wifi_network_changed" -> {
                storedValue.equals("any", ignoreCase = true) ||
                    storedValue.equals(currentValue, ignoreCase = true)
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_BLUETOOTH_CONNECTED,
            "bluetooth_connected", "device_connected" -> {
                storedValue.equals("any", ignoreCase = true) ||
                    storedValue.equals(currentValue, ignoreCase = true)
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_TIME -> {
                storedValue == currentValue
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_LOCATION -> {
                true // Location matching handled by conditions
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_APP_OPENED -> {
                storedValue.equals(currentValue, ignoreCase = true)
            }
            "incoming_call", "missed_call", "call_ended" -> {
                storedValue.equals("any", ignoreCase = true) ||
                    storedValue.equals(currentValue, ignoreCase = true)
            }
            "nfc_tag_scanned" -> {
                storedValue.equals("any", ignoreCase = true) ||
                    storedValue.equals(currentValue, ignoreCase = true)
            }
            "battery_charging", "battery_discharging" -> true
            "wifi_disconnected", "device_disconnected" -> true
            else -> false
        }
    }
}
