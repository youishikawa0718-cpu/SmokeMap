package com.example.smokemap.ui.addspot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smokemap.data.repository.SpotRepository
import com.example.smokemap.domain.model.SpotCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddSpotUiState(
    val name: String = "",
    val category: SpotCategory = SpotCategory.OUTDOOR,
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AddSpotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SpotRepository(application)

    private val _uiState = MutableStateFlow(AddSpotUiState())
    val uiState: StateFlow<AddSpotUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onCategoryChanged(category: SpotCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onLocationSelected(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(latitude = lat, longitude = lng)
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "名称を入力してください")
            return
        }
        if (state.latitude == null || state.longitude == null) {
            _uiState.value = state.copy(error = "地図をタップして場所を選択してください")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            val result = repository.createSpot(
                name = state.name,
                latitude = state.latitude,
                longitude = state.longitude,
                category = state.category,
                description = state.description.ifBlank { null }
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "登録に失敗しました"
                )
            }
        }
    }
}
