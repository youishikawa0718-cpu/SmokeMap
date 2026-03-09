package com.example.smokemap.data.repository

import android.content.Context
import com.example.smokemap.data.local.DatabaseProvider
import com.example.smokemap.data.local.SpotEntity
import com.example.smokemap.data.remote.CreateSpotRequest
import com.example.smokemap.data.remote.NearbyRequest
import com.example.smokemap.data.remote.NearbySpotDto
import com.example.smokemap.data.remote.ReviewDto
import com.example.smokemap.data.remote.SpotDto
import com.example.smokemap.data.remote.SupabaseClient
import com.example.smokemap.domain.model.Review
import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import com.example.smokemap.domain.model.SpotStatus

class SpotRepository(private val context: Context? = null) {

    private val api = SupabaseClient.api
    private val apiKey = SupabaseClient.supabaseKey
    private val spotDao by lazy { context?.let { DatabaseProvider.getDatabase(it).spotDao() } }

    suspend fun getSpots(): Result<List<Spot>> {
        return try {
            val response = api.getSpots(apiKey = apiKey)
            val spots = response.map { it.toDomain() }
            // オフラインキャッシュに保存
            spotDao?.let { dao ->
                dao.clearAll()
                dao.insertAll(spots.map { it.toEntity() })
            }
            Result.success(spots)
        } catch (e: Exception) {
            // オフラインフォールバック
            val cached = spotDao?.getAllSpots()
            if (!cached.isNullOrEmpty()) {
                Result.success(cached.map { it.toDomain() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getNearbySpots(
        lat: Double,
        lng: Double,
        radiusM: Int = 500
    ): Result<List<Spot>> {
        return try {
            val request = NearbyRequest(lat = lat, lng = lng, radiusM = radiusM)
            val response = api.getNearbySpots(apiKey = apiKey, request = request)
            val spots = response.map { it.toDomain() }
            spotDao?.let { dao ->
                dao.insertAll(spots.map { it.toEntity() })
            }
            Result.success(spots)
        } catch (e: Exception) {
            val cached = spotDao?.getAllSpots()
            if (!cached.isNullOrEmpty()) {
                Result.success(cached.map { it.toDomain() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getSpotById(spotId: String): Result<Spot> {
        return try {
            val response = api.getSpotById(apiKey = apiKey, id = "eq.$spotId")
            val dto = response.firstOrNull()
                ?: return Result.failure(Exception("スポットが見つかりません"))
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            // オフラインフォールバック
            val cached = spotDao?.getSpotById(spotId)
            if (cached != null) {
                Result.success(cached.toDomain())
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getReviewsForSpot(spotId: String): Result<List<Review>> {
        return try {
            val response = api.getReviews(apiKey = apiKey, spotId = "eq.$spotId")
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSpot(
        name: String,
        latitude: Double,
        longitude: Double,
        category: SpotCategory,
        description: String?
    ): Result<Spot> {
        return try {
            val request = CreateSpotRequest(
                name = name,
                latitude = latitude,
                longitude = longitude,
                category = category.name.lowercase(),
                description = description
            )
            val response = api.createSpot(
                apiKey = apiKey,
                auth = "Bearer $apiKey",
                spot = request
            )
            val dto = response.firstOrNull()
                ?: return Result.failure(Exception("登録に失敗しました"))
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// === DTO → ドメインモデル変換 ===

private fun SpotDto.toDomain(): Spot {
    return Spot(
        id = id,
        name = name ?: "",
        latitude = latitude,
        longitude = longitude,
        category = SpotCategory.fromString(category),
        description = description,
        status = SpotStatus.fromString(status),
        createdByUserId = createdBy,
    )
}

private fun NearbySpotDto.toDomain(): Spot {
    return Spot(
        id = id,
        name = name ?: "",
        latitude = latitude,
        longitude = longitude,
        category = SpotCategory.fromString(category),
        description = description,
        status = SpotStatus.fromString(status),
        distanceMeters = distanceM,
        avgRating = avgRating
    )
}

private fun ReviewDto.toDomain(): Review {
    return Review(
        id = id,
        spotId = spotId,
        userId = userId,
        rating = rating,
        comment = comment,
        userName = userName,
        createdAt = 0L
    )
}

fun Spot.toEntity(): SpotEntity {
    return SpotEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        category = category.name.lowercase(),
        description = description,
        status = status.name.lowercase(),
        createdByUserId = createdByUserId,
        avgRating = avgRating
    )
}

fun SpotEntity.toDomain(): Spot {
    return Spot(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        category = SpotCategory.fromString(category),
        description = description,
        status = SpotStatus.fromString(status),
        createdByUserId = createdByUserId,
        avgRating = avgRating
    )
}
