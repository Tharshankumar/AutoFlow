package com.autoflow.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.autoflow.app.engine.AutoFlowService

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting AutoFlow service")
            val serviceIntent = Intent(context, AutoFlowService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
