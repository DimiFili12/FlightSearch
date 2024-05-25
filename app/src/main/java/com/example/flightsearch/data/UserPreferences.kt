package com.example.flightsearch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class UserPreferences(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {
    private companion object {
        val LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")
    }

    override suspend fun savePreferences(query: String) {
        dataStore.edit { word ->
            word[LAST_SEARCH_QUERY] = query
        }
    }

    override suspend fun readPreferences(): String {
        val preferences = dataStore.data.first()
        return preferences[LAST_SEARCH_QUERY] ?: ""
    }
}