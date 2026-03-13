package com.example.smokemap.ui.detail

import com.example.smokemap.domain.model.Review
import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotDetailUiStateTest {

    @Test
    fun `default state has correct initial values`() {
        val state = SpotDetailUiState()
        assertNull(state.spot)
        assertTrue(state.reviews.isEmpty())
        assertFalse(state.isFavorite)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(0, state.reviewRating)
        assertEquals("", state.reviewComment)
        assertFalse(state.isSubmittingReview)
        assertFalse(state.reviewSubmitted)
        assertFalse(state.showReportDialog)
        assertFalse(state.isSubmittingReport)
        assertFalse(state.reportSubmitted)
    }

    @Test
    fun `state with spot loaded`() {
        val spot = Spot(
            id = "s1",
            name = "Test",
            latitude = 35.0,
            longitude = 139.0,
            category = SpotCategory.INDOOR
        )
        val reviews = listOf(
            Review(id = "r1", spotId = "s1", rating = 4, comment = "Good"),
            Review(id = "r2", spotId = "s1", rating = 5, comment = "Great")
        )

        val state = SpotDetailUiState(
            spot = spot,
            reviews = reviews,
            isFavorite = true,
            isLoading = false
        )

        assertEquals(spot, state.spot)
        assertEquals(2, state.reviews.size)
        assertTrue(state.isFavorite)
    }

    @Test
    fun `review form state management`() {
        val state = SpotDetailUiState()
            .copy(reviewRating = 4, reviewComment = "Nice place")

        assertEquals(4, state.reviewRating)
        assertEquals("Nice place", state.reviewComment)

        val cleared = state.copy(reviewRating = 0, reviewComment = "", reviewSubmitted = true)
        assertEquals(0, cleared.reviewRating)
        assertEquals("", cleared.reviewComment)
        assertTrue(cleared.reviewSubmitted)
    }

    @Test
    fun `report dialog state management`() {
        val state = SpotDetailUiState().copy(showReportDialog = true)
        assertTrue(state.showReportDialog)

        val submitting = state.copy(isSubmittingReport = true)
        assertTrue(submitting.isSubmittingReport)

        val submitted = submitting.copy(
            isSubmittingReport = false,
            showReportDialog = false,
            reportSubmitted = true
        )
        assertFalse(submitted.showReportDialog)
        assertFalse(submitted.isSubmittingReport)
        assertTrue(submitted.reportSubmitted)
    }
}
