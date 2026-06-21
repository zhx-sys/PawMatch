package com.pawmatch.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawmatch.app.data.model.ShelterInfo
import com.pawmatch.app.data.model.ShelterStats
import com.pawmatch.app.data.model.ShelterStory
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShelterProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _shelterInfo = MutableStateFlow<ShelterInfo?>(null)
    val shelterInfo: StateFlow<ShelterInfo?> = _shelterInfo.asStateFlow()

    private val _stats = MutableStateFlow(ShelterStats())
    val stats: StateFlow<ShelterStats> = _stats.asStateFlow()

    private val _stories = MutableStateFlow<List<ShelterStory>>(emptyList())
    val stories: StateFlow<List<ShelterStory>> = _stories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProfile(shelterId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.shelterProfile(shelterId).fold(
                onSuccess = { data ->
                    _shelterInfo.value = data?.shelterInfo
                    _stats.value = data?.stats ?: ShelterStats()
                    _stories.value = data?.recentStories ?: emptyList()
                    _isLoading.value = false
                },
                onFailure = {
                    _error.value = it.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun clearError() { _error.value = null }
}
