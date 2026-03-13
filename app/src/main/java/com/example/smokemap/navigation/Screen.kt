package com.example.smokemap.navigation

object Screen {
    const val MAP = "map"
    const val SPOT_DETAIL = "spot/{spotId}"
const val FAVORITES = "favorites"

    fun spotDetail(spotId: String) = "spot/$spotId"
}
