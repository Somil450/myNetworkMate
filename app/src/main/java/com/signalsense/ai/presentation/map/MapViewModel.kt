package com.signalsense.ai.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalsense.ai.data.local.TowerMapEntity
import com.signalsense.ai.data.location.LocationTracker
import com.signalsense.ai.data.remote.OpenCelliDCell
import com.signalsense.ai.data.remote.OpenCelliDRepository
import com.signalsense.ai.data.telephony.TelephonyTracker
import com.signalsense.ai.domain.model.TowerInfo
import com.signalsense.ai.domain.usecase.HeatPoint
import com.signalsense.ai.domain.usecase.HeatmapProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val telephonyTracker: TelephonyTracker,
    private val locationTracker: LocationTracker,
    private val openCelliDRepository: OpenCelliDRepository,
    private val heatmapProcessor: HeatmapProcessor
) : ViewModel() {

    // Live towers from your SIM (converted for map display)
    val connectedTower: StateFlow<TowerInfo?> = telephonyTracker.connectedTower
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All towers from OpenCelliD database (all carriers near you)
    private val _allNearbyTowers = MutableStateFlow<List<OpenCelliDCell>>(emptyList())
    val allNearbyTowers: StateFlow<List<OpenCelliDCell>> = _allNearbyTowers.asStateFlow()

    private val _isLoadingTowers = MutableStateFlow(false)
    val isLoadingTowers: StateFlow<Boolean> = _isLoadingTowers.asStateFlow()

    private val _heatmapPoints = MutableStateFlow<List<HeatPoint>>(emptyList())
    val heatmapPoints = _heatmapPoints.asStateFlow()

    val userLocation = locationTracker.location

    private val _selectedTower = MutableStateFlow<OpenCelliDCell?>(null)
    val selectedTower: StateFlow<OpenCelliDCell?> = _selectedTower.asStateFlow()

    private val _currentTower = MutableStateFlow<OpenCelliDCell?>(null)
    val currentTower: StateFlow<OpenCelliDCell?> = _currentTower.asStateFlow()

    private val _targetTower = MutableStateFlow<OpenCelliDCell?>(null)
    val targetTower: StateFlow<OpenCelliDCell?> = _targetTower.asStateFlow()

    fun selectTower(tower: OpenCelliDCell?) {
        _selectedTower.value = tower
    }

    fun setCurrentTower(tower: OpenCelliDCell?) {
        _currentTower.value = tower
        _selectedTower.value = null
    }

    fun setTargetTower(tower: OpenCelliDCell?) {
        _targetTower.value = tower
        _selectedTower.value = null
    }

    init {
        locationTracker.startTracking()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                fetchTowersFromCloud()
                delay(30_000L) // refresh every 30 seconds
            }
        }
    }

    fun fetchTowersFromCloud() {
        viewModelScope.launch {
            _isLoadingTowers.value = true
            openCelliDRepository.getTowersNearUser().collect { towers ->
                _allNearbyTowers.value = towers
                // Also build heatmap from tower positions
                _heatmapPoints.value = towers.map {
                    HeatPoint(
                        lat = it.lat,
                        lng = it.lon,
                        intensity = ((it.averageSignal + 140).coerceIn(0, 100) / 100.0).toFloat(),
                        carrier = it.carrierName
                    )
                }
            }
            _isLoadingTowers.value = false
        }
    }

    fun refreshHeatmap(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double) {
        viewModelScope.launch {
            _heatmapPoints.value = heatmapProcessor.generateHeatPoints(minLat, maxLat, minLng, maxLng)
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationTracker.stopTracking()
    }
}
