package com.android.kopitoko.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kopitoko_preferences")

class ApiKeyDataStore(private val context: Context) {

    companion object {
        private val GEMINI_API_KEY_PREF = stringPreferencesKey("gemini_api_key")
    }

    val customApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GEMINI_API_KEY_PREF]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[GEMINI_API_KEY_PREF] = key.trim()
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(GEMINI_API_KEY_PREF)
        }
    }
}
