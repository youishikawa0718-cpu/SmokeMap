package com.example.smokemap.data.repository

import android.content.Context
import com.example.smokemap.data.local.DatabaseProvider
import com.example.smokemap.data.local.SpotEntity
import com.example.smokemap.data.remote.CreateReviewRequest
import com.example.smokemap.data.remote.CreateSpotReportRequest
import com.example.smokemap.data.remote.NearbyRequest
import com.example.smokemap.data.remote.NearbySpotDto
import com.example.smokemap.data.remote.ReviewDto
import com.example.smokemap.data.remote.SpotDto
import com.example.smokemap.data.remote.SupabaseClient
import com.example.smokemap.domain.model.Review
import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import com.example.smokemap.domain.model.SpotStatus
import retrofit2.HttpException

data class SpotsResult(
    val spots: List<Spot>,
    val isOffline: Boolean = false
)

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
    ): Result<SpotsResult> {
        return try {
            val request = NearbyRequest(lat = lat, lng = lng, radiusM = radiusM)
            val response = api.getNearbySpots(apiKey = apiKey, request = request)
            val spots = response.map { it.toDomain() }
            spotDao?.let { dao ->
                dao.insertAll(spots.map { it.toEntity() })
            }
            Result.success(SpotsResult(spots, isOffline = false))
        } catch (e: Exception) {
            val cached = spotDao?.getAllSpots()
            if (!cached.isNullOrEmpty()) {
                Result.success(SpotsResult(cached.map { it.toDomain() }, isOffline = true))
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

suspend fun createReview(
        spotId: String,
        rating: Int,
        comment: String?,
        deviceId: String
    ): Result<Review> {
        return try {
            val request = CreateReviewRequest(
                spotId = spotId,
                userId = deviceId,
                rating = rating,
                comment = comment
            )
            val response = api.createReview(
                apiKey = apiKey,
                auth = "Bearer $apiKey",
                review = request
            )
            val dto = response.firstOrNull()
                ?: return Result.failure(Exception("投稿に失敗しました"))
            Result.success(dto.toDomain())
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("SpotRepository", "createReview HTTP ${e.code()}: $errorBody")
            Result.failure(Exception("HTTP ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            android.util.Log.e("SpotRepository", "createReview error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String, deviceId: String): Result<Unit> {
        return try {
            api.deleteReview(
                apiKey = apiKey,
                auth = "Bearer $apiKey",
                id = "eq.$reviewId",
                userId = "eq.$deviceId"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportSpot(
        spotId: String,
        deviceId: String,
        reason: String,
        comment: String?
    ): Result<Unit> {
        return try {
            api.createSpotReport(
                apiKey = apiKey,
                auth = "Bearer $apiKey",
                report = CreateSpotReportRequest(
                    spotId = spotId,
                    userId = deviceId,
                    reason = reason,
                    comment = comment
                )
            )
            Result.success(Unit)
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
