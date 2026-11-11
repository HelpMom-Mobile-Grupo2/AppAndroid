package com.helpmom.mother.core.ui

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>
    data object Offline : UiState<Nothing>
}
