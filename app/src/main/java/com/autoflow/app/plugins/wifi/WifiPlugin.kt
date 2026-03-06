package com.autoflow.app.plugins.wifi

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.Event
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class WifiPlugin : AutomationPlugin {

    companion object {
        private const val TAG = "WifiPlugin"
    }

    override val pluginId = "wifi"
    override val pluginName = "WiFi"

    private var context: Context? = null
    private var eventBus: EventBus? = null
    private var stateManager: StateManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var lastSsid: String? = null

    @Suppress("DEPRECATION")
    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        this.eventBus = eventBus
        this.stateManager = stateManager

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    val wifiManager = context.applicationContext
                        .getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val ssid = wifiManager?.connectionInfo?.ssid?.removeSurrounding("\"") ?: "unknown"

                    Log.d(TAG, "WiFi connected: $ssid")

                    eventBus.publish(Event(
                        type = Event.TYPE_WIFI_CONNECTED,
                        payload = mapOf(Event.KEY_WIFI_SSID to ssid)
                    ))

                    if (lastSsid != null && lastSsid != ssid) {
                        eventBus.publish(Event(
                            type = Event.TYPE_WIFI_NETWORK_CHANGED,
                            payload = mapOf(Event.KEY_WIFI_SSID to ssid)
                        ))
                    }
                    lastSsid = ssid
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "WiFi disconnected")

                eventBus.publish(Event(
                    type = Event.TYPE_WIFI_DISCONNECTED,
                    payload = mapOf(Event.KEY_WIFI_SSID to (lastSsid ?: ""))
                ))
                lastSsid = null
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
        Log.d(TAG, "WiFi plugin initialized")
    }

    override fun registerTriggers(): List<TriggerDefinition> = listOf(
        TriggerDefinition(
            type = "wifi_connected",
            displayName = "WiFi Connected",
            description = "Triggers when WiFi connects",
            valueHint = "SSID or 'any'"
        ),
        TriggerDefinition(
            type = "wifi_disconnected",
            displayName = "WiFi Disconnected",
            description = "Triggers when WiFi disconnects",
            valueHint = "any"
        ),
        TriggerDefinition(
            type = "wifi_network_changed",
            displayName = "WiFi Network Changed",
            description = "Triggers when WiFi network changes",
            valueHint = "new SSID or 'any'"
        )
    )

    override fun registerConditions(): List<ConditionDefinition> = listOf(
        ConditionDefinition(
            type = "ssid_matches",
            displayName = "SSID Matches",
            description = "Connected to a specific WiFi network",
            valueHint = "e.g., HomeWiFi"
        ),
        ConditionDefinition(
            type = "connected_to_wifi",
            displayName = "Connected to WiFi",
            description = "Device is connected to any WiFi network",
            valueHint = "true/false"
        ),
        ConditionDefinition(
            type = "wifi_network",
            displayName = "WiFi Network",
            description = "Connected to a specific network",
            valueHint = "e.g., OfficeWiFi"
        )
    )

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "enable_wifi",
            displayName = "Enable WiFi",
            description = "Open WiFi settings to enable",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "disable_wifi",
            displayName = "Disable WiFi",
            description = "Open WiFi settings to disable"
        ),
        ActionDefinition(
            type = "toggle_wifi",
            displayName = "Toggle WiFi",
            description = "Toggle WiFi on/off",
            valueHint = "on/off",
            supportsReverse = true
        ),
        ActionDefinition(
            type = "connect_wifi",
            displayName = "Connect to WiFi",
            description = "Connect to a specific WiFi network",
            valueHint = "SSID"
        ),
        ActionDefinition(
            type = "disconnect_wifi",
            displayName = "Disconnect WiFi",
            description = "Disconnect from current WiFi"
        )
    )

    override fun shutdown() {
        try {
            networkCallback?.let {
                val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                connectivityManager?.unregisterNetworkCallback(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering WiFi callback", e)
        }
        networkCallback = null
        Log.d(TAG, "WiFi plugin shut down")
    }

    /**
     * Execute a WiFi action.
     */
    fun executeAction(context: Context, actionType: String, value: String) {
        when (actionType) {
            "enable_wifi", "disable_wifi", "toggle_wifi" -> {
                val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "WiFi settings panel opened for: $actionType")
            }
            "connect_wifi" -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "WiFi settings opened to connect: $value")
            }
            "disconnect_wifi" -> {
                val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "WiFi settings opened to disconnect")
            }
        }
    }
}
