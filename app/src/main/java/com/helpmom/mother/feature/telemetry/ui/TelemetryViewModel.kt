package com.helpmom.mother.feature.telemetry.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.database.MeasurementEntity
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.feature.pregnancy.data.SymptomDto
import com.helpmom.mother.feature.telemetry.data.TelemetryRepository
import com.helpmom.mother.feature.telemetry.data.TelemetryRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TelemetryViewModel(
    private val repository: TelemetryRepository,
    private val symptomsLoader: suspend () -> List<SymptomDto> = { emptyList() }
) : ViewModel() {
    val history = repository.cachedHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var state: UiState<MeasurementEntity> by mutableStateOf(UiState.Loading)
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set
    var isSaving: Boolean by mutableStateOf(false)
        private set
    var symptoms: List<SymptomDto> by mutableStateOf(emptyList())
        private set

    /** Medición roja recién tomada que todavía no se cerró. Abre el modal de emergencia. */
    var pendingEmergency: MeasurementEntity? by mutableStateOf(null)
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

    fun loadSymptoms() {
        viewModelScope.launch {
            runCatching { symptomsLoader() }.onSuccess { list ->
                symptoms = list.sortedByDescending { it.occurredAt }
            }
        }
    }

    fun record(heartRate: Int, temperature: Double) {
        if (isSaving) return
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            repository.record(TelemetryRequest(heartRate, temperature)).onSuccess { dto ->
                val measurement = dto.toEntity()
                state = UiState.Success(measurement)
                if (measurement.healthLight == 3) pendingEmergency = measurement
                repository.refreshHistory()
            }.onFailure { err ->
                errorMessage = if (err is java.io.IOException) {
                    "Sin conexión. No se pudo guardar la medición."
                } else {
                    err.message ?: "No se pudo guardar la medición."
                }
            }
            isSaving = false
        }
    }

    fun dismissEmergency() {
        pendingEmergency = null
    }

    private fun com.helpmom.mother.feature.telemetry.data.TelemetryDto.toEntity() =
        MeasurementEntity(id, fetalHeartRateBpm, temperatureCelsius, measuredAt, healthLight, healthSummary)
}
