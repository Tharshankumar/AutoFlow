package com.autoflow.app.engine

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.autoflow.app.receivers.BatteryReceiver
import com.autoflow.app.receivers.BluetoothReceiver
import com.autoflow.app.receivers.WifiReceiver
import com.autoflow.app.triggers.BatteryTrigger
import com.autoflow.app.triggers.BluetoothTrigger
import com.autoflow.app.triggers.LocationTrigger
import com.autoflow.app.triggers.TimeTrigger
import com.autoflow.app.triggers.TriggerListener
import com.autoflow.app.triggers.WifiTrigger

class TriggerManager(private val context: Context) {

    companion object {
        private const val TAG = "TriggerManager"
    }

    private val triggers = mutableListOf<TriggerListener>()
    private var batteryReceiver: BatteryReceiver? = null
    private var wifiReceiver: WifiReceiver? = null
    private var bluetoothReceiver: BluetoothReceiver? = null

    fun startAllListeners() {
        Log.d(TAG, "Starting all trigger listeners")
        stopAllListeners()

        val ruleEngine = RuleEngine.getInstance(context)

        // Battery trigger
        val batteryTrigger = BatteryTrigger(context)
        batteryTrigger.startListening(context)
        triggers.add(batteryTrigger)

        // WiFi trigger
        val wifiTrigger = WifiTrigger(context)
        wifiTrigger.startListening(context)
        triggers.add(wifiTrigger)

        // Bluetooth trigger
        val bluetoothTrigger = BluetoothTrigger(context)
        bluetoothTrigger.startListening(context)
        triggers.add(bluetoothTrigger)

        // Time trigger
        val timeTrigger = TimeTrigger(context)
        timeTrigger.startListening(context)
        triggers.add(timeTrigger)

        // Location trigger
        val locationTrigger = LocationTrigger(context)
        locationTrigger.startListening(context)
        triggers.add(locationTrigger)

        // Register broadcast receivers
        registerReceivers()

        Log.d(TAG, "All trigger listeners started")
    }

    fun stopAllListeners() {
        Log.d(TAG, "Stopping all trigger listeners")
        triggers.forEach { it.stopListening() }
        triggers.clear()
        unregisterReceivers()
    }

    private fun registerReceivers() {
        try {
            batteryReceiver = BatteryReceiver().also {
                context.registerReceiver(it, IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            }

            wifiReceiver = WifiReceiver().also {
                val filter = IntentFilter().apply {
                    addAction("android.net.conn.CONNECTIVITY_CHANGE")
                    addAction("android.net.wifi.STATE_CHANGE")
                }
                context.registerReceiver(it, filter)
            }

            bluetoothReceiver = BluetoothReceiver().also {
                val filter = IntentFilter().apply {
                    addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")
                    addAction("android.bluetooth.device.action.ACL_CONNECTED")
                    addAction("android.bluetooth.device.action.ACL_DISCONNECTED")
                }
                context.registerReceiver(it, filter)
            }

            Log.d(TAG, "Broadcast receivers registered")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering receivers", e)
        }
    }

    private fun unregisterReceivers() {
        try {
            batteryReceiver?.let { context.unregisterReceiver(it) }
            wifiReceiver?.let { context.unregisterReceiver(it) }
            bluetoothReceiver?.let { context.unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers", e)
        }
        batteryReceiver = null
        wifiReceiver = null
        bluetoothReceiver = null
    }
}
