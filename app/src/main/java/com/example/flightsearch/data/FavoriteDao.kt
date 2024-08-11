package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert
    suspend fun putFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT * from favorite")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE id = :id)")
    suspend fun checkFavorite(id: Int): Boolean
}