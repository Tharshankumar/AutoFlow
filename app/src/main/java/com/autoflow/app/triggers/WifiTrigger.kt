package com.autoflow.app.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine

class WifiTrigger(private val appContext: Context) : TriggerListener {

    companion object {
        private const val TAG = "WifiTrigger"
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    @Suppress("DEPRECATION")
    override fun startListening(context: Context) {
        Log.d(TAG, "Starting WiFi trigger listener")

        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    val wifiManager = appContext.applicationContext
                        .getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val ssid = wifiManager?.connectionInfo?.ssid?.removeSurrounding("\"") ?: "unknown"
                    Log.d(TAG, "WiFi connected: $ssid")
                    RuleEngine.getInstance(appContext)
                        .onTrigger(Trigger.TYPE_WIFI_CONNECTED, ssid)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "WiFi disconnected")
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    override fun stopListening() {
        Log.d(TAG, "Stopping WiFi trigger listener")
        try {
            val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering WiFi callback", e)
        }
        networkCallback = null
    }
}
