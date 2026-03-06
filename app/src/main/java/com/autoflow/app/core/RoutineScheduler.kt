package com.autoflow.app.core

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages routine scheduling with cooldown periods and state-change detection
 * to prevent repeated/unnecessary executions.
 */
class RoutineScheduler private constructor() {

    companion object {
        private const val TAG = "RoutineScheduler"
        const val DEFAULT_COOLDOWN_MS = 60_000L // 1 minute default cooldown

        @Volatile
        private var INSTANCE: RoutineScheduler? = null

        fun getInstance(): RoutineScheduler {
            return INSTANCE ?: synchronized(this) {
                val instance = RoutineScheduler()
                INSTANCE = instance
                instance
            }
        }
    }

    data class ExecutionRecord(
        val routineId: Long,
        val lastExecutedAt: Long,
        val lastTriggerValue: String,
        val executionCount: Int = 0
    )

    private val executionRecords = ConcurrentHashMap<Long, ExecutionRecord>()
    private val cooldowns = ConcurrentHashMap<Long, Long>() // routineId -> cooldownMs

    /**
     * Set the cooldown period for a specific routine.
     */
    fun setCooldown(routineId: Long, cooldownMs: Long) {
        cooldowns[routineId] = cooldownMs
        Log.d(TAG, "Cooldown set for routine $routineId: ${cooldownMs}ms")
    }

    /**
     * Check if a routine should execute based on cooldown and state change.
     * Returns true if the routine should execute.
     */
    fun shouldExecute(routineId: Long, triggerValue: String): Boolean {
        val record = executionRecords[routineId]
        val now = System.currentTimeMillis()

        // First execution - always allow
        if (record == null) {
            Log.d(TAG, "First execution for routine $routineId - allowing")
            return true
        }

        // Check cooldown
        val cooldown = cooldowns[routineId] ?: DEFAULT_COOLDOWN_MS
        val timeSinceLastExecution = now - record.lastExecutedAt
        if (timeSinceLastExecution < cooldown) {
            Log.d(TAG, "Routine $routineId in cooldown (${timeSinceLastExecution}ms < ${cooldown}ms)")
            return false
        }

        // Check state change - only execute if trigger value changed
        if (record.lastTriggerValue == triggerValue) {
            Log.d(TAG, "Routine $routineId - trigger value unchanged: $triggerValue")
            return false
        }

        Log.d(TAG, "Routine $routineId - state changed from '${record.lastTriggerValue}' to '$triggerValue'")
        return true
    }

    /**
     * Record that a routine was executed.
     */
    fun recordExecution(routineId: Long, triggerValue: String) {
        val existing = executionRecords[routineId]
        executionRecords[routineId] = ExecutionRecord(
            routineId = routineId,
            lastExecutedAt = System.currentTimeMillis(),
            lastTriggerValue = triggerValue,
            executionCount = (existing?.executionCount ?: 0) + 1
        )
        Log.d(TAG, "Recorded execution for routine $routineId with value: $triggerValue")
    }

    /**
     * Get the execution record for a routine.
     */
    fun getExecutionRecord(routineId: Long): ExecutionRecord? {
        return executionRecords[routineId]
    }

    /**
     * Reset execution tracking for a routine.
     */
    fun resetRoutine(routineId: Long) {
        executionRecords.remove(routineId)
        cooldowns.remove(routineId)
        Log.d(TAG, "Reset tracking for routine $routineId")
    }

    /**
     * Clear all execution records.
     */
    fun clear() {
        executionRecords.clear()
        cooldowns.clear()
        Log.d(TAG, "All execution records cleared")
    }
}
