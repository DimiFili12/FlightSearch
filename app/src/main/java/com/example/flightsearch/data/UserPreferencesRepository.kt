package com.example.flightsearch.data

interface UserPreferencesRepository {
    suspend fun savePreferences(query: String)
    suspend fun readPreferences(): String
}