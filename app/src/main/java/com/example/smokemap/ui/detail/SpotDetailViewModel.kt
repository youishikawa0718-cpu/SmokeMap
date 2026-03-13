package com.example.smokemap.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smokemap.data.local.DatabaseProvider
import com.example.smokemap.data.local.DeviceId
import com.example.smokemap.data.local.FavoriteSpotEntity
import com.example.smokemap.data.repository.SpotRepository
import com.example.smokemap.domain.model.Review
import com.example.smokemap.domain.model.Spot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reviewRating: Int = 0,
    val reviewComment: String = "",
    val reviewUserName: String = "",
    val isSubmittingReview: Boolean = false,
    val reviewSubmitted: Boolean = false,
    val deviceId: String = "",
    val showReportDialog: Boolean = false,
    val isSubmittingReport: Boolean = false,
    val reportSubmitted: Boolean = false
)

class SpotDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SpotRepository(application)
    private val favoriteDao = DatabaseProvider.getDatabase(application).favoriteSpotDao()
    private val deviceId = DeviceId.get(application)

    private val _uiState = MutableStateFlow(SpotDetailUiState(deviceId = deviceId))
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    private var currentSpotId: String = ""

    fun loadSpot(spotId: String) {
        currentSpotId = spotId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val spotResult = repository.getSpotById(spotId)
            spotResult.onSuccess { spot ->
                _uiState.value = _uiState.value.copy(spot = spot, isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "読み込みに失敗しました"
                )
            }

            val reviewResult = repository.getReviewsForSpot(spotId)
            reviewResult.onSuccess { reviews ->
                _uiState.value = _uiState.value.copy(reviews = reviews)
            }
        }

        viewModelScope.launch {
            favoriteDao.isFavorite(spotId).collect { isFav ->
                _uiState.value = _uiState.value.copy(isFavorite = isFav)
            }
        }
    }

    fun toggleFavorite() {
        val spot = _uiState.value.spot ?: return
        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                favoriteDao.removeFavorite(spot.id)
            } else {
                favoriteDao.addFavorite(
                    FavoriteSpotEntity(
                        spotId = spot.id,
                        name = spot.name,
                        latitude = spot.latitude,
                        longitude = spot.longitude,
                        category = spot.category.name.lowercase(),
                        description = spot.description
                    )
                )
            }
        }
    }

    fun onReviewRatingChanged(rating: Int) {
        _uiState.value = _uiState.value.copy(reviewRating = rating)
    }

    fun onReviewCommentChanged(comment: String) {
        _uiState.value = _uiState.value.copy(reviewComment = comment)
    }

    fun onReviewUserNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(reviewUserName = name)
    }

    fun submitReview() {
        val state = _uiState.value
        if (state.reviewRating == 0) {
            _uiState.value = state.copy(error = "星をタップして評価してください")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReview = true, error = null)
            val result = repository.createReview(
                spotId = currentSpotId,
                rating = state.reviewRating,
                comment = state.reviewComment.ifBlank { null },
                deviceId = deviceId
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false,
                    reviewSubmitted = true,
                    reviewRating = 0,
                    reviewComment = "",
                    reviewUserName = ""
                )
                reloadReviews()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false,
                    error = e.message ?: "投稿に失敗しました"
                )
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            val result = repository.deleteReview(reviewId, deviceId)
            result.onSuccess {
                reloadReviews()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "削除に失敗しました"
                )
            }
        }
    }

    fun clearReviewSubmitted() {
        _uiState.value = _uiState.value.copy(reviewSubmitted = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryLoadSpot() {
        if (currentSpotId.isNotEmpty()) loadSpot(currentSpotId)
    }

    fun showReportDialog() {
        _uiState.value = _uiState.value.copy(showReportDialog = true)
    }

    fun dismissReportDialog() {
        _uiState.value = _uiState.value.copy(showReportDialog = false)
    }

    fun submitReport(reason: String, comment: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReport = true)
            val result = repository.reportSpot(
                spotId = currentSpotId,
                deviceId = deviceId,
                reason = reason,
                comment = comment
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmittingReport = false,
                    showReportDialog = false,
                    reportSubmitted = true
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingReport = false,
                    error = e.message ?: "報告の送信に失敗しました"
                )
            }
        }
    }

    fun clearReportSubmitted() {
        _uiState.value = _uiState.value.copy(reportSubmitted = false)
    }

    private suspend fun reloadReviews() {
        val reviewResult = repository.getReviewsForSpot(currentSpotId)
        reviewResult.onSuccess { reviews ->
            _uiState.value = _uiState.value.copy(reviews = reviews)
        }
    }
}
