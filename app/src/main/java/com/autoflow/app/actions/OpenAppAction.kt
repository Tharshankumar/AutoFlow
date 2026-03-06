package com.autoflow.app.actions

import android.content.Context
import android.content.Intent
import android.util.Log

class OpenAppAction {

    companion object {
        private const val TAG = "OpenAppAction"
    }

    fun execute(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Log.d(TAG, "Opened app: $packageName")
        } else {
            Log.w(TAG, "App not found: $packageName")
        }
    }
}
