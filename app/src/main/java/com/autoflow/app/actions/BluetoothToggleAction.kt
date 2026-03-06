package com.autoflow.app.actions

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

class BluetoothToggleAction {

    companion object {
        private const val TAG = "BluetoothToggleAction"
    }

    @Suppress("MissingPermission")
    fun execute(context: Context, value: String) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter

        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth not available on this device")
            return
        }

        when (value.lowercase()) {
            "on" -> {
                if (!bluetoothAdapter.isEnabled) {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            "off" -> {
                // On Android 13+, apps cannot programmatically disable Bluetooth
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
        Log.d(TAG, "Bluetooth toggle: $value")
    }
}
