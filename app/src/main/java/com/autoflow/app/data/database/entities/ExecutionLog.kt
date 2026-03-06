package com.autoflow.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "execution_logs",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId"])]
)
data class ExecutionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val triggerType: String
) {
    companion object {
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILURE = "failure"
        const val STATUS_SKIPPED = "skipped"
    }
}
