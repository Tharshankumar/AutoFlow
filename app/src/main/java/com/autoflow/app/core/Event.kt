package com.autoflow.app.core

import java.util.UUID

/**
 * Represents an event in the AutoFlow event-driven architecture.
 * All system changes are represented as Events that flow through the EventBus.
 */
data class Event(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: Map<String, Any> = emptyMap()
) {
    companion object {
        // Battery events
        const val TYPE_BATTERY_LEVEL_CHANGED = "battery_level_changed"
        const val TYPE_BATTERY_CHARGING = "battery_charging"
        const val TYPE_BATTERY_DISCHARGING = "battery_discharging"

        // WiFi events
        const val TYPE_WIFI_CONNECTED = "wifi_connected"
        const val TYPE_WIFI_DISCONNECTED = "wifi_disconnected"
        const val TYPE_WIFI_NETWORK_CHANGED = "wifi_network_changed"

        // Bluetooth events
        const val TYPE_BLUETOOTH_CONNECTED = "bluetooth_connected"
        const val TYPE_BLUETOOTH_DEVICE_CONNECTED = "bluetooth_device_connected"
        const val TYPE_BLUETOOTH_DEVICE_DISCONNECTED = "bluetooth_device_disconnected"

        // Phone events
        const val TYPE_INCOMING_CALL = "incoming_call"
        const val TYPE_MISSED_CALL = "missed_call"
        const val TYPE_CALL_ENDED = "call_ended"

        // NFC events
        const val TYPE_NFC_TAG_SCANNED = "nfc_tag_scanned"

        // Time events
        const val TYPE_TIME_REACHED = "time_reached"

        // Location events
        const val TYPE_LOCATION_CHANGED = "location_changed"

        // App events
        const val TYPE_APP_OPENED = "app_opened"

        // Payload keys
        const val KEY_BATTERY_LEVEL = "battery_level"
        const val KEY_BATTERY_IS_CHARGING = "is_charging"
        const val KEY_WIFI_SSID = "ssid"
        const val KEY_BLUETOOTH_DEVICE_NAME = "device_name"
        const val KEY_BLUETOOTH_DEVICE_ADDRESS = "device_address"
        const val KEY_PHONE_NUMBER = "phone_number"
        const val KEY_NFC_TAG_ID = "tag_id"
        const val KEY_NFC_PAYLOAD = "nfc_payload"
        const val KEY_TIME = "time"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_VOLUME_LEVEL = "volume_level"
    }

    fun getStringPayload(key: String): String? = payload[key] as? String
    fun getIntPayload(key: String): Int? = payload[key] as? Int
    fun getBooleanPayload(key: String): Boolean? = payload[key] as? Boolean
    fun getDoublePayload(key: String): Double? = payload[key] as? Double
}
