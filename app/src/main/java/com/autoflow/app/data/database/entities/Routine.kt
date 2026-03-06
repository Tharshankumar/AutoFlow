package com.autoflow.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val cooldownMs: Long = 60000L,
    val lastExecutedAt: Long = 0L,
    val reverseActionsEnabled: Boolean = false,
    val conditionOperator: String = "AND"
)
