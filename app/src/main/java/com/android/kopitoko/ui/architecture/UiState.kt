package com.android.kopitoko.ui.architecture

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}

sealed interface ApiKeyTestStatus {
    data object Idle : ApiKeyTestStatus
    data object Testing : ApiKeyTestStatus
    data class Success(val message: String) : ApiKeyTestStatus
    data class Error(val message: String) : ApiKeyTestStatus
}
