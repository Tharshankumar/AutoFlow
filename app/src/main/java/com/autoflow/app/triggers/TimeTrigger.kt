package com.autoflow.app.triggers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TimeTrigger(private val appContext: Context) : TriggerListener {

    companion object {
        private const val TAG = "TimeTrigger"
        private const val CHECK_INTERVAL_SECONDS = 60L
    }

    private var scheduledFuture: ScheduledFuture<*>? = null
    private val executor = Executors.newSingleThreadScheduledExecutor()

    override fun startListening(context: Context) {
        Log.d(TAG, "Starting time trigger listener")

        scheduledFuture = executor.scheduleAtFixedRate({
            val now = Calendar.getInstance()
            val currentTime = String.format(
                "%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE)
            )
            Log.d(TAG, "Time check: $currentTime")
            RuleEngine.getInstance(appContext)
                .onTrigger(Trigger.TYPE_TIME, currentTime)
        }, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS)
    }

    override fun stopListening() {
        Log.d(TAG, "Stopping time trigger listener")
        scheduledFuture?.cancel(false)
        scheduledFuture = null
    }
}
