package com.pawmatch.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawmatch.app.data.api.ApiClient
import com.pawmatch.app.data.api.ServerConfigManager
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.data.model.*
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _isLoggedIn = MutableStateFlow(TokenManager.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _registeredAccount = MutableStateFlow<String?>(null)
    val registeredAccount: StateFlow<String?> = _registeredAccount.asStateFlow()

    fun updateServerUrl(url: String) {
        ServerConfigManager.serverUrl = url
        ApiClient.rebuild(ServerConfigManager.serverUrl)
    }

    fun login(account: String, password: String, userType: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.login(account, password, userType).fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _isLoading.value = false
                },
                onFailure = {
                    _error.value = it.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun register(password: String, confirmPassword: String, nickname: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.register(password, confirmPassword, nickname).fold(
                onSuccess = { resp ->
                    _isLoading.value = false
                    _registeredAccount.value = resp.account
                },
                onFailure = {
                    _error.value = it.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _isLoggedIn.value = false
        }
    }

    private val _userInfo = MutableStateFlow(UserInfo())
    val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

    fun loadUserInfo() {
        viewModelScope.launch {
            repo.getUserInfo().fold(
                onSuccess = { _userInfo.value = it },
                onFailure = { }
            )
        }
    }

    init {
        if (TokenManager.isLoggedIn) loadUserInfo()
    }

    fun clearError() { _error.value = null }

    fun clearRegisteredAccount() { _registeredAccount.value = null }
}

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _selectedPet = MutableStateFlow<PetDetail?>(null)
    val selectedPet: StateFlow<PetDetail?> = _selectedPet.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var currentPage = 1
        private set
    var hasMore = true
        private set

    fun loadPets(species: String? = null, breed: String? = null, gender: String? = null,
                 sizeLevel: String? = null, activityLevel: String? = null,
                 minAge: Int? = null, maxAge: Int? = null, refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) { currentPage = 1; hasMore = true }
            _isLoading.value = true
            repo.petList(currentPage, species, breed, gender, sizeLevel, activityLevel, minAge, maxAge)
                .fold(
                    onSuccess = { list ->
                        _pets.value = if (refresh) list else _pets.value + list
                        hasMore = list.size >= 20
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

    fun searchPets(keyword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.petSearch(keyword).fold(
                onSuccess = { _pets.value = it; _isLoading.value = false },
                onFailure = { _error.value = it.message; _isLoading.value = false }
            )
        }
    }

    fun loadPetDetail(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.petDetail(id).fold(
                onSuccess = { _selectedPet.value = it; _isLoading.value = false },
                onFailure = { _error.value = it.message; _isLoading.value = false }
            )
        }
    }

    fun loadFavoriteIds() {
        viewModelScope.launch {
            repo.favoriteIds().fold(
                onSuccess = { _favoriteIds.value = it },
                onFailure = { }
            )
        }
    }

    fun toggleFavorite(petId: Long) {
        viewModelScope.launch {
            repo.toggleFavorite(petId).fold(
                onSuccess = {
                    val current = _favoriteIds.value.toMutableSet()
                    if (current.contains(petId)) current.remove(petId) else current.add(petId)
                    _favoriteIds.value = current
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearError() { _error.value = null }

    // ===== 收藏列表 =====
    private val _favoritePets = MutableStateFlow<List<FavoritePet>>(emptyList())
    val favoritePets: StateFlow<List<FavoritePet>> = _favoritePets.asStateFlow()

    fun loadFavoriteList() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.favoriteList().fold(
                onSuccess = { _favoritePets.value = it; _isLoading.value = false },
                onFailure = { _error.value = it.message; _isLoading.value = false }
            )
        }
    }

    // ===== 领养 =====
    private val _myAdoptions = MutableStateFlow<List<AdoptionApplication>>(emptyList())
    val myAdoptions: StateFlow<List<AdoptionApplication>> = _myAdoptions.asStateFlow()

    fun loadMyAdoptions() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.myAdoptions().fold(
                onSuccess = { _myAdoptions.value = it; _isLoading.value = false },
                onFailure = { _error.value = it.message; _isLoading.value = false }
            )
        }
    }

    private val _adoptionResult = MutableStateFlow<String?>(null)
    val adoptionResult: StateFlow<String?> = _adoptionResult.asStateFlow()

    fun applyAdoption(petId: Long, reason: String, experience: String, housingCondition: String, confirmFlood: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.applyAdoption(petId, reason, experience, housingCondition, confirmFlood).fold(
                onSuccess = {
                    _isLoading.value = false
                    _adoptionResult.value = "申请已提交，请等待救助站审核"
                },
                onFailure = {
                    _isLoading.value = false
                    _adoptionResult.value = it.message ?: "申请失败"
                }
            )
        }
    }

    fun clearAdoptionResult() { _adoptionResult.value = null }

    // ===== 智能推荐 =====
    private val _matchedPets = MutableStateFlow<List<MatchedPet>>(emptyList())
    val matchedPets: StateFlow<List<MatchedPet>> = _matchedPets.asStateFlow()

    private val _hasProfile = MutableStateFlow<Boolean?>(null)
    val hasProfile: StateFlow<Boolean?> = _hasProfile.asStateFlow()

    fun loadRecommendations() {
        viewModelScope.launch {
            repo.getMatchingProfile().fold(
                onSuccess = { profile ->
                    _hasProfile.value = profile?.matchingProfileComplete == true
                    if (profile?.matchingProfileComplete == true) {
                        repo.getMatchingRecommend().fold(
                            onSuccess = { _matchedPets.value = it },
                            onFailure = { }
                        )
                    }
                },
                onFailure = { _hasProfile.value = false }
            )
        }
    }
}

class FriendViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _friends = MutableStateFlow<List<FriendResponse>>(emptyList())
    val friends: StateFlow<List<FriendResponse>> = _friends.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<FriendResponse>>(emptyList())
    val pendingRequests: StateFlow<List<FriendResponse>> = _pendingRequests.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchUser>>(emptyList())
    val searchResults: StateFlow<List<SearchUser>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadFriends() {
        viewModelScope.launch {
            repo.friendList().fold(
                onSuccess = { _friends.value = it },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            repo.pendingRequests().fold(
                onSuccess = { _pendingRequests.value = it },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun searchUsers(keyword: String) {
        viewModelScope.launch {
            repo.searchUsers(keyword).fold(
                onSuccess = { _searchResults.value = it },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun sendFriendRequest(user: SearchUser) {
        viewModelScope.launch {
            repo.sendFriendRequest(user.id, user.userType).fold(
                onSuccess = {
                    val updated = _searchResults.value.map {
                        if (it.id == user.id) it.copy(added = true) else it
                    }
                    _searchResults.value = updated
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun acceptFriend(id: Long) {
        viewModelScope.launch {
            repo.acceptFriend(id).fold(
                onSuccess = {
                    _pendingRequests.value = _pendingRequests.value.filter { it.id != id }
                    loadFriends()
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun rejectFriend(id: Long) {
        viewModelScope.launch {
            repo.rejectFriend(id).fold(
                onSuccess = { _pendingRequests.value = _pendingRequests.value.filter { it.id != id } },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun deleteFriend(friendId: Long) {
        viewModelScope.launch {
            repo.deleteFriend(friendId).fold(
                onSuccess = { loadFriends() },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearError() { _error.value = null }
}
