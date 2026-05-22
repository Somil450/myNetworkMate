package com.networkmate.app.data.repository

import com.networkmate.app.data.local.SignalDao
import com.networkmate.app.data.local.SignalReadingEntity
import com.networkmate.app.data.local.SpeedTestEntity
import com.networkmate.app.data.remote.SpeedTestEngine
import com.networkmate.app.data.telephony.TelephonyTracker
import com.networkmate.app.domain.model.TowerInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.*

@Singleton
class NetworkRepository @Inject constructor(
    private val telephonyTracker: TelephonyTracker,
    private val signalDao: SignalDao,
    private val speedTestEngine: SpeedTestEngine
) {
    val connectedTower = telephonyTracker.connectedTower
    val nearbyTowers = telephonyTracker.towerInfoList

    suspend fun saveCurrentSignal() {
        connectedTower.value?.let { tower ->
            val reading = SignalReadingEntity(
                towerId = tower.id,
                carrier = tower.carrier,
                networkType = tower.type,
                dbm = tower.signalStrength,
                level = tower.signalLevel,
                lat = tower.lat ?: 0.0,
                lng = tower.lng ?: 0.0
            )
            signalDao.insertSignal(reading)
        }
    }

    fun runSpeedTest(): Flow<SpeedResultState> = flow {
        emit(SpeedResultState.Starting)
        
        val latency = speedTestEngine.measureLatency()
        emit(SpeedResultState.LatencyMeasured(latency.pingMs, latency.jitterMs))
        
        speedTestEngine.runDownloadTest().collect { result ->
            when (result) {
                is com.networkmate.app.data.remote.SpeedResult.Progress -> {
                    emit(SpeedResultState.Downloading(result.speedMbps))
                }
                is com.networkmate.app.data.remote.SpeedResult.Completed -> {
                    emit(SpeedResultState.Finished(result.speedMbps, latency.pingMs))
                    saveSpeedTest(result.speedMbps, latency.pingMs, latency.jitterMs)
                }
                is com.networkmate.app.data.remote.SpeedResult.Error -> {
                    emit(SpeedResultState.Error(result.message))
                }
            }
        }
    }

    private suspend fun saveSpeedTest(speed: Double, ping: Int, jitter: Int) {
        connectedTower.value?.let { tower ->
            val test = SpeedTestEntity(
                downloadSpeed = speed,
                uploadSpeed = speed * 0.2, // Rough estimate for now
                latency = ping,
                jitter = jitter,
                towerId = tower.id,
                carrier = tower.carrier
            )
            signalDao.insertSpeedTest(test)
        }
    }

    fun calculateQualityScores(tower: TowerInfo, ping: Int): QualityScores {
        val gamingScore = when {
            ping < 30 && tower.signalLevel >= 3 -> "Excellent"
            ping < 60 && tower.signalLevel >= 2 -> "Good"
            else -> "Poor"
        }
        
        val streamingScore = when {
            tower.estimatedSpeedMbps > 25 -> "Excellent (4K)"
            tower.estimatedSpeedMbps > 10 -> "Good (HD)"
            else -> "Moderate"
        }

        return QualityScores(gamingScore, streamingScore, "Stable")
    }
}

sealed class SpeedResultState {
    object Starting : SpeedResultState()
    data class LatencyMeasured(val ping: Int, val jitter: Int) : SpeedResultState()
    data class Downloading(val speed: Double) : SpeedResultState()
    data class Finished(val speed: Double, val ping: Int) : SpeedResultState()
    data class Error(val message: String) : SpeedResultState()
}

data class QualityScores(
    val gaming: String,
    val streaming: String,
    val stability: String
)
