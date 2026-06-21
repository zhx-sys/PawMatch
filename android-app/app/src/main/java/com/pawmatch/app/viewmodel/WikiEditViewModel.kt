package com.pawmatch.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawmatch.app.data.model.WikiCategory
import com.pawmatch.app.data.model.WikiEntryCreateRequest
import com.pawmatch.app.data.model.WikiEntryDetail
import com.pawmatch.app.data.model.WikiEntryEditRequest
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WikiEditViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    val isEdit: Boolean get() = _entryId.value != null
    private val _entryId = MutableStateFlow<Long?>(null)

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _summary = MutableStateFlow("")
    val summary: StateFlow<String> = _summary.asStateFlow()

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()

    private val _editSummary = MutableStateFlow("")
    val editSummary: StateFlow<String> = _editSummary.asStateFlow()

    private val _categories = MutableStateFlow<List<WikiCategory>>(emptyList())
    val categories: StateFlow<List<WikiCategory>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    private val _submittedEntryId = MutableStateFlow<Long?>(null)
    val submittedEntryId: StateFlow<Long?> = _submittedEntryId.asStateFlow()

    fun init(entryId: Long?) {
        _entryId.value = entryId
        loadCategories()
        if (entryId != null) loadEntry(entryId)
    }

    fun updateTitle(value: String) { _title.value = value }
    fun updateContent(value: String) { _content.value = value }
    fun updateSummary(value: String) { _summary.value = value }
    fun updateCategoryId(value: Long?) { _categoryId.value = value }
    fun updateEditSummary(value: String) { _editSummary.value = value }

    private fun loadCategories() {
        viewModelScope.launch {
            repo.wikiCategories().fold(
                onSuccess = { _categories.value = it },
                onFailure = { }
            )
        }
    }

    private fun loadEntry(entryId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.wikiEntryDetail(entryId).fold(
                onSuccess = { entry ->
                    entry?.let {
                        _title.value = it.title
                        _content.value = it.content ?: ""
                        _summary.value = it.summary ?: ""
                        _categoryId.value = it.categoryId
                    }
                    _isLoading.value = false
                },
                onFailure = {
                    _error.value = it.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun submit() {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            val id = _entryId.value

            if (id != null) {
                val req = WikiEntryEditRequest(
                    title = _title.value,
                    content = _content.value,
                    summary = _summary.value.ifBlank { null },
                    categoryId = _categoryId.value,
                    editSummary = _editSummary.value.ifBlank { null }
                )
                repo.editWikiEntry(id, req).fold(
                    onSuccess = { _submitSuccess.value = true; _isSubmitting.value = false },
                    onFailure = { _error.value = it.message; _isSubmitting.value = false }
                )
            } else {
                val req = WikiEntryCreateRequest(
                    title = _title.value,
                    content = _content.value,
                    summary = _summary.value.ifBlank { null },
                    categoryId = _categoryId.value
                )
                repo.createWikiEntry(req).fold(
                    onSuccess = { id ->
                        _submittedEntryId.value = id.takeIf { it > 0 }
                        _submitSuccess.value = true
                        _isSubmitting.value = false
                    },
                    onFailure = { _error.value = it.message; _isSubmitting.value = false }
                )
            }
        }
    }

    fun clearError() { _error.value = null }
    fun resetSubmitSuccess() { _submitSuccess.value = false; _submittedEntryId.value = null }
}
