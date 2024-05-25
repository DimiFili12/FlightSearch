package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

class OfflineAirportRepository(private val airportDao: AirportDao) : AirportRepository {
    override fun getDepartures(searchQuery: String): Flow<List<Airport>>
        = airportDao.getDepartures(searchQuery)

    override fun getArrivals(searchQuery: String): Flow<List<Airport>>
        = airportDao.getArrivals(searchQuery)

    override fun getAirport(iataCode: String): Flow<Airport>
        = airportDao.getAirport(iataCode)
}