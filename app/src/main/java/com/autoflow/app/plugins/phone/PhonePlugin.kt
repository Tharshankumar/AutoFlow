package com.autoflow.app.plugins.phone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.os.Build
import android.util.Log
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.Event
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class PhonePlugin : AutomationPlugin {

    companion object {
        private const val TAG = "PhonePlugin"
    }

    override val pluginId = "phone"
    override val pluginName = "Phone"

    private var context: Context? = null
    private var eventBus: EventBus? = null
    private var telephonyManager: TelephonyManager? = null
    @Suppress("DEPRECATION")
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null
    private var lastCallState = TelephonyManager.CALL_STATE_IDLE
    private var wasRinging = false

    @Suppress("DEPRECATION")
    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        this.eventBus = eventBus

        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    handleCallStateChanged(state)
                }
            }
            try {
                telephonyManager?.registerTelephonyCallback(
                    context.mainExecutor,
                    telephonyCallback!!
                )
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot register telephony callback - permission denied", e)
            }
        } else {
            phoneStateListener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    handleCallStateChanged(state, phoneNumber)
                }
            }
            try {
                telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot listen to phone state - permission denied", e)
            }
        }

        Log.d(TAG, "Phone plugin initialized")
    }

    private fun handleCallStateChanged(state: Int, phoneNumber: String? = null) {
        val eb = eventBus ?: return

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                wasRinging = true
                Log.d(TAG, "Incoming call: $phoneNumber")
                eb.publish(Event(
                    type = Event.TYPE_INCOMING_CALL,
                    payload = buildMap {
                        put(Event.KEY_PHONE_NUMBER, phoneNumber ?: "unknown")
                    }
                ))
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (wasRinging && lastCallState == TelephonyManager.CALL_STATE_RINGING) {
                    Log.d(TAG, "Missed call")
                    eb.publish(Event(
                        type = Event.TYPE_MISSED_CALL,
                        payload = mapOf(Event.KEY_PHONE_NUMBER to (phoneNumber ?: "unknown"))
                    ))
                }
                if (lastCallState != TelephonyManager.CALL_STATE_IDLE) {
                    Log.d(TAG, "Call ended")
                    eb.publish(Event(
                        type = Event.TYPE_CALL_ENDED,
                        payload = mapOf(Event.KEY_PHONE_NUMBER to (phoneNumber ?: "unknown"))
                    ))
                }
                wasRinging = false
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                wasRinging = false
            }
        }
        lastCallState = state
    }

    override fun registerTriggers(): List<TriggerDefinition> = listOf(
        TriggerDefinition(
            type = "incoming_call",
            displayName = "Incoming Call",
            description = "Triggers when an incoming call is received",
            valueHint = "phone number or 'any'"
        ),
        TriggerDefinition(
            type = "missed_call",
            displayName = "Missed Call",
            description = "Triggers when a call is missed",
            valueHint = "phone number or 'any'"
        ),
        TriggerDefinition(
            type = "call_ended",
            displayName = "Call Ended",
            description = "Triggers when a call ends",
            valueHint = "any"
        )
    )

    override fun registerConditions(): List<ConditionDefinition> = emptyList()

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "send_sms",
            displayName = "Send SMS",
            description = "Send an SMS message",
            valueHint = "phone_number|message"
        ),
        ActionDefinition(
            type = "auto_reply",
            displayName = "Auto Reply",
            description = "Auto-reply with an SMS",
            valueHint = "message text"
        ),
        ActionDefinition(
            type = "reject_call",
            displayName = "Reject Call",
            description = "Reject incoming call"
        )
    )

    fun executeAction(context: Context, actionType: String, value: String) {
        when (actionType) {
            "send_sms" -> {
                val parts = value.split("|", limit = 2)
                val phoneNumber = parts.getOrElse(0) { "" }
                val message = parts.getOrElse(1) { "" }
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$phoneNumber")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "SMS intent opened for: $phoneNumber")
            }
            "auto_reply" -> {
                Log.d(TAG, "Auto-reply configured: $value")
            }
            "reject_call" -> {
                Log.d(TAG, "Reject call action triggered")
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun shutdown() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyCallback?.let { telephonyManager?.unregisterTelephonyCallback(it) }
            } else {
                phoneStateListener?.let { telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down phone plugin", e)
        }
        telephonyCallback = null
        phoneStateListener = null
        Log.d(TAG, "Phone plugin shut down")
    }
}
