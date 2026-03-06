package com.autoflow.app.actions

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

class WifiToggleAction {

    companion object {
        private const val TAG = "WifiToggleAction"
    }

    fun execute(context: Context, value: String) {
        // On Android 10+, apps cannot directly toggle WiFi
        // Open the WiFi settings panel for the user
        val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Log.d(TAG, "WiFi settings panel opened (requested: $value)")
    }
}
