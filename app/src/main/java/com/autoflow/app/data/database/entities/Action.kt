package com.autoflow.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actions",
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
data class Action(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val type: String,
    val value: String
) {
    companion object {
        const val TYPE_SET_VOLUME = "set_volume"
        const val TYPE_TOGGLE_WIFI = "toggle_wifi"
        const val TYPE_TOGGLE_BLUETOOTH = "toggle_bluetooth"
        const val TYPE_OPEN_APP = "open_app"
        const val TYPE_SHOW_NOTIFICATION = "show_notification"

        val ALL_TYPES = listOf(
            TYPE_SET_VOLUME,
            TYPE_TOGGLE_WIFI,
            TYPE_TOGGLE_BLUETOOTH,
            TYPE_OPEN_APP,
            TYPE_SHOW_NOTIFICATION
        )
    }
}
