package com.autoflow.app.core

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe event bus for the AutoFlow event-driven architecture.
 * All system triggers publish events through this bus, and the AutomationEngine subscribes to process them.
 */
class EventBus private constructor() {

    companion object {
        private const val TAG = "EventBus"

        @Volatile
        private var INSTANCE: EventBus? = null

        fun getInstance(): EventBus {
            return INSTANCE ?: synchronized(this) {
                val instance = EventBus()
                INSTANCE = instance
                instance
            }
        }
    }

    fun interface EventListener {
        fun onEvent(event: Event)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val listeners = ConcurrentHashMap<String, MutableSet<EventListener>>()
    private val globalListeners = ConcurrentHashMap.newKeySet<EventListener>()

    private val _eventFlow = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    val eventFlow: SharedFlow<Event> = _eventFlow.asSharedFlow()

    /**
     * Subscribe to a specific event type.
     */
    fun subscribe(eventType: String, listener: EventListener) {
        listeners.getOrPut(eventType) { ConcurrentHashMap.newKeySet() }.add(listener)
        Log.d(TAG, "Listener subscribed to: $eventType")
    }

    /**
     * Subscribe to all events.
     */
    fun subscribeAll(listener: EventListener) {
        globalListeners.add(listener)
        Log.d(TAG, "Global listener subscribed")
    }

    /**
     * Unsubscribe a listener from a specific event type.
     */
    fun unsubscribe(eventType: String, listener: EventListener) {
        listeners[eventType]?.remove(listener)
        Log.d(TAG, "Listener unsubscribed from: $eventType")
    }

    /**
     * Unsubscribe a listener from all events.
     */
    fun unsubscribeAll(listener: EventListener) {
        globalListeners.remove(listener)
        listeners.values.forEach { it.remove(listener) }
        Log.d(TAG, "Listener unsubscribed from all events")
    }

    /**
     * Publish an event to all subscribers.
     */
    fun publish(event: Event) {
        Log.d(TAG, "Publishing event: type=${event.type}, payload=${event.payload}")

        scope.launch {
            _eventFlow.emit(event)
        }

        // Notify type-specific listeners
        listeners[event.type]?.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error delivering event to listener: ${event.type}", e)
            }
        }

        // Notify global listeners
        globalListeners.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error delivering event to global listener", e)
            }
        }
    }

    /**
     * Remove all listeners.
     */
    fun clear() {
        listeners.clear()
        globalListeners.clear()
        Log.d(TAG, "All listeners cleared")
    }
}
