package com.android.kopitoko.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kopitoko.data.repository.ApiKeyRepository
import com.android.kopitoko.data.repository.GeminiRepository
import com.android.kopitoko.ui.architecture.ApiKeyTestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApiKeyViewModel(
    private val apiKeyRepository: ApiKeyRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    val apiKeyFlow: StateFlow<String> = apiKeyRepository.apiKeyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val isKeyConfiguredFlow: StateFlow<Boolean> = apiKeyRepository.isKeyConfiguredFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _testStatus = MutableStateFlow<ApiKeyTestStatus>(ApiKeyTestStatus.Idle)
    val testStatus: StateFlow<ApiKeyTestStatus> = _testStatus.asStateFlow()

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            apiKeyRepository.saveCustomApiKey(key)
            _testStatus.value = ApiKeyTestStatus.Idle
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            apiKeyRepository.clearCustomApiKey()
            _testStatus.value = ApiKeyTestStatus.Idle
        }
    }

    fun testKey(key: String) {
        viewModelScope.launch {
            _testStatus.value = ApiKeyTestStatus.Testing
            val result = geminiRepository.testApiKey(key)
            result.fold(
                onSuccess = { isWorking ->
                    if (isWorking) {
                        _testStatus.value = ApiKeyTestStatus.Success("API Key validated successfully!")
                    } else {
                        _testStatus.value = ApiKeyTestStatus.Error("API Key is blank or invalid.")
                    }
                },
                onFailure = { error ->
                    _testStatus.value = ApiKeyTestStatus.Error(
                        error.localizedMessage ?: "Failed to connect to Gemini AI."
                    )
                }
            )
        }
    }
}
