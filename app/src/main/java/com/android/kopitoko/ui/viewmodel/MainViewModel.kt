package com.android.kopitoko.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kopitoko.data.repository.ApiKeyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    apiKeyRepository: ApiKeyRepository
) : ViewModel() {

    val isApiKeyConfigured: StateFlow<Boolean> = apiKeyRepository.isKeyConfiguredFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
}
