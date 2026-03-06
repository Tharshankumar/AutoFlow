package com.autoflow.app.triggers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine

class BluetoothTrigger(private val appContext: Context) : TriggerListener {

    companion object {
        private const val TAG = "BluetoothTrigger"
    }

    private var receiver: BroadcastReceiver? = null

    @Suppress("MissingPermission")
    override fun startListening(context: Context) {
        Log.d(TAG, "Starting Bluetooth trigger listener")

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val deviceName = device?.name ?: "unknown"
                        Log.d(TAG, "Bluetooth device connected: $deviceName")
                        RuleEngine.getInstance(appContext)
                            .onTrigger(Trigger.TYPE_BLUETOOTH_CONNECTED, deviceName)
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val deviceName = device?.name ?: "unknown"
                        Log.d(TAG, "Bluetooth device disconnected: $deviceName")
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        appContext.registerReceiver(receiver, filter)
    }

    override fun stopListening() {
        Log.d(TAG, "Stopping Bluetooth trigger listener")
        try {
            receiver?.let { appContext.unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering Bluetooth receiver", e)
        }
        receiver = null
    }
}
