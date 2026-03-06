package com.autoflow.app

import android.app.Application
import android.content.Intent
import android.util.Log
import com.autoflow.app.data.database.AppDatabase
import com.autoflow.app.engine.AutoFlowService

class AutoFlowApplication : Application() {

    companion object {
        private const val TAG = "AutoFlowApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AutoFlow application started")

        // Initialize database
        AppDatabase.getInstance(this)

        // Start foreground service
        startAutoFlowService()
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
