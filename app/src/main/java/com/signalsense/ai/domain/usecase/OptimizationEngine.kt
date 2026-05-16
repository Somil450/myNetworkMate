package com.signalsense.ai.domain.usecase

import com.signalsense.ai.domain.model.NetworkType
import com.signalsense.ai.domain.model.TowerInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OptimizationEngine @Inject constructor() {

    fun generateOptimizationAdvice(currentTower: TowerInfo?, nearbyTowers: List<TowerInfo>): OptimizationAdvice {
        if (currentTower == null) return OptimizationAdvice("No Signal", "Toggle Airplane Mode")

        // 1. Gaming / Low-Latency Check
        val betterPingTower = nearbyTowers.find { 
            it.type == NetworkType.FOUR_G && currentTower.type == NetworkType.FIVE_G && it.signalStrength > currentTower.signalStrength + 10
        }
        
        if (betterPingTower != null) {
            return OptimizationAdvice(
                "Gaming Optimization Available",
                "Switch to LTE (4G Only) in system settings. The nearby LTE tower has a stronger signal and likely lower latency than your current weak 5G connection."
            )
        }

        // 2. Congestion & Reconnect Check
        if (currentTower.signalLevel <= 1 && nearbyTowers.any { it.carrier == currentTower.carrier && it.signalLevel >= 3 }) {
            return OptimizationAdvice(
                "Sub-optimal Tower Connected",
                "You are stuck on a weak tower. Toggle Airplane Mode to force a reconnection to a stronger nearby tower."
            )
        }

        // 3. Directional Guidance (Simulated based on nearby tower locations)
        val bestNearby = nearbyTowers.maxByOrNull { it.signalStrength }
        if (bestNearby != null && bestNearby.signalStrength > currentTower.signalStrength + 15) {
            // In a real scenario, calculate bearing from user's current GPS to tower GPS
            return OptimizationAdvice(
                "Stronger Signal Nearby",
                "Move approximately 20m towards the nearest window or North-East for a ${bestNearby.carrier} tower connection."
            )
        }

        return OptimizationAdvice("Network Optimal", "You are connected to the best available network for your area.")
    }
}

data class OptimizationAdvice(
    val title: String,
    val description: String
)
