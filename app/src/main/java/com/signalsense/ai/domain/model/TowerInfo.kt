package com.signalsense.ai.domain.model

data class TowerInfo(
    val id: String,
    val carrier: String,
    val type: NetworkType,
    val signalStrength: Int, // in dBm
    val signalLevel: Int, // 0 to 4
    val pci: Int?,
    val tac: Int?,
    val isConnected: Boolean,
    val lat: Double? = null,
    val lng: Double? = null,
    val estimatedSpeedMbps: Double = 0.0,
    val congestionEstimate: String = "Low"
)

enum class NetworkType {
    FOUR_G, FIVE_G, THREE_G, TWO_G, UNKNOWN
}

data class CarrierComparison(
    val carrierName: String,
    val avgSpeedMbps: Double,
    val towerCount: Int,
    val bestSignalDbm: Int
)
