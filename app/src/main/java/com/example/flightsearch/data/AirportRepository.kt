package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

interface AirportRepository {
    fun getDepartures(searchQuery: String): Flow<List<Airport>>

    fun getArrivals(searchQuery: String): Flow<List<Airport>>

    fun getAirport(iataCode: String): Flow<Airport>
}