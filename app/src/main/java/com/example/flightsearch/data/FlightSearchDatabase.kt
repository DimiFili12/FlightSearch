package com.example.flightsearch.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File
import java.io.FileOutputStream

@Database(entities = [Airport::class, Favorite::class], version = 1, exportSchema = false)
abstract class FlightSearchDatabase : RoomDatabase() {
    abstract fun airportDao(): AirportDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var instance: FlightSearchDatabase? = null

        fun getDatabase(context: Context): FlightSearchDatabase {
            return instance ?: synchronized(this) {
                val databaseFile = context.getDatabasePath("flight_search.db")

                if (!databaseFile.exists()) {
                    copyDatabaseFromAssets(context, "flight_search.db", databaseFile)
                    Log.d("no db", "getDatabase: ")
                }

                Room.databaseBuilder(
                    context.applicationContext,
                    FlightSearchDatabase::class.java,
                    "flight_search.db"
                ).build().also { instance = it }
            }
        }

        private fun copyDatabaseFromAssets(context: Context, databaseName: String, destinationFile: File) {
            val inputStream = context.assets.open(databaseName)
            val outputStream = FileOutputStream(destinationFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        }
    }
}