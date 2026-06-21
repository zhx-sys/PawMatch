package com.pawmatch.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.data.model.*
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentChat = MutableStateFlow<ChatTarget?>(null)
    val currentChat: StateFlow<ChatTarget?> = _currentChat.asStateFlow()

    val myId: Long get() = TokenManager.userId
    val myType: Int get() = TokenManager.userType

    fun loadConversations() {
        viewModelScope.launch {
            repo.getConversations().fold(
                onSuccess = { _conversations.value = it },
                onFailure = { }
            )
        }
    }

    fun openChat(otherUserId: Long, otherUserType: Int, nickname: String? = null) {
        _currentChat.value = ChatTarget(otherUserId, otherUserType, nickname)
        loadMessages(otherUserId, otherUserType)
    }

    fun closeChat() {
        _currentChat.value = null
        _messages.value = emptyList()
    }

    fun loadMessages(otherUserId: Long, otherUserType: Int) {
        viewModelScope.launch {
            repo.getConversation(otherUserId, otherUserType).fold(
                onSuccess = { _messages.value = it },
                onFailure = { }
            )
        }
    }

    fun sendMessage(content: String) {
        val chat = _currentChat.value ?: return
        viewModelScope.launch {
            repo.sendMessage(chat.otherUserId, chat.otherUserType, content).fold(
                onSuccess = {
                    loadMessages(chat.otherUserId, chat.otherUserType)
                    loadConversations()
                },
                onFailure = { }
            )
        }
    }
}

data class ChatTarget(
    val otherUserId: Long,
    val otherUserType: Int,
    val nickname: String? = null
) {
    val displayName: String get() = nickname ?: if (otherUserType == 1) "救助站${otherUserId}" else "用户${otherUserId}"
}

class WikiViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _categories = MutableStateFlow<List<WikiCategory>>(emptyList())
    val categories: StateFlow<List<WikiCategory>> = _categories.asStateFlow()

    private val _entries = MutableStateFlow<List<WikiEntryItem>>(emptyList())
    val entries: StateFlow<List<WikiEntryItem>> = _entries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<WikiEntryDetail?>(null)
    val selectedEntry: StateFlow<WikiEntryDetail?> = _selectedEntry.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentCategoryId = MutableStateFlow<Long?>(null)
    val currentCategoryId: StateFlow<Long?> = _currentCategoryId.asStateFlow()

    private val _currentCategoryName = MutableStateFlow("全部词条")
    val currentCategoryName: StateFlow<String> = _currentCategoryName.asStateFlow()

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    private val _sortBy = MutableStateFlow("newest")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    var currentPage = 1; private set
    var pageSize = 10; private set
    var total = 0L; private set
    var hasMore = true; private set

    fun loadCategories() {
        viewModelScope.launch {
            repo.wikiCategories().fold(
                onSuccess = { _categories.value = it },
                onFailure = { }
            )
        }
    }

    fun loadEntries(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) { currentPage = 1; hasMore = true }
            _isLoading.value = true
            repo.wikiEntryList(
                currentPage, pageSize,
                _currentCategoryId.value,
                _keyword.value.ifBlank { null },
                _sortBy.value
            ).fold(
                onSuccess = { page ->
                    _entries.value = if (refresh) (page?.records ?: emptyList()) else _entries.value + (page?.records ?: emptyList())
                    total = page?.total ?: 0
                    hasMore = currentPage * pageSize < total
                    if (hasMore) currentPage++
                    _isLoading.value = false
                },
                onFailure = { _isLoading.value = false }
            )
        }
    }

    fun selectCategory(categoryId: Long?, name: String) {
        _currentCategoryId.value = categoryId
        _currentCategoryName.value = name
        loadEntries(refresh = true)
    }

    fun search(keyword: String) {
        _keyword.value = keyword
        loadEntries(refresh = true)
    }

    fun updateSortBy(sort: String) {
        _sortBy.value = sort
        loadEntries(refresh = true)
    }

    fun loadEntryDetail(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.wikiEntryDetail(id).fold(
                onSuccess = { _selectedEntry.value = it; _isLoading.value = false },
                onFailure = { _isLoading.value = false }
            )
        }
    }

    fun clearSelection() { _selectedEntry.value = null }
}

class CommunityViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _selectedPost = MutableStateFlow<PostDetail?>(null)
    val selectedPost: StateFlow<PostDetail?> = _selectedPost.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError.asStateFlow()

    private val _friendIds = MutableStateFlow<Set<Long>>(emptySet())
    val friendIds: StateFlow<Set<Long>> = _friendIds.asStateFlow()

    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.postList().fold(
                onSuccess = { _posts.value = it; _isLoading.value = false },
                onFailure = { _isLoading.value = false }
            )
            repo.friendList().fold(
                onSuccess = { _friendIds.value = it.map { f -> f.friendId }.toSet() },
                onFailure = { }
            )
        }
    }

    fun loadPostDetail(id: Long) {
        viewModelScope.launch {
            repo.postDetail(id).fold(
                onSuccess = { _selectedPost.value = it },
                onFailure = { }
            )
        }
    }

    fun likePost(id: Long) {
        viewModelScope.launch {
            repo.likePost(id)
        }
    }

    fun sendFriendRequest(post: Post) {
        viewModelScope.launch {
            repo.sendFriendRequest(post.userId, post.userType).fold(
                onSuccess = { _friendIds.value = _friendIds.value + post.userId },
                onFailure = { }
            )
        }
    }

    fun reportPost(reporterId: Long, targetId: Long, reason: String) {
        viewModelScope.launch {
            repo.createReport(ReportRequest(reporterId, "POST", targetId, reason)).fold(
                onSuccess = { },
                onFailure = { _reportError.value = it.message }
            )
        }
    }

    fun takeDownPost(postId: Long) {
        viewModelScope.launch {
            repo.takeDownPost(postId).fold(
                onSuccess = { loadPosts() },
                onFailure = { _reportError.value = it.message }
            )
        }
    }

    fun clearReportError() { _reportError.value = null }
}

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun loadNotifications() { load() }

    fun load() {
        viewModelScope.launch {
            repo.notifications().fold(
                onSuccess = { _notifications.value = it },
                onFailure = { }
            )
            try { _unreadCount.value = repo.unreadNotificationCount() } catch (_: Exception) {}
        }
    }

    fun markAsRead(id: Long) { markRead(id) }

    fun markRead(id: Long) {
        viewModelScope.launch {
            repo.markNotificationRead(id)
            _notifications.value = _notifications.value.map {
                if (it.id == id) it.copy(read = true, isRead = true) else it
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            _notifications.value.filter { !it.read && !it.isRead }.forEach {
                repo.markNotificationRead(it.id)
            }
            _notifications.value = _notifications.value.map { it.copy(read = true, isRead = true) }
        }
    }
}

class PetGameViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _applying = MutableStateFlow(false)
    val applying: StateFlow<Boolean> = _applying.asStateFlow()

    private val _applySuccess = MutableStateFlow(false)
    val applySuccess: StateFlow<Boolean> = _applySuccess.asStateFlow()

    fun loadPets() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.petList(page = 1).fold(
                onSuccess = { _pets.value = it; _isLoading.value = false },
                onFailure = { _isLoading.value = false }
            )
        }
    }

    fun selectPet(pet: Pet) { _selectedPet.value = pet }

    fun clearSelection() { _selectedPet.value = null; _applySuccess.value = false }

    fun applyAdoption(reason: String, experience: String, housingCondition: String) {
        val pet = _selectedPet.value ?: return
        viewModelScope.launch {
            _applying.value = true
            repo.applyAdoption(pet.id, reason, experience, housingCondition, true).fold(
                onSuccess = {
                    _applying.value = false
                    _applySuccess.value = true
                },
                onFailure = {
                    _applying.value = false
                }
            )
        }
    }
}
