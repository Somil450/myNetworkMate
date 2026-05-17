package com.signalsense.ai.data.remote

import com.signalsense.ai.data.location.LocationTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCelliDRepository @Inject constructor(
    private val api: OpenCelliDApi,
    private val locationTracker: LocationTracker
) {
    // IMPORTANT: Replace with your real key from https://opencellid.org/
    // It's FREE — just register and you get instant access
    companion object {
        const val API_KEY = "pk.YOUR_API_KEY_HERE"
        const val RADIUS_DEG = 0.05 // ~5.5km radius around you
    }

    fun getTowersNearUser(): Flow<List<OpenCelliDCell>> = flow {
        val location = locationTracker.location.value
            ?: run {
                // Fallback to Bangalore if location not available yet
                com.signalsense.ai.data.location.UserLocation(12.9716, 77.5946)
            }

        val minLat = location.lat - RADIUS_DEG
        val minLon = location.lng - RADIUS_DEG
        val maxLat = location.lat + RADIUS_DEG
        val maxLon = location.lng + RADIUS_DEG

        val bbox = "$minLat,$minLon,$maxLat,$maxLon"

        try {
            val response = api.getTowersInArea(API_KEY, bbox)
            emit(response.cells)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}
