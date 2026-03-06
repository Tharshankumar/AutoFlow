package com.autoflow.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine

class BatteryReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BatteryReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            if (level >= 0 && scale > 0) {
                val batteryPct = (level * 100) / scale
                Log.d(TAG, "Battery changed: $batteryPct%")
                RuleEngine.getInstance(context)
                    .onTrigger(Trigger.TYPE_BATTERY_LEVEL, batteryPct.toString())
            }
        }
    }
}
