package com.autoflow.app.plugins.camera

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import com.autoflow.app.core.ActionDefinition
import com.autoflow.app.core.AutomationPlugin
import com.autoflow.app.core.ConditionDefinition
import com.autoflow.app.core.EventBus
import com.autoflow.app.core.StateManager
import com.autoflow.app.core.TriggerDefinition

class CameraPlugin : AutomationPlugin {

    companion object {
        private const val TAG = "CameraPlugin"
    }

    override val pluginId = "camera"
    override val pluginName = "Camera"

    private var context: Context? = null

    override fun initialize(context: Context, eventBus: EventBus, stateManager: StateManager) {
        this.context = context
        Log.d(TAG, "Camera plugin initialized")
    }

    override fun registerTriggers(): List<TriggerDefinition> = emptyList()

    override fun registerConditions(): List<ConditionDefinition> = emptyList()

    override fun registerActions(): List<ActionDefinition> = listOf(
        ActionDefinition(
            type = "open_camera",
            displayName = "Open Camera",
            description = "Open the camera app"
        ),
        ActionDefinition(
            type = "take_photo",
            displayName = "Take Photo",
            description = "Open camera in photo mode"
        ),
        ActionDefinition(
            type = "record_video",
            displayName = "Record Video",
            description = "Open camera in video mode"
        )
    )

    fun executeAction(context: Context, actionType: String, value: String) {
        when (actionType) {
            "open_camera" -> {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Camera opened")
            }
            "take_photo" -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Photo capture opened")
            }
            "record_video" -> {
                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Video capture opened")
            }
        }
    }

    override fun shutdown() {
        Log.d(TAG, "Camera plugin shut down")
    }
}
