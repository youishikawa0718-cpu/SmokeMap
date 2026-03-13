package com.example.smokemap.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SpotTest {

    @Test
    fun `SpotCategory fromString returns correct category for valid input`() {
        assertEquals(SpotCategory.INDOOR, SpotCategory.fromString("indoor"))
        assertEquals(SpotCategory.OUTDOOR, SpotCategory.fromString("outdoor"))
        assertEquals(SpotCategory.RESTAURANT, SpotCategory.fromString("restaurant"))
    }

    @Test
    fun `SpotCategory fromString is case insensitive`() {
        assertEquals(SpotCategory.INDOOR, SpotCategory.fromString("INDOOR"))
        assertEquals(SpotCategory.INDOOR, SpotCategory.fromString("Indoor"))
    }

    @Test
    fun `SpotCategory fromString returns OUTDOOR for unknown value`() {
        assertEquals(SpotCategory.OUTDOOR, SpotCategory.fromString("unknown"))
        assertEquals(SpotCategory.OUTDOOR, SpotCategory.fromString(null))
        assertEquals(SpotCategory.OUTDOOR, SpotCategory.fromString(""))
    }

    @Test
    fun `SpotStatus fromString returns correct status for valid input`() {
        assertEquals(SpotStatus.PENDING, SpotStatus.fromString("pending"))
        assertEquals(SpotStatus.APPROVED, SpotStatus.fromString("approved"))
        assertEquals(SpotStatus.CLOSED, SpotStatus.fromString("closed"))
    }

    @Test
    fun `SpotStatus fromString returns APPROVED for unknown value`() {
        assertEquals(SpotStatus.APPROVED, SpotStatus.fromString("unknown"))
        assertEquals(SpotStatus.APPROVED, SpotStatus.fromString(null))
    }

    @Test
    fun `SpotCategory displayName returns Japanese name`() {
        assertEquals("屋内", SpotCategory.INDOOR.displayName)
        assertEquals("屋外", SpotCategory.OUTDOOR.displayName)
        assertEquals("飲食店併設", SpotCategory.RESTAURANT.displayName)
    }

    @Test
    fun `Spot default values are correct`() {
        val spot = Spot()
        assertEquals("", spot.id)
        assertEquals("", spot.name)
        assertEquals(0.0, spot.latitude, 0.001)
        assertEquals(0.0, spot.longitude, 0.001)
        assertEquals(SpotCategory.OUTDOOR, spot.category)
        assertEquals(null, spot.description)
        assertEquals(SpotStatus.APPROVED, spot.status)
        assertEquals(null, spot.distanceMeters)
        assertEquals(null, spot.avgRating)
    }
}
