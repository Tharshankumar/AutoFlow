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
        const val TYPE_BATTERY_CHARGING = "battery_charging"
        const val TYPE_BATTERY_DISCHARGING = "battery_discharging"
        const val TYPE_WIFI_CONNECTED = "wifi_connected"
        const val TYPE_WIFI_DISCONNECTED = "wifi_disconnected"
        const val TYPE_WIFI_NETWORK_CHANGED = "wifi_network_changed"
        const val TYPE_BLUETOOTH_CONNECTED = "bluetooth_connected"
        const val TYPE_DEVICE_CONNECTED = "device_connected"
        const val TYPE_DEVICE_DISCONNECTED = "device_disconnected"
        const val TYPE_TIME = "time"
        const val TYPE_LOCATION = "location"
        const val TYPE_APP_OPENED = "app_opened"
        const val TYPE_INCOMING_CALL = "incoming_call"
        const val TYPE_MISSED_CALL = "missed_call"
        const val TYPE_CALL_ENDED = "call_ended"
        const val TYPE_NFC_TAG_SCANNED = "nfc_tag_scanned"

        val ALL_TYPES = listOf(
            TYPE_BATTERY_LEVEL,
            TYPE_BATTERY_CHARGING,
            TYPE_BATTERY_DISCHARGING,
            TYPE_WIFI_CONNECTED,
            TYPE_WIFI_DISCONNECTED,
            TYPE_WIFI_NETWORK_CHANGED,
            TYPE_BLUETOOTH_CONNECTED,
            TYPE_DEVICE_CONNECTED,
            TYPE_DEVICE_DISCONNECTED,
            TYPE_TIME,
            TYPE_LOCATION,
            TYPE_APP_OPENED,
            TYPE_INCOMING_CALL,
            TYPE_MISSED_CALL,
            TYPE_CALL_ENDED,
            TYPE_NFC_TAG_SCANNED
        )
    }
}
