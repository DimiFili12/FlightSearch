package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface AppContainer {
    val airportRepository: AirportRepository
    val favoriteRepository: FavoriteRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class AppDataContainer(private val context: Context, dataStore: DataStore<Preferences>) : AppContainer {
    override val airportRepository: AirportRepository by lazy {
        OfflineAirportRepository(FlightSearchDatabase.getDatabase(context).airportDao())
    }

    override val favoriteRepository: FavoriteRepository by lazy {
        OfflineFavoriteRepository(FlightSearchDatabase.getDatabase(context).favoriteDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferences(dataStore)
    }
}