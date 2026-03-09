package com.example.smokemap.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    @GET("rest/v1/spots")
    suspend fun getSpots(
        @Header("apikey") apiKey: String,
        @Query("status") status: String = "eq.approved",
        @Query("select") select: String = "*"
    ): List<SpotDto>

    @POST("rest/v1/rpc/nearby_spots")
    suspend fun getNearbySpots(
        @Header("apikey") apiKey: String,
        @Body request: NearbyRequest
    ): List<NearbySpotDto>

    @GET("rest/v1/spots")
    suspend fun getSpotById(
        @Header("apikey") apiKey: String,
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): List<SpotDto>

    @GET("rest/v1/reviews")
    suspend fun getReviews(
        @Header("apikey") apiKey: String,
        @Query("spot_id") spotId: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<ReviewDto>

    @POST("rest/v1/spots")
    suspend fun createSpot(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body spot: CreateSpotRequest
    ): List<SpotDto>
}

// === DTO ===

@Serializable
data class SpotDto(
    val id: String = "",
    val name: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val category: String = "outdoor",
    val description: String? = null,
    val status: String = "approved",
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class NearbySpotDto(
    val id: String = "",
    val name: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val category: String = "outdoor",
    val description: String? = null,
    val status: String = "approved",
    @SerialName("distance_m") val distanceM: Double = 0.0,
    @SerialName("avg_rating") val avgRating: Double = 0.0
)

@Serializable
data class NearbyRequest(
    val lat: Double,
    val lng: Double,
    @SerialName("radius_m") val radiusM: Int = 500
)

@Serializable
data class ReviewDto(
    val id: String = "",
    @SerialName("spot_id") val spotId: String = "",
    @SerialName("user_id") val userId: String = "",
    val rating: Int = 0,
    val comment: String? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class CreateSpotRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val description: String? = null,
    val status: String = "pending",
    @SerialName("created_by") val createdBy: String = "anonymous"
)
