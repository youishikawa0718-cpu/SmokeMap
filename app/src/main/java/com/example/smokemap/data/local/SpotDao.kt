package com.example.smokemap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SpotDao {
    @Query("SELECT * FROM spots_cache")
    suspend fun getAllSpots(): List<SpotEntity>

    @Query("SELECT * FROM spots_cache WHERE id = :spotId")
    suspend fun getSpotById(spotId: String): SpotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(spots: List<SpotEntity>)

    @Query("DELETE FROM spots_cache")
    suspend fun clearAll()
}
