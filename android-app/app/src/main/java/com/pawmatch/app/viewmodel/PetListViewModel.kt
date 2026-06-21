package com.pawmatch.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawmatch.app.data.model.Pet
import com.pawmatch.app.data.model.ShelterInfo
import com.pawmatch.app.data.model.ShelterStats
import com.pawmatch.app.data.model.ShelterStory
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PetListViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var currentPage = 1; private set
    var hasMore = true; private set
    val pageSize = 20

    fun loadPets(type: String? = null, ageRange: String? = null,
                 sizeLevel: String? = null, activityLevel: String? = null,
                 keyword: String? = null, refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) { currentPage = 1; hasMore = true }
            _isLoading.value = true
            _error.value = null

            var minAge: Int? = null
            var maxAge: Int? = null
            when (ageRange) {
                "baby" -> maxAge = 0
                "young" -> { minAge = 1; maxAge = 3 }
                "adult" -> { minAge = 3; maxAge = 7 }
                "senior" -> minAge = 7
            }

            repo.petList(
                page = currentPage,
                species = type,
                sizeLevel = sizeLevel,
                activityLevel = activityLevel,
                minAge = minAge,
                maxAge = maxAge
            ).fold(
                onSuccess = { list ->
                    _pets.value = if (refresh) list else _pets.value + list
                    hasMore = list.size >= pageSize
                    if (hasMore) currentPage++
                    _isLoading.value = false
                },
                onFailure = {
                    _error.value = it.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun loadFavoriteIds() {
        viewModelScope.launch {
            repo.petFavoriteIds().fold(
                onSuccess = { _favoriteIds.value = it.toSet() },
                onFailure = { }
            )
        }
    }

    fun toggleFavorite(petId: Long) {
        viewModelScope.launch {
            repo.petFavorite(petId).fold(
                onSuccess = {
                    val current = _favoriteIds.value.toMutableSet()
                    if (petId in current) current.remove(petId) else current.add(petId)
                    _favoriteIds.value = current
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearError() { _error.value = null }
}
