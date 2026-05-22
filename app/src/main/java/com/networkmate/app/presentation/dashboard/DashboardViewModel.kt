package com.networkmate.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.networkmate.app.data.telephony.TelephonyTracker
import com.networkmate.app.domain.model.TowerInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val telephonyTracker: TelephonyTracker
) : ViewModel() {

    val towerInfoList: StateFlow<List<TowerInfo>> = telephonyTracker.towerInfoList
    val connectedTower: StateFlow<TowerInfo?> = telephonyTracker.connectedTower

    init {
        startTracking()
    }

    private fun startTracking() {
        viewModelScope.launch {
            while (true) {
                telephonyTracker.updateTowerData()
                delay(3000) // Update every 3 seconds
            }
        }
    }
}
