package com.signalsense.ai.domain.usecase

import com.signalsense.ai.data.local.SignalDao
import com.signalsense.ai.data.local.SignalReadingEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeatmapProcessor @Inject constructor(
    private val signalDao: SignalDao
) {
    suspend fun generateHeatPoints(
        minLat: Double, maxLat: Double, 
        minLng: Double, maxLng: Double,
        carrier: String? = null
    ): List<HeatPoint> {
        val signals = signalDao.getSignalsInArea(minLat, maxLat, minLng, maxLng)
        val filtered = if (carrier != null) signals.filter { it.carrier == carrier } else signals

        // Grid-based aggregation for performance
        val grid = mutableMapOf<Pair<Int, Int>, MutableList<SignalReadingEntity>>()
        val gridSize = 0.0001 // Approx 10 meters

        filtered.forEach { signal ->
            val latIdx = (signal.lat / gridSize).toInt()
            val lngIdx = (signal.lng / gridSize).toInt()
            grid.getOrPut(latIdx to lngIdx) { mutableListOf() }.add(signal)
        }

        return grid.map { (pos, readings) ->
            val avgDbm = readings.map { it.dbm }.average()
            HeatPoint(
                lat = pos.first * gridSize,
                lng = pos.second * gridSize,
                intensity = normalizeIntensity(avgDbm),
                carrier = readings.first().carrier
            )
        }
    }

    private fun normalizeIntensity(dbm: Double): Float {
        // -110dBm (0.0) to -60dBm (1.0)
        return ((dbm + 110) / 50.0).toFloat().coerceIn(0f, 1f)
    }
}

data class HeatPoint(
    val lat: Double,
    val lng: Double,
    val intensity: Float,
    val carrier: String
)
