package com.autoflow.app.triggers

import android.content.Context

interface TriggerListener {
    fun startListening(context: Context)
    fun stopListening()
}
