package com.autoflow.app.plugins.nfc

import android.content.Context
import android.nfc.NfcAdapter
import android.util.Log
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.Event
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class NfcPlugin : AutomationPlugin {

    companion object {
        private const val TAG = "NfcPlugin"
    }

    override val pluginId = "nfc"
    override val pluginName = "NFC"

    private var context: Context? = null
    private var eventBus: EventBus? = null

    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        this.eventBus = eventBus
        Log.d(TAG, "NFC plugin initialized")
    }

    /**
     * Called from Activity when an NFC tag is scanned.
     * NFC tag reading requires Activity foreground dispatch.
     */
    fun onNfcTagScanned(tagId: String, payload: String = "") {
        eventBus?.publish(Event(
            type = Event.TYPE_NFC_TAG_SCANNED,
            payload = mapOf(
                Event.KEY_NFC_TAG_ID to tagId,
                Event.KEY_NFC_PAYLOAD to payload
            )
        ))
        Log.d(TAG, "NFC tag scanned: $tagId")
    }

    override fun registerTriggers(): List<TriggerDefinition> = listOf(
        TriggerDefinition(
            type = "nfc_tag_scanned",
            displayName = "NFC Tag Scanned",
            description = "Triggers when an NFC tag is scanned",
            valueHint = "tag ID or 'any'"
        )
    )

    override fun registerConditions(): List<ConditionDefinition> = emptyList()

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "execute_routine",
            displayName = "Execute Routine",
            description = "Execute a specific routine by name",
            valueHint = "routine name"
        )
    )

    fun executeAction(context: Context, actionType: String, value: String) {
        when (actionType) {
            "execute_routine" -> {
                Log.d(TAG, "Execute routine action: $value")
            }
        }
    }

    override fun shutdown() {
        Log.d(TAG, "NFC plugin shut down")
    }
}
