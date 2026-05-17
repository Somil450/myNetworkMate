package com.signalsense.ai.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class UserLocation(val lat: Double, val lng: Double)

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow<UserLocation?>(null)
    val location = _location.asStateFlow()

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 5000L
    ).setMinUpdateDistanceMeters(10f).build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                _location.value = UserLocation(it.latitude, it.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        // Also get last known immediately
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let { _location.value = UserLocation(it.latitude, it.longitude) }
        }
    }

    fun stopTracking() {
        fusedClient.removeLocationUpdates(callback)
    }
}
