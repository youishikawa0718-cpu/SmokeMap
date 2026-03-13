package com.example.smokemap.ui.map

import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapUiStateTest {

    @Test
    fun `default state has correct initial values`() {
        val state = MapUiState()
        assertTrue(state.spots.isEmpty())
        assertTrue(state.filteredSpots.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isOfflineData)
        assertNull(state.selectedSpot)
        assertEquals(500, state.searchRadiusM)
        assertFalse(state.locationReady)
        assertEquals(SpotCategory.entries.toSet(), state.selectedCategories)
        assertFalse(state.isListView)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `filtering spots by category works correctly`() {
        val spots = listOf(
            Spot(id = "1", name = "Indoor", category = SpotCategory.INDOOR),
            Spot(id = "2", name = "Outdoor", category = SpotCategory.OUTDOOR),
            Spot(id = "3", name = "Restaurant", category = SpotCategory.RESTAURANT),
            Spot(id = "4", name = "Indoor 2", category = SpotCategory.INDOOR)
        )

        val indoorOnly = spots.filter { it.category in setOf(SpotCategory.INDOOR) }
        assertEquals(2, indoorOnly.size)
        assertTrue(indoorOnly.all { it.category == SpotCategory.INDOOR })

        val outdoorAndRestaurant = spots.filter {
            it.category in setOf(SpotCategory.OUTDOOR, SpotCategory.RESTAURANT)
        }
        assertEquals(2, outdoorAndRestaurant.size)
    }

    @Test
    fun `state copy preserves unmodified fields`() {
        val original = MapUiState(searchRadiusM = 1000, isListView = true)
        val modified = original.copy(isLoading = true)
        assertEquals(1000, modified.searchRadiusM)
        assertTrue(modified.isListView)
        assertTrue(modified.isLoading)
    }
}
