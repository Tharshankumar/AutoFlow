package com.autoflow.app.core

import android.content.Context
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages reverse actions for routines. When a trigger condition reverses
 * (e.g., headphones disconnected), the reverse actions are executed to
 * restore the previous state.
 */
class ReverseActionEngine private constructor() {

    companion object {
        private const val TAG = "ReverseActionEngine"

        @Volatile
        private var INSTANCE: ReverseActionEngine? = null

        fun getInstance(): ReverseActionEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = ReverseActionEngine()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Represents a reversible action with apply and reverse operations.
     */
    interface ReversibleAction {
        /** Execute the forward action, saving state for reversal. */
        fun apply(context: Context, stateManager: StateManager): Boolean
        /** Reverse the action, restoring previous state. */
        fun reverse(context: Context, stateManager: StateManager): Boolean
        /** Get the state keys this action modifies. */
        fun getAffectedStateKeys(): List<String>
    }

    data class ReverseActionRecord(
        val routineId: Long,
        val triggerType: String,
        val savedStates: Map<String, String>,
        val actions: List<ReversibleAction>,
        val timestamp: Long = System.currentTimeMillis()
    )

    // routineId -> ReverseActionRecord
    private val activeReverseActions = ConcurrentHashMap<Long, ReverseActionRecord>()

    /**
     * Store reverse actions for a routine when its trigger activates.
     */
    fun storeReverseActions(
        routineId: Long,
        triggerType: String,
        actions: List<ReversibleAction>,
        stateManager: StateManager
    ) {
        // Collect all affected state keys
        val allKeys = actions.flatMap { it.getAffectedStateKeys() }.distinct()
        val savedStates = stateManager.saveSnapshot(allKeys)

        val record = ReverseActionRecord(
            routineId = routineId,
            triggerType = triggerType,
            savedStates = savedStates,
            actions = actions
        )

        activeReverseActions[routineId] = record
        Log.d(TAG, "Stored reverse actions for routine $routineId: ${savedStates.size} states saved")
    }

    /**
     * Execute reverse actions for a routine when its trigger deactivates.
     */
    fun executeReverseActions(context: Context, routineId: Long, stateManager: StateManager): Boolean {
        val record = activeReverseActions.remove(routineId) ?: run {
            Log.d(TAG, "No reverse actions found for routine $routineId")
            return false
        }

        Log.d(TAG, "Executing reverse actions for routine $routineId")

        // Restore saved states
        record.savedStates.forEach { (key, value) ->
            stateManager.setState(key, value)
        }

        // Execute reverse on each action
        var allSuccess = true
        record.actions.forEach { action ->
            try {
                if (!action.reverse(context, stateManager)) {
                    Log.w(TAG, "Reverse action failed for routine $routineId")
                    allSuccess = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing reverse action for routine $routineId", e)
                allSuccess = false
            }
        }

        return allSuccess
    }

    /**
     * Check if a routine has pending reverse actions.
     */
    fun hasReverseActions(routineId: Long): Boolean {
        return activeReverseActions.containsKey(routineId)
    }

    /**
     * Get the saved state for a routine's reverse actions.
     */
    fun getSavedStates(routineId: Long): Map<String, String>? {
        return activeReverseActions[routineId]?.savedStates
    }

    /**
     * Clear all stored reverse actions.
     */
    fun clear() {
        activeReverseActions.clear()
        Log.d(TAG, "All reverse actions cleared")
    }
}
