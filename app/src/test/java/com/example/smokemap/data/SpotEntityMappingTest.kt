package com.example.smokemap.data

import com.example.smokemap.data.local.SpotEntity
import com.example.smokemap.data.repository.toEntity
import com.example.smokemap.data.repository.toDomain
import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import com.example.smokemap.domain.model.SpotStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SpotEntityMappingTest {

    @Test
    fun `Spot toEntity converts correctly`() {
        val spot = Spot(
            id = "s1",
            name = "Test Spot",
            latitude = 35.6812,
            longitude = 139.7671,
            category = SpotCategory.INDOOR,
            description = "A test spot",
            status = SpotStatus.APPROVED,
            createdByUserId = "user1",
            avgRating = 4.5
        )

        val entity = spot.toEntity()

        assertEquals("s1", entity.id)
        assertEquals("Test Spot", entity.name)
        assertEquals(35.6812, entity.latitude, 0.0001)
        assertEquals(139.7671, entity.longitude, 0.0001)
        assertEquals("indoor", entity.category)
        assertEquals("A test spot", entity.description)
        assertEquals("approved", entity.status)
        assertEquals("user1", entity.createdByUserId)
        assertEquals(4.5, entity.avgRating ?: 0.0, 0.01)
    }

    @Test
    fun `SpotEntity toDomain converts correctly`() {
        val entity = SpotEntity(
            id = "s1",
            name = "Test Spot",
            latitude = 35.6812,
            longitude = 139.7671,
            category = "indoor",
            description = "A test spot",
            status = "approved",
            createdByUserId = "user1",
            avgRating = 4.5
        )

        val spot = entity.toDomain()

        assertEquals("s1", spot.id)
        assertEquals("Test Spot", spot.name)
        assertEquals(35.6812, spot.latitude, 0.0001)
        assertEquals(139.7671, spot.longitude, 0.0001)
        assertEquals(SpotCategory.INDOOR, spot.category)
        assertEquals("A test spot", spot.description)
        assertEquals(SpotStatus.APPROVED, spot.status)
        assertEquals("user1", spot.createdByUserId)
        assertEquals(4.5, spot.avgRating ?: 0.0, 0.01)
    }

    @Test
    fun `roundtrip Spot to Entity and back preserves data`() {
        val original = Spot(
            id = "s1",
            name = "Roundtrip",
            latitude = 33.59,
            longitude = 130.42,
            category = SpotCategory.RESTAURANT,
            description = null,
            status = SpotStatus.PENDING,
            createdByUserId = "u1",
            avgRating = 3.0
        )

        val roundtripped = original.toEntity().toDomain()

        assertEquals(original.id, roundtripped.id)
        assertEquals(original.name, roundtripped.name)
        assertEquals(original.latitude, roundtripped.latitude, 0.0001)
        assertEquals(original.longitude, roundtripped.longitude, 0.0001)
        assertEquals(original.category, roundtripped.category)
        assertEquals(original.description, roundtripped.description)
        assertEquals(original.status, roundtripped.status)
        assertEquals(original.createdByUserId, roundtripped.createdByUserId)
        assertEquals(original.avgRating ?: 0.0, roundtripped.avgRating ?: 0.0, 0.01)
    }

    @Test
    fun `SpotEntity toDomain handles unknown category gracefully`() {
        val entity = SpotEntity(
            id = "s1",
            name = "Test",
            latitude = 0.0,
            longitude = 0.0,
            category = "unknown_type",
            description = null,
            status = "unknown_status",
            createdByUserId = "",
            avgRating = null
        )

        val spot = entity.toDomain()
        assertEquals(SpotCategory.OUTDOOR, spot.category)
        assertEquals(SpotStatus.APPROVED, spot.status)
    }
}
