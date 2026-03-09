package com.example.smokemap.ui.map

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smokemap.data.repository.SpotRepository
import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class MapUiState(
    val spots: List<Spot> = emptyList(),
    val filteredSpots: List<Spot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedSpot: Spot? = null,
    val searchRadiusM: Int = 500,
    val userLat: Double = 33.5902,
    val userLng: Double = 130.4207,
    val locationReady: Boolean = false,
    val selectedCategories: Set<SpotCategory> = SpotCategory.entries.toSet(),
    val isListView: Boolean = false,
    val searchQuery: String = ""
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SpotRepository(application)
    private val geocoder = Geocoder(application, Locale.JAPAN)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun onLocationResolved(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            userLat = lat,
            userLng = lng,
            locationReady = true
        )
        loadSpots()
    }

    fun loadSpots() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getSpots()
            result.onSuccess { spots ->
                _uiState.value = _uiState.value.copy(
                    spots = spots,
                    filteredSpots = spots.filterByCategory(_uiState.value.selectedCategories),
                    isLoading = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "読み込みに失敗しました"
                )
            }
        }
    }

    fun searchNearby(lat: Double, lng: Double, radiusM: Int = 500) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                userLat = lat,
                userLng = lng,
                searchRadiusM = radiusM
            )
            val result = repository.getNearbySpots(lat, lng, radiusM)
            result.onSuccess { spots ->
                _uiState.value = _uiState.value.copy(
                    spots = spots,
                    filteredSpots = spots.filterByCategory(_uiState.value.selectedCategories),
                    isLoading = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "検索に失敗しました"
                )
            }
        }
    }

    fun updateRadius(radiusM: Int) {
        _uiState.value = _uiState.value.copy(searchRadiusM = radiusM)
        searchNearby(
            lat = _uiState.value.userLat,
            lng = _uiState.value.userLng,
            radiusM = radiusM
        )
    }

    fun toggleCategory(category: SpotCategory) {
        val current = _uiState.value.selectedCategories
        val updated = if (current.contains(category)) {
            if (current.size > 1) current - category else current // 最低1つは選択
        } else {
            current + category
        }
        _uiState.value = _uiState.value.copy(
            selectedCategories = updated,
            filteredSpots = _uiState.value.spots.filterByCategory(updated)
        )
    }

    fun selectSpot(spot: Spot?) {
        _uiState.value = _uiState.value.copy(selectedSpot = spot)
    }

    fun toggleListView() {
        _uiState.value = _uiState.value.copy(isListView = !_uiState.value.isListView)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun searchByAddress() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            @Suppress("DEPRECATION")
            val addresses = withContext(Dispatchers.IO) {
                runCatching { geocoder.getFromLocationName(query, 1) }.getOrNull()
            }
            val address = addresses?.firstOrNull()
            if (address != null) {
                _uiState.value = _uiState.value.copy(
                    userLat = address.latitude,
                    userLng = address.longitude,
                    locationReady = true
                )
                searchNearby(address.latitude, address.longitude, _uiState.value.searchRadiusM)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "「$query」の場所が見つかりませんでした"
                )
            }
        }
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(userLat = lat, userLng = lng)
    }

    private fun List<Spot>.filterByCategory(categories: Set<SpotCategory>): List<Spot> {
        return filter { it.category in categories }
    }
}
