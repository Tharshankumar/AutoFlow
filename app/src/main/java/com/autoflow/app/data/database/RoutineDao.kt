package com.autoflow.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.autoflow.app.data.database.entities.Action
import com.autoflow.app.data.database.entities.Condition
import com.autoflow.app.data.database.entities.ExecutionLog
import com.autoflow.app.data.database.entities.Routine
import com.autoflow.app.data.database.entities.Trigger
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    // Routine operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE id = :routineId")
    suspend fun getRoutineById(routineId: Long): Routine?

    @Query("SELECT * FROM routines WHERE enabled = 1")
    suspend fun getEnabledRoutines(): List<Routine>

    @Query("UPDATE routines SET enabled = :enabled WHERE id = :routineId")
    suspend fun setRoutineEnabled(routineId: Long, enabled: Boolean)

    // Trigger operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: Trigger): Long

    @Query("SELECT * FROM triggers WHERE routineId = :routineId")
    suspend fun getTriggersForRoutine(routineId: Long): List<Trigger>

    @Query("SELECT * FROM triggers WHERE type = :triggerType")
    suspend fun getTriggersByType(triggerType: String): List<Trigger>

    @Query("DELETE FROM triggers WHERE routineId = :routineId")
    suspend fun deleteTriggersForRoutine(routineId: Long)

    // Condition operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCondition(condition: Condition): Long

    @Query("SELECT * FROM conditions WHERE routineId = :routineId")
    suspend fun getConditionsForRoutine(routineId: Long): List<Condition>

    @Query("DELETE FROM conditions WHERE routineId = :routineId")
    suspend fun deleteConditionsForRoutine(routineId: Long)

    // Action operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: Action): Long

    @Query("SELECT * FROM actions WHERE routineId = :routineId")
    suspend fun getActionsForRoutine(routineId: Long): List<Action>

    @Query("DELETE FROM actions WHERE routineId = :routineId")
    suspend fun deleteActionsForRoutine(routineId: Long)

    // Execution Log operations
    @Insert
    suspend fun insertExecutionLog(log: ExecutionLog): Long

    @Query("SELECT * FROM execution_logs WHERE routineId = :routineId ORDER BY timestamp DESC")
    fun getLogsForRoutine(routineId: Long): Flow<List<ExecutionLog>>

    @Query("SELECT * FROM execution_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<ExecutionLog>>

    // Transaction: Save complete routine with triggers, conditions, and actions
    @Transaction
    suspend fun saveCompleteRoutine(
        routine: Routine,
        triggers: List<Trigger>,
        conditions: List<Condition>,
        actions: List<Action>
    ): Long {
        val routineId = insertRoutine(routine)
        deleteTriggersForRoutine(routineId)
        deleteConditionsForRoutine(routineId)
        deleteActionsForRoutine(routineId)
        triggers.forEach { insertTrigger(it.copy(routineId = routineId)) }
        conditions.forEach { insertCondition(it.copy(routineId = routineId)) }
        actions.forEach { insertAction(it.copy(routineId = routineId)) }
        return routineId
    }

    // Get routines that have a specific trigger type
    @Query("""
        SELECT DISTINCT r.* FROM routines r
        INNER JOIN triggers t ON r.id = t.routineId
        WHERE t.type = :triggerType AND r.enabled = 1
    """)
    suspend fun getEnabledRoutinesByTriggerType(triggerType: String): List<Routine>
}
