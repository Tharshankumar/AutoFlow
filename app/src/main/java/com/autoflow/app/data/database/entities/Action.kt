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
        const val TYPE_ENABLE_WIFI = "enable_wifi"
        const val TYPE_DISABLE_WIFI = "disable_wifi"
        const val TYPE_CONNECT_WIFI = "connect_wifi"
        const val TYPE_DISCONNECT_WIFI = "disconnect_wifi"
        const val TYPE_ENABLE_BLUETOOTH = "enable_bluetooth"
        const val TYPE_DISABLE_BLUETOOTH = "disable_bluetooth"
        const val TYPE_ENABLE_BATTERY_SAVER = "enable_battery_saver"
        const val TYPE_DISABLE_BATTERY_SAVER = "disable_battery_saver"
        const val TYPE_PLAY_MUSIC = "play_music"
        const val TYPE_PAUSE_MUSIC = "pause_music"
        const val TYPE_NEXT_TRACK = "next_track"
        const val TYPE_PREVIOUS_TRACK = "previous_track"
        const val TYPE_PLAY_PLAYLIST = "play_playlist"
        const val TYPE_SEND_SMS = "send_sms"
        const val TYPE_AUTO_REPLY = "auto_reply"
        const val TYPE_REJECT_CALL = "reject_call"
        const val TYPE_EXECUTE_ROUTINE = "execute_routine"
        const val TYPE_OPEN_CAMERA = "open_camera"
        const val TYPE_TAKE_PHOTO = "take_photo"
        const val TYPE_RECORD_VIDEO = "record_video"
        const val TYPE_ENABLE_MOBILE_DATA = "enable_mobile_data"
        const val TYPE_DISABLE_MOBILE_DATA = "disable_mobile_data"
        const val TYPE_ENABLE_AIRPLANE_MODE = "enable_airplane_mode"
        const val TYPE_DISABLE_AIRPLANE_MODE = "disable_airplane_mode"
        const val TYPE_WAIT_SECONDS = "wait_seconds"
        const val TYPE_WAIT_MINUTES = "wait_minutes"

        val ALL_TYPES = listOf(
            TYPE_SET_VOLUME,
            TYPE_TOGGLE_WIFI,
            TYPE_TOGGLE_BLUETOOTH,
            TYPE_OPEN_APP,
            TYPE_SHOW_NOTIFICATION,
            TYPE_ENABLE_WIFI,
            TYPE_DISABLE_WIFI,
            TYPE_CONNECT_WIFI,
            TYPE_DISCONNECT_WIFI,
            TYPE_ENABLE_BLUETOOTH,
            TYPE_DISABLE_BLUETOOTH,
            TYPE_ENABLE_BATTERY_SAVER,
            TYPE_DISABLE_BATTERY_SAVER,
            TYPE_PLAY_MUSIC,
            TYPE_PAUSE_MUSIC,
            TYPE_NEXT_TRACK,
            TYPE_PREVIOUS_TRACK,
            TYPE_PLAY_PLAYLIST,
            TYPE_SEND_SMS,
            TYPE_AUTO_REPLY,
            TYPE_REJECT_CALL,
            TYPE_EXECUTE_ROUTINE,
            TYPE_OPEN_CAMERA,
            TYPE_TAKE_PHOTO,
            TYPE_RECORD_VIDEO,
            TYPE_ENABLE_MOBILE_DATA,
            TYPE_DISABLE_MOBILE_DATA,
            TYPE_ENABLE_AIRPLANE_MODE,
            TYPE_DISABLE_AIRPLANE_MODE,
            TYPE_WAIT_SECONDS,
            TYPE_WAIT_MINUTES
        )
    }
}
