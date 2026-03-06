package com.autoflow.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "triggers",
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
data class Trigger(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val type: String,
    val value: String
) {
    companion object {
        const val TYPE_BATTERY_LEVEL = "battery_level"
        const val TYPE_WIFI_CONNECTED = "wifi_connected"
        const val TYPE_BLUETOOTH_CONNECTED = "bluetooth_connected"
        const val TYPE_TIME = "time"
        const val TYPE_LOCATION = "location"
        const val TYPE_APP_OPENED = "app_opened"

        val ALL_TYPES = listOf(
            TYPE_BATTERY_LEVEL,
            TYPE_WIFI_CONNECTED,
            TYPE_BLUETOOTH_CONNECTED,
            TYPE_TIME,
            TYPE_LOCATION,
            TYPE_APP_OPENED
        )
    }
}
