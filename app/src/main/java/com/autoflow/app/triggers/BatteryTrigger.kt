package com.autoflow.app.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine

class BatteryTrigger(private val appContext: Context) : TriggerListener {

    companion object {
        private const val TAG = "BatteryTrigger"
    }

    private var receiver: BroadcastReceiver? = null

    override fun startListening(context: Context) {
        Log.d(TAG, "Starting battery trigger listener")

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        val batteryPct = (level * 100) / scale
                        Log.d(TAG, "Battery level: $batteryPct%")
                        RuleEngine.getInstance(appContext)
                            .onTrigger(Trigger.TYPE_BATTERY_LEVEL, batteryPct.toString())
                    }
                }
            }
        }

        appContext.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun stopListening() {
        Log.d(TAG, "Stopping battery trigger listener")
        try {
            receiver?.let { appContext.unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering battery receiver", e)
        }
        receiver = null
    }
}
