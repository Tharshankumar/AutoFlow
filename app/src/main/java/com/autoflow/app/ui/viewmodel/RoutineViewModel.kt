package com.autoflow.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoflow.app.data.database.AppDatabase
import com.autoflow.app.data.database.entities.Action
import com.autoflow.app.data.database.entities.Condition
import com.autoflow.app.data.database.entities.ExecutionLog
import com.autoflow.app.data.database.entities.Routine
import com.autoflow.app.data.database.entities.Trigger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).routineDao()

    val allRoutines: Flow<List<Routine>> = dao.getAllRoutines()

    private val _allTriggers = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val allTriggers: StateFlow<Map<Long, List<String>>> = _allTriggers

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allRoutines.collect { routines ->
                val triggerMap = mutableMapOf<Long, List<String>>()
                routines.forEach { routine ->
                    val triggers = dao.getTriggersForRoutine(routine.id)
                    triggerMap[routine.id] = triggers.map { it.type }
                }
                _allTriggers.value = triggerMap
            }
        }
    }

    fun saveRoutine(
        routine: Routine,
        triggers: List<Trigger>,
        conditions: List<Condition>,
        actions: List<Action>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.saveCompleteRoutine(routine, triggers, conditions, actions)
        }
    }

    fun toggleRoutine(routineId: Long, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setRoutineEnabled(routineId, enabled)
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteRoutine(routine)
        }
    }

    fun loadRoutineDetail(
        routineId: Long,
        onLoaded: (Routine?, List<Trigger>, List<Condition>, List<Action>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val routine = dao.getRoutineById(routineId)
            val triggers = dao.getTriggersForRoutine(routineId)
            val conditions = dao.getConditionsForRoutine(routineId)
            val actions = dao.getActionsForRoutine(routineId)
            viewModelScope.launch(Dispatchers.Main) {
                onLoaded(routine, triggers, conditions, actions)
            }
        }
    }

    fun getLogsForRoutine(routineId: Long): Flow<List<ExecutionLog>> {
        return dao.getLogsForRoutine(routineId)
    }
}
