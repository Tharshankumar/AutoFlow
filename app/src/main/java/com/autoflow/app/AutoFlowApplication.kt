package com.autoflow.app

import android.app.Application
import android.content.Intent
import android.util.Log
import com.autoflow.app.core.AutomationEngine
import com.autoflow.app.core.PluginRegistry
import com.autoflow.app.data.database.AppDatabase
import com.autoflow.app.engine.AutoFlowService
import com.autoflow.app.plugins.battery.BatteryPlugin
import com.autoflow.app.plugins.bluetooth.BluetoothPlugin
import com.autoflow.app.plugins.camera.CameraPlugin
import com.autoflow.app.plugins.internet.InternetControlPlugin
import com.autoflow.app.plugins.music.MusicPlugin
import com.autoflow.app.plugins.nfc.NfcPlugin
import com.autoflow.app.plugins.phone.PhonePlugin
import com.autoflow.app.plugins.wifi.WifiPlugin

class AutoFlowApplication : Application() {

    companion object {
        private const val TAG = "AutoFlowApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AutoFlow application started")

        // Initialize database
        AppDatabase.getInstance(this)

        // Initialize AutomationEngine
        AutomationEngine.getInstance(this)
        Log.d(TAG, "AutomationEngine initialized")

        // Register all plugins
        registerPlugins()

        // Initialize all plugins
        PluginRegistry.getInstance().initializeAll(this)
        Log.d(TAG, "All plugins initialized")

        // Start foreground service
        startAutoFlowService()
    }

    private fun registerPlugins() {
        val registry = PluginRegistry.getInstance()

        registry.registerPlugin(BatteryPlugin())
        registry.registerPlugin(WifiPlugin())
        registry.registerPlugin(BluetoothPlugin())
        registry.registerPlugin(MusicPlugin())
        registry.registerPlugin(PhonePlugin())
        registry.registerPlugin(NfcPlugin())
        registry.registerPlugin(CameraPlugin())
        registry.registerPlugin(InternetControlPlugin())

        Log.d(TAG, "Registered ${registry.getAllPlugins().size} plugins")
    }

    private fun startAutoFlowService() {
        try {
            val serviceIntent = Intent(this, AutoFlowService::class.java)
            startForegroundService(serviceIntent)
            Log.d(TAG, "AutoFlow service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AutoFlow service", e)
        }
    }
}
