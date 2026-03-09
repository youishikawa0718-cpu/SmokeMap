package com.example.smokemap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_spots")
data class FavoriteSpotEntity(
    @PrimaryKey val spotId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val description: String?,
    val addedAt: Long = System.currentTimeMillis()
)
