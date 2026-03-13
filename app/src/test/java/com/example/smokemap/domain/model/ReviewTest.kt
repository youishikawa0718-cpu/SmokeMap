package com.example.smokemap.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewTest {

    @Test
    fun `Review default values are correct`() {
        val review = Review(rating = 3)
        assertEquals("", review.id)
        assertEquals("", review.spotId)
        assertEquals("", review.userId)
        assertEquals(3, review.rating)
        assertNull(review.comment)
        assertNull(review.userName)
        assertEquals(0L, review.createdAt)
    }

    @Test
    fun `Review with all fields set`() {
        val review = Review(
            id = "r1",
            spotId = "s1",
            userId = "u1",
            rating = 5,
            comment = "Great spot",
            userName = "Taro",
            createdAt = 1000L
        )
        assertEquals("r1", review.id)
        assertEquals("s1", review.spotId)
        assertEquals("u1", review.userId)
        assertEquals(5, review.rating)
        assertEquals("Great spot", review.comment)
        assertEquals("Taro", review.userName)
        assertEquals(1000L, review.createdAt)
    }
}
