package com.example.smokemap.domain.model

data class Review(
    val id: String = "",
    val spotId: String = "",
    val userId: String = "",
    val rating: Int ,
    val comment: String? = null,
    val userName: String? = null,
    val createdAt: Long = 0L
)