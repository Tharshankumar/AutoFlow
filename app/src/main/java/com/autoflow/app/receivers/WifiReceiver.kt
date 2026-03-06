package com.autoflow.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine

class WifiReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WifiReceiver"
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ssid = wifiManager?.connectionInfo?.ssid?.removeSurrounding("\"") ?: "unknown"
            Log.d(TAG, "WiFi connected: $ssid")
            RuleEngine.getInstance(context)
                .onTrigger(Trigger.TYPE_WIFI_CONNECTED, ssid)
        }
    }
}
