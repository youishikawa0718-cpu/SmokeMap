package com.example.smokemap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spots_cache")
data class SpotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val description: String?,
    val status: String,
    val createdByUserId: String,
    val avgRating: Double?,
    val cachedAt: Long = System.currentTimeMillis()
)
