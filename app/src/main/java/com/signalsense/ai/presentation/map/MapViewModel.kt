package com.signalsense.ai.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalsense.ai.data.local.SignalDao
import com.signalsense.ai.data.local.TowerMapEntity
import com.signalsense.ai.domain.usecase.HeatmapProcessor
import com.signalsense.ai.domain.usecase.HeatPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val signalDao: SignalDao,
    private val heatmapProcessor: HeatmapProcessor
) : ViewModel() {

    val towerLocations: StateFlow<List<TowerMapEntity>> = signalDao.getAllTowers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _heatmapPoints = MutableStateFlow<List<HeatPoint>>(emptyList())
    val heatmapPoints = _heatmapPoints.asStateFlow()

    fun refreshHeatmap(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double) {
        viewModelScope.launch {
            _heatmapPoints.value = heatmapProcessor.generateHeatPoints(minLat, maxLat, minLng, maxLng)
        }
    }
}
