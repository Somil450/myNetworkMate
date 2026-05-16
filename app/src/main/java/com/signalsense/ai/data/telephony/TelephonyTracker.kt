package com.signalsense.ai.data.telephony

import android.content.Context
import android.telephony.*
import com.signalsense.ai.domain.model.NetworkType
import com.signalsense.ai.domain.model.TowerInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelephonyTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    private val _towerInfoList = MutableStateFlow<List<TowerInfo>>(emptyList())
    val towerInfoList = _towerInfoList.asStateFlow()

    private val _connectedTower = MutableStateFlow<TowerInfo?>(null)
    val connectedTower = _connectedTower.asStateFlow()

    fun updateTowerData() {
        try {
            val allCellInfo = telephonyManager.allCellInfo ?: return
            val towers = allCellInfo.mapNotNull { cellInfo ->
                parseCellInfo(cellInfo)
            }
            _towerInfoList.value = towers
            _connectedTower.value = towers.find { it.isConnected }
        } catch (e: SecurityException) {
            // Handle missing permissions
        }
    }

    private fun parseCellInfo(info: CellInfo): TowerInfo? {
        val carrier = getCarrierName(telephonyManager.networkOperatorName)
        val isConnected = info.isRegistered
        
        return when (info) {
            is CellInfoLte -> {
                val identity = info.cellIdentity
                val signal = info.cellSignalStrength
                TowerInfo(
                    id = identity.ci.toString(),
                    carrier = carrier,
                    type = NetworkType.FOUR_G,
                    signalStrength = signal.dbm,
                    signalLevel = signal.level,
                    pci = identity.pci,
                    tac = identity.tac,
                    isConnected = isConnected,
                    estimatedSpeedMbps = calculateEstimatedSpeed(signal.dbm, NetworkType.FOUR_G)
                )
            }
            is CellInfoNr -> {
                // 5G NR parsing (API 29+)
                val identity = info.cellIdentity as CellIdentityNr
                val signal = info.cellSignalStrength as CellSignalStrengthNr
                TowerInfo(
                    id = identity.nci.toString(),
                    carrier = carrier,
                    type = NetworkType.FIVE_G,
                    signalStrength = signal.dbm,
                    signalLevel = signal.level,
                    pci = identity.pci,
                    tac = identity.tac,
                    isConnected = isConnected,
                    estimatedSpeedMbps = calculateEstimatedSpeed(signal.dbm, NetworkType.FIVE_G)
                )
            }
            else -> null
        }
    }

    private fun getCarrierName(operatorName: String): String {
        return when {
            operatorName.contains("Jio", true) -> "Jio"
            operatorName.contains("Airtel", true) -> "Airtel"
            operatorName.contains("Vi", true) || operatorName.contains("Vodafone", true) -> "Vi"
            operatorName.contains("BSNL", true) -> "BSNL"
            else -> operatorName.ifEmpty { "Unknown" }
        }
    }

    private fun calculateEstimatedSpeed(dbm: Int, type: NetworkType): Double {
        // Simple heuristic for estimation based on signal strength
        val baseSpeed = if (type == NetworkType.FIVE_G) 200.0 else 50.0
        val factor = (dbm + 140.0) / 100.0 // Normalizing -140dBm (0) to -40dBm (1)
        return (baseSpeed * factor.coerceIn(0.1, 1.2)).coerceAtLeast(1.0)
    }
}
