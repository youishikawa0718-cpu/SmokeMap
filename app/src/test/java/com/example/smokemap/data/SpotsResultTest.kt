package com.example.smokemap.data

import com.example.smokemap.data.repository.SpotsResult
import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotsResultTest {

    @Test
    fun `SpotsResult online has isOffline false`() {
        val result = SpotsResult(
            spots = listOf(Spot(id = "1", name = "Test")),
            isOffline = false
        )
        assertFalse(result.isOffline)
        assertEquals(1, result.spots.size)
    }

    @Test
    fun `SpotsResult offline has isOffline true`() {
        val result = SpotsResult(
            spots = listOf(Spot(id = "1", name = "Cached")),
            isOffline = true
        )
        assertTrue(result.isOffline)
    }

    @Test
    fun `SpotsResult default isOffline is false`() {
        val result = SpotsResult(spots = emptyList())
        assertFalse(result.isOffline)
    }
}
