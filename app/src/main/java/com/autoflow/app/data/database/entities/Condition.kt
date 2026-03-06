package com.autoflow.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conditions",
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
data class Condition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val type: String,
    val value: String
) {
    companion object {
        const val TYPE_TIME_RANGE = "time_range"
        const val TYPE_BATTERY_RANGE = "battery_range"
        const val TYPE_WIFI_NETWORK = "wifi_network"
        const val TYPE_LOCATION_RADIUS = "location_radius"

        val ALL_TYPES = listOf(
            TYPE_TIME_RANGE,
            TYPE_BATTERY_RANGE,
            TYPE_WIFI_NETWORK,
            TYPE_LOCATION_RADIUS
        )
    }
}
