package com.example.smokemap.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smokemap.data.local.DatabaseProvider
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
    val error: String? = null
)

class SpotDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SpotRepository(application)
    private val favoriteDao = DatabaseProvider.getDatabase(application).favoriteSpotDao()

    private val _uiState = MutableStateFlow(SpotDetailUiState())
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    fun loadSpot(spotId: String) {
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
}
