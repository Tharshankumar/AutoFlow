package com.autoflow.app.triggers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.engine.RuleEngine
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationTrigger(private val appContext: Context) : TriggerListener {

    companion object {
        private const val TAG = "LocationTrigger"
        private const val UPDATE_INTERVAL_MS = 60000L
        private const val FASTEST_INTERVAL_MS = 30000L
    }

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    override fun startListening(context: Context) {
        Log.d(TAG, "Starting location trigger listener")

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permission not granted")
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val locationValue = "${location.latitude},${location.longitude}"
                    Log.d(TAG, "Location update: $locationValue")
                    RuleEngine.getInstance(appContext)
                        .onTrigger(Trigger.TYPE_LOCATION, locationValue)
                }
            }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            UPDATE_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .build()

        try {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting location updates", e)
        }
    }

    override fun stopListening() {
        Log.d(TAG, "Stopping location trigger listener")
        try {
            locationCallback?.let {
                fusedLocationClient?.removeLocationUpdates(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates", e)
        }
        fusedLocationClient = null
        locationCallback = null
    }
}
