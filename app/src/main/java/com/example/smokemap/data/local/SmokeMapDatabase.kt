package com.example.smokemap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteSpotEntity::class, SpotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SmokeMapDatabase : RoomDatabase() {
    abstract fun favoriteSpotDao(): FavoriteSpotDao
    abstract fun spotDao(): SpotDao
}
