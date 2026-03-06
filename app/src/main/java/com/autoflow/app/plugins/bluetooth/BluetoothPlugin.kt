package com.autoflow.app.plugins.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.util.Log
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.Event
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class BluetoothPlugin : AutomationPlugin {

    companion object {
        private const val TAG = "BluetoothPlugin"
    }

    override val pluginId = "bluetooth"
    override val pluginName = "Bluetooth"

    private var context: Context? = null
    private var eventBus: EventBus? = null
    private var stateManager: StateManager? = null
    private var bluetoothReceiver: BroadcastReceiver? = null

    @Suppress("MissingPermission")
    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        this.eventBus = eventBus
        this.stateManager = stateManager

        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val deviceName = device?.name ?: "unknown"
                        val deviceAddress = device?.address ?: "unknown"
                        Log.d(TAG, "Bluetooth device connected: $deviceName")

                        eventBus.publish(Event(
                            type = Event.TYPE_BLUETOOTH_DEVICE_CONNECTED,
                            payload = mapOf(
                                Event.KEY_BLUETOOTH_DEVICE_NAME to deviceName,
                                Event.KEY_BLUETOOTH_DEVICE_ADDRESS to deviceAddress
                            )
                        ))

                        eventBus.publish(Event(
                            type = Event.TYPE_BLUETOOTH_CONNECTED,
                            payload = mapOf(
                                Event.KEY_BLUETOOTH_DEVICE_NAME to deviceName,
                                Event.KEY_BLUETOOTH_DEVICE_ADDRESS to deviceAddress
                            )
                        ))
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val deviceName = device?.name ?: "unknown"
                        val deviceAddress = device?.address ?: "unknown"
                        Log.d(TAG, "Bluetooth device disconnected: $deviceName")

                        eventBus.publish(Event(
                            type = Event.TYPE_BLUETOOTH_DEVICE_DISCONNECTED,
                            payload = mapOf(
                                Event.KEY_BLUETOOTH_DEVICE_NAME to deviceName,
                                Event.KEY_BLUETOOTH_DEVICE_ADDRESS to deviceAddress
                            )
                        ))
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
        Log.d(TAG, "Bluetooth plugin initialized")
    }

    override fun registerTriggers(): List<TriggerDefinition> = listOf(
        TriggerDefinition(
            type = "bluetooth_connected",
            displayName = "Bluetooth Connected",
            description = "Triggers when any Bluetooth device connects",
            valueHint = "device name or 'any'"
        ),
        TriggerDefinition(
            type = "device_connected",
            displayName = "Device Connected",
            description = "Triggers when a specific device connects",
            valueHint = "device name"
        ),
        TriggerDefinition(
            type = "device_disconnected",
            displayName = "Device Disconnected",
            description = "Triggers when a device disconnects",
            valueHint = "device name or 'any'"
        )
    )

    override fun registerConditions(): List<ConditionDefinition> = emptyList()

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "enable_bluetooth",
            displayName = "Enable Bluetooth",
            description = "Request to enable Bluetooth",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "disable_bluetooth",
            displayName = "Disable Bluetooth",
            description = "Open Bluetooth settings to disable"
        ),
        ActionDefinition(
            type = "toggle_bluetooth",
            displayName = "Toggle Bluetooth",
            description = "Toggle Bluetooth on/off",
            valueHint = "on/off",
            supportsReverse = true
        )
    )

    @Suppress("MissingPermission")
    fun executeAction(context: Context, actionType: String, value: String) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter

        when (actionType) {
            "enable_bluetooth" -> {
                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            "disable_bluetooth" -> {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            "toggle_bluetooth" -> {
                when (value.lowercase()) {
                    "on" -> executeAction(context, "enable_bluetooth", value)
                    "off" -> executeAction(context, "disable_bluetooth", value)
                }
            }
        }
    }

    override fun shutdown() {
        try {
            bluetoothReceiver?.let { context?.unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering Bluetooth receiver", e)
        }
        bluetoothReceiver = null
        Log.d(TAG, "Bluetooth plugin shut down")
    }
}
