package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

class OfflineFavoriteRepository(private val favoriteDao: FavoriteDao): FavoriteRepository {
    override suspend fun insertFav(favorite: Favorite) = favoriteDao.putFavorite(favorite)

    override suspend fun deleteFav(favorite: Favorite) = favoriteDao.deleteFavorite(favorite)

    override fun getFavorites(): Flow<List<Favorite>> = favoriteDao.getAllFavorites()

    override suspend fun checkFav(id: Int): Int = favoriteDao.checkFavorite(id)
}