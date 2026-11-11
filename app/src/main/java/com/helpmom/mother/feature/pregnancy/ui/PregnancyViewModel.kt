package com.helpmom.mother.feature.pregnancy.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.session.SessionStore
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.feature.pregnancy.data.PregnancyApi
import com.helpmom.mother.feature.pregnancy.data.PregnancyDto
import com.helpmom.mother.feature.pregnancy.data.PregnancyRequest
import kotlinx.coroutines.launch
import java.time.LocalDate

class PregnancyViewModel(private val api: PregnancyApi, private val session: SessionStore) : ViewModel() {
    var state: UiState<PregnancyDto> by mutableStateOf(UiState.Empty)
        private set
    fun create(fum: LocalDate, fpp: LocalDate) { viewModelScope.launch {
        state = UiState.Loading
        runCatching { api.createPregnancy(PregnancyRequest(fum.toString(), fpp.toString())) }
            .onSuccess { response ->
                if (response.isSuccessful) response.body()?.let { session.savePregnancyId(it.id); state = UiState.Success(it) }
                else state = UiState.Error(com.helpmom.mother.core.network.ApiError.from(response).detail)
            }.onFailure { state = if (it is java.io.IOException) UiState.Offline else UiState.Error(it.message ?: "No se pudo guardar.") }
    } }
}
