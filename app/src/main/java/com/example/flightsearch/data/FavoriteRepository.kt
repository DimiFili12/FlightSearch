package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun insertFav(favorite: Favorite)

    suspend fun deleteFav(favorite: Favorite)

    fun getFavorites(): Flow<List<Favorite>>

    fun checkFav(id: Int): Boolean
}