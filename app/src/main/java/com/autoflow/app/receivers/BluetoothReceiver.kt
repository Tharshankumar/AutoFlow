package com.autoflow.app.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.autoflow.app.core.Event
import com.autoflow.app.core.EventBus
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine

class BluetoothReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BluetoothReceiver"
    }

    @Suppress("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device?.name ?: "unknown"
                val deviceAddress = device?.address ?: "unknown"
                Log.d(TAG, "Bluetooth connected: $deviceName")

                // Publish event through EventBus (new architecture)
                EventBus.getInstance().publish(
                    Event(
                        type = Event.TYPE_BLUETOOTH_DEVICE_CONNECTED,
                        payload = mapOf(
                            Event.KEY_BLUETOOTH_DEVICE_NAME to deviceName,
                            Event.KEY_BLUETOOTH_DEVICE_ADDRESS to deviceAddress
                        )
                    )
                )

                // Legacy: still notify RuleEngine directly for backward compatibility
                RuleEngine.getInstance(context)
                    .onTrigger(Trigger.TYPE_BLUETOOTH_CONNECTED, deviceName)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device?.name ?: "unknown"
                val deviceAddress = device?.address ?: "unknown"
                Log.d(TAG, "Bluetooth disconnected: $deviceName")

                // Publish disconnect event through EventBus
                EventBus.getInstance().publish(
                    Event(
                        type = Event.TYPE_BLUETOOTH_DEVICE_DISCONNECTED,
                        payload = mapOf(
                            Event.KEY_BLUETOOTH_DEVICE_NAME to deviceName,
                            Event.KEY_BLUETOOTH_DEVICE_ADDRESS to deviceAddress
                        )
                    )
                )
            }
        }
    }
}
