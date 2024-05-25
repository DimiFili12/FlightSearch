package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {
    @Query("SELECT * from airport WHERE (name LIKE '%' || :searchQuery || '%' OR iata_Code LIKE '%' || :searchQuery || '%') ORDER BY passengers DESC")
    fun getDepartures(searchQuery: String): Flow<List<Airport>>

    @Query("SELECT * from airport WHERE NOT (name = :searchQuery OR iata_Code = :searchQuery)")
    fun getArrivals(searchQuery: String): Flow<List<Airport>>

    @Query("SELECT * from airport WHERE iata_code = :iataCode")
    fun getAirport(iataCode: String): Flow<Airport>
}