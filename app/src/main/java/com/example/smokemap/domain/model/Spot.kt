package com.example.smokemap.domain.model


data class Spot(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val category: SpotCategory = SpotCategory.OUTDOOR,
    val description: String? = null,
    val status: SpotStatus = SpotStatus.APPROVED,
    val createdByUserId: String = "",
    val distanceMeters: Double? = null,
    val avgRating: Double? = null
)

enum class SpotCategory(val displayName: String) {
    INDOOR("屋内"),
    OUTDOOR("屋外"),
    RESTAURANT("飲食店併設");

    companion object {
        fun fromString(value: String?): SpotCategory {
            return when (value?.lowercase()) {
                "indoor" -> INDOOR
                "outdoor" -> OUTDOOR
                "restaurant" -> RESTAURANT
                else -> OUTDOOR
            }
        }
    }
}

enum class SpotStatus {
    PENDING, APPROVED, CLOSED;

    companion object {
        fun fromString(value: String?): SpotStatus {
            return when (value?.lowercase()) {
                "pending" -> PENDING
                "approved" -> APPROVED
                "closed" -> CLOSED
                else -> APPROVED
            }
        }
    }
}