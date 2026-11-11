package com.helpmom.mother.feature.telemetry.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.database.MeasurementEntity
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.feature.telemetry.data.TelemetryRepository
import com.helpmom.mother.feature.telemetry.data.TelemetryRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TelemetryViewModel(private val repository: TelemetryRepository) : ViewModel() {
    val history = repository.cachedHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var state: UiState<MeasurementEntity> by mutableStateOf(UiState.Loading)
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            state = UiState.Loading
            errorMessage = null
            repository.refreshHistory().onSuccess { rows ->
                state = rows.firstOrNull()?.let { UiState.Success(it.toEntity()) } ?: UiState.Empty
            }.onFailure {
                state = if (it is java.io.IOException) UiState.Offline else UiState.Error(it.message ?: "No se pudo cargar.")
            }
        }
    }

    fun record(heartRate: Int, temperature: Double) {
        viewModelScope.launch {
            errorMessage = null
            repository.record(TelemetryRequest(heartRate, temperature)).onSuccess { dto ->
                state = UiState.Success(dto.toEntity())
                repository.refreshHistory()
            }.onFailure { err ->
                errorMessage = err.message ?: "No se pudo guardar la medición."
            }
        }
    }

    private fun com.helpmom.mother.feature.telemetry.data.TelemetryDto.toEntity() =
        MeasurementEntity(id, fetalHeartRateBpm, temperatureCelsius, measuredAt, healthLight, healthSummary)
}
