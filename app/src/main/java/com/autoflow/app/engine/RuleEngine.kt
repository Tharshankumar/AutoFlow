package com.autoflow.app.engine

import android.content.Context
import android.util.Log
import com.autoflow.app.data.database.AppDatabase
import com.autoflow.app.data.database.entities.ExecutionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RuleEngine private constructor(context: Context) {

    companion object {
        private const val TAG = "RuleEngine"

        @Volatile
        private var INSTANCE: RuleEngine? = null

        fun getInstance(context: Context): RuleEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = RuleEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = AppDatabase.getInstance(context)
    private val dao = database.routineDao()
    private val conditionEvaluator = ConditionEvaluator(context, dao)
    private val actionExecutor = ActionExecutor(context, dao)

    fun onTrigger(triggerType: String, triggerValue: String) {
        Log.d(TAG, "Trigger received: type=$triggerType, value=$triggerValue")

        scope.launch {
            try {
                val routines = dao.getEnabledRoutinesByTriggerType(triggerType)
                Log.d(TAG, "Found ${routines.size} enabled routines for trigger: $triggerType")

                for (routine in routines) {
                    if (routine.enabled) {
                        val triggers = dao.getTriggersForRoutine(routine.id)
                        val matchingTrigger = triggers.find { trigger ->
                            trigger.type == triggerType && matchesTriggerValue(trigger.value, triggerValue, triggerType)
                        }

                        if (matchingTrigger != null) {
                            if (conditionEvaluator.evaluate(routine.id)) {
                                Log.d(TAG, "Executing actions for routine: ${routine.name}")
                                actionExecutor.execute(routine.id)

                                dao.insertExecutionLog(
                                    ExecutionLog(
                                        routineId = routine.id,
                                        status = ExecutionLog.STATUS_SUCCESS,
                                        triggerType = triggerType
                                    )
                                )
                            } else {
                                Log.d(TAG, "Conditions not met for routine: ${routine.name}")
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
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing trigger: $triggerType", e)
            }
        }
    }

    private fun matchesTriggerValue(
        storedValue: String,
        currentValue: String,
        triggerType: String
    ): Boolean {
        return when (triggerType) {
            com.autoflow.app.data.database.entities.Trigger.TYPE_BATTERY_LEVEL -> {
                // storedValue: threshold (e.g., "20"), currentValue: current battery level
                val threshold = storedValue.toIntOrNull() ?: return false
                val current = currentValue.toIntOrNull() ?: return false
                current <= threshold
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_WIFI_CONNECTED -> {
                // storedValue: SSID or "any", currentValue: connected SSID
                storedValue.equals("any", ignoreCase = true) ||
                    storedValue.equals(currentValue, ignoreCase = true)
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_BLUETOOTH_CONNECTED -> {
                // storedValue: device name or "any", currentValue: connected device
                storedValue.equals("any", ignoreCase = true) ||
                    storedValue.equals(currentValue, ignoreCase = true)
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_TIME -> {
                // storedValue: "HH:mm", currentValue: current time "HH:mm"
                storedValue == currentValue
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_LOCATION -> {
                // Location matching requires more complex geo-fencing
                true
            }
            com.autoflow.app.data.database.entities.Trigger.TYPE_APP_OPENED -> {
                storedValue.equals(currentValue, ignoreCase = true)
            }
            else -> false
        }
    }
}
