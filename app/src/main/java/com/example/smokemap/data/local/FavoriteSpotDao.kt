package com.example.smokemap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSpotDao {
    @Query("SELECT * FROM favorite_spots ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteSpotEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_spots WHERE spotId = :spotId)")
    fun isFavorite(spotId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(entity: FavoriteSpotEntity)

    @Query("DELETE FROM favorite_spots WHERE spotId = :spotId")
    suspend fun removeFavorite(spotId: String)
}
