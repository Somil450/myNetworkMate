package com.signalsense.ai.presentation.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalsense.ai.data.repository.NetworkRepository
import com.signalsense.ai.data.repository.SpeedResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    private val _testState = MutableStateFlow<SpeedResultState>(SpeedResultState.Starting)
    val testState = _testState.asStateFlow()

    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed = _currentSpeed.asStateFlow()

    fun startSpeedTest() {
        viewModelScope.launch {
            repository.runSpeedTest().collect { state ->
                _testState.value = state
                if (state is SpeedResultState.Downloading) {
                    _currentSpeed.value = state.speed
                }
            }
        }
    }
}
