package com.android.kopitoko.data.repository

import com.android.kopitoko.BuildConfig
import com.android.kopitoko.data.local.ApiKeyDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class ApiKeyRepository(private val apiKeyDataStore: ApiKeyDataStore?) {

    open val apiKeyFlow: Flow<String> = apiKeyDataStore?.customApiKey?.map { customKey ->
        if (!customKey.isNullOrBlank()) {
            customKey
        } else {
            val buildConfigKey: String = BuildConfig.GEMINI_API_KEY
            buildConfigKey
        }
    } ?: kotlinx.coroutines.flow.flowOf("")

    open val isKeyConfiguredFlow: Flow<Boolean> = apiKeyFlow.map { key ->
        key.isNotBlank()
    }

    open suspend fun saveCustomApiKey(key: String) {
        apiKeyDataStore?.saveApiKey(key)
    }

    open suspend fun clearCustomApiKey() {
        apiKeyDataStore?.clearApiKey()
    }
}
