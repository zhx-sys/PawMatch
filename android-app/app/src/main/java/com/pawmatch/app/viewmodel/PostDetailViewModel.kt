package com.pawmatch.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawmatch.app.data.model.CreateCommentRequest
import com.pawmatch.app.data.model.PostDetail
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    private val _postDetail = MutableStateFlow<PostDetail?>(null)
    val postDetail: StateFlow<PostDetail?> = _postDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLiking = MutableStateFlow(false)
    val isLiking: StateFlow<Boolean> = _isLiking.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPostDetail(postId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repo.postDetail(postId).fold(
                onSuccess = { _postDetail.value = it; _isLoading.value = false },
                onFailure = { _error.value = it.message; _isLoading.value = false }
            )
        }
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            _isLiking.value = true
            repo.likePost(postId).fold(
                onSuccess = { loadPostDetail(postId) },
                onFailure = { _error.value = it.message }
            )
            _isLiking.value = false
        }
    }

    fun submitComment(postId: Long, content: String) {
        viewModelScope.launch {
            _isSending.value = true
            _error.value = null
            val request = CreateCommentRequest(postId = postId, content = content)
            repo.createComment(request).fold(
                onSuccess = {
                    _isSending.value = false
                    loadPostDetail(postId)
                },
                onFailure = { _error.value = it.message; _isSending.value = false }
            )
        }
    }

    fun clearError() { _error.value = null }
}
