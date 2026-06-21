package com.pawmatch.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawmatch.app.data.api.ShelterProfileData
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.data.model.*
import com.pawmatch.app.data.repository.PawMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ShelterViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PawMatchRepository()

    // ===== 宠物管理 =====
    private val _shelterPets = MutableStateFlow<List<Pet>>(emptyList())
    val shelterPets: StateFlow<List<Pet>> = _shelterPets.asStateFlow()

    private val _isPetsLoading = MutableStateFlow(false)
    val isPetsLoading: StateFlow<Boolean> = _isPetsLoading.asStateFlow()

    private val _createPetResult = MutableStateFlow<String?>(null)
    val createPetResult: StateFlow<String?> = _createPetResult.asStateFlow()

    private val _deletePetResult = MutableStateFlow<String?>(null)
    val deletePetResult: StateFlow<String?> = _deletePetResult.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    fun loadShelterPets() {
        viewModelScope.launch {
            _isPetsLoading.value = true
            repo.petList(page = 1, species = null).fold(
                onSuccess = { _shelterPets.value = it; _isPetsLoading.value = false },
                onFailure = { _isPetsLoading.value = false }
            )
        }
    }

    fun createPet(request: CreatePetRequest) {
        viewModelScope.launch {
            _isPetsLoading.value = true
            repo.createPet(request).fold(
                onSuccess = {
                    _isPetsLoading.value = false
                    _createPetResult.value = "success"
                    loadShelterPets()
                },
                onFailure = {
                    _isPetsLoading.value = false
                    _createPetResult.value = it.message ?: "发布失败"
                }
            )
        }
    }

    fun deletePet(petId: Long) {
        viewModelScope.launch {
            repo.deletePet(petId).fold(
                onSuccess = {
                    _deletePetResult.value = "success"
                    loadShelterPets()
                },
                onFailure = {
                    _deletePetResult.value = it.message ?: "删除失败"
                }
            )
        }
    }

    fun clearCreatePetResult() { _createPetResult.value = null }
    fun clearDeletePetResult() { _deletePetResult.value = null }

    fun uploadImage(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val file = uriToFile(uri) ?: run {
                    _isUploading.value = false
                    onError("无法读取图片")
                    return@launch
                }
                repo.uploadImage(file).fold(
                    onSuccess = {
                        _isUploading.value = false
                        onSuccess(it)
                    },
                    onFailure = {
                        _isUploading.value = false
                        onError(it.message ?: "上传失败")
                    }
                )
            } catch (e: Exception) {
                _isUploading.value = false
                onError(e.message ?: "上传失败")
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "upload_${System.currentTimeMillis()}.jpg"
            val tempDir = File(context.cacheDir, "uploads")
            tempDir.mkdirs()
            val file = File(tempDir, fileName)
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    // ===== 领养审核 =====
    private val _adoptions = MutableStateFlow<List<AdoptionReview>>(emptyList())
    val adoptions: StateFlow<List<AdoptionReview>> = _adoptions.asStateFlow()

    private val _isAdoptionsLoading = MutableStateFlow(false)
    val isAdoptionsLoading: StateFlow<Boolean> = _isAdoptionsLoading.asStateFlow()

    private val _adoptionTotal = MutableStateFlow(0)
    val adoptionTotal: StateFlow<Int> = _adoptionTotal.asStateFlow()

    private val _auditResult = MutableStateFlow<String?>(null)
    val auditResult: StateFlow<String?> = _auditResult.asStateFlow()

    fun loadAdoptions(statusFilter: Int? = null, pageNum: Int = 1) {
        viewModelScope.launch {
            _isAdoptionsLoading.value = true
            repo.shelterAdoptions(pageNum, 10, statusFilter).fold(
                onSuccess = { _adoptions.value = it; _isAdoptionsLoading.value = false },
                onFailure = { _isAdoptionsLoading.value = false }
            )
            repo.shelterAdoptionTotal(pageNum, 10, statusFilter).fold(
                onSuccess = { _adoptionTotal.value = it },
                onFailure = { }
            )
        }
    }

    fun auditAdoption(id: Long, status: Int, rejectReason: String? = null) {
        viewModelScope.launch {
            repo.auditAdoption(id, status, rejectReason).fold(
                onSuccess = {
                    _auditResult.value = if (status == 1) "审核通过" else "已拒绝"
                },
                onFailure = {
                    _auditResult.value = it.message ?: "审核失败"
                }
            )
        }
    }

    fun completeAdoption(id: Long) {
        viewModelScope.launch {
            repo.completeAdoption(id).fold(
                onSuccess = {
                    _auditResult.value = "领养已完成"
                },
                onFailure = {
                    _auditResult.value = it.message ?: "操作失败"
                }
            )
        }
    }

    fun clearAuditResult() { _auditResult.value = null }

    // ===== 救助站信息 =====
    private val _shelterProfile = MutableStateFlow<ShelterProfileData?>(null)
    val shelterProfile: StateFlow<ShelterProfileData?> = _shelterProfile.asStateFlow()

    fun loadShelterProfile() {
        viewModelScope.launch {
            repo.shelterProfile(TokenManager.userId).fold(
                onSuccess = { _shelterProfile.value = it },
                onFailure = { }
            )
        }
    }

    val myId: Long get() = TokenManager.userId

    // ===== 帖子审核 =====
    private val _postReviews = MutableStateFlow<List<PostReviewItem>>(emptyList())
    val postReviews: StateFlow<List<PostReviewItem>> = _postReviews.asStateFlow()

    private val _isPostReviewsLoading = MutableStateFlow(false)
    val isPostReviewsLoading: StateFlow<Boolean> = _isPostReviewsLoading.asStateFlow()

    private val _postReviewTotal = MutableStateFlow(0)
    val postReviewTotal: StateFlow<Int> = _postReviewTotal.asStateFlow()

    private val _postReviewResult = MutableStateFlow<String?>(null)
    val postReviewResult: StateFlow<String?> = _postReviewResult.asStateFlow()

    fun loadPostReviews(pageNum: Int = 1) {
        viewModelScope.launch {
            _isPostReviewsLoading.value = true
            repo.postReviewList(pageNum).fold(
                onSuccess = { _postReviews.value = it; _isPostReviewsLoading.value = false },
                onFailure = { _isPostReviewsLoading.value = false }
            )
            repo.postReviewTotal(pageNum).fold(
                onSuccess = { _postReviewTotal.value = it },
                onFailure = { }
            )
        }
    }

    fun reviewPost(id: Long, approved: Boolean) {
        viewModelScope.launch {
            repo.reviewPost(id, approved).fold(
                onSuccess = { _postReviewResult.value = if (approved) "审核通过" else "已拒绝" },
                onFailure = { _postReviewResult.value = it.message ?: "操作失败" }
            )
        }
    }

    fun clearPostReviewResult() { _postReviewResult.value = null }

    // ===== 举报审核 =====
    private val _reportItems = MutableStateFlow<List<ReportItem>>(emptyList())
    val reportItems: StateFlow<List<ReportItem>> = _reportItems.asStateFlow()

    private val _isReportsLoading = MutableStateFlow(false)
    val isReportsLoading: StateFlow<Boolean> = _isReportsLoading.asStateFlow()

    private val _reportReviewResult = MutableStateFlow<String?>(null)
    val reportReviewResult: StateFlow<String?> = _reportReviewResult.asStateFlow()

    fun loadReports() {
        viewModelScope.launch {
            _isReportsLoading.value = true
            repo.pendingReports().fold(
                onSuccess = { _reportItems.value = it; _isReportsLoading.value = false },
                onFailure = { _isReportsLoading.value = false }
            )
        }
    }

    fun reviewReport(id: Long, status: Int) {
        viewModelScope.launch {
            repo.reviewReport(id, status).fold(
                onSuccess = { _reportReviewResult.value = if (status == 1) "审核通过" else "已驳回" },
                onFailure = { _reportReviewResult.value = it.message ?: "操作失败" }
            )
        }
    }

    fun clearReportReviewResult() { _reportReviewResult.value = null }

    // ===== 百科审核 =====
    private val _wikiReviews = MutableStateFlow<List<WikiReviewItem>>(emptyList())
    val wikiReviews: StateFlow<List<WikiReviewItem>> = _wikiReviews.asStateFlow()

    private val _isWikiReviewsLoading = MutableStateFlow(false)
    val isWikiReviewsLoading: StateFlow<Boolean> = _isWikiReviewsLoading.asStateFlow()

    private val _wikiReviewTotal = MutableStateFlow(0)
    val wikiReviewTotal: StateFlow<Int> = _wikiReviewTotal.asStateFlow()

    private val _wikiReviewResult = MutableStateFlow<String?>(null)
    val wikiReviewResult: StateFlow<String?> = _wikiReviewResult.asStateFlow()

    fun loadWikiReviews(pageNum: Int = 1) {
        viewModelScope.launch {
            _isWikiReviewsLoading.value = true
            repo.wikiReviewList(pageNum).fold(
                onSuccess = { _wikiReviews.value = it; _isWikiReviewsLoading.value = false },
                onFailure = { _isWikiReviewsLoading.value = false }
            )
            repo.wikiReviewTotal(pageNum).fold(
                onSuccess = { _wikiReviewTotal.value = it },
                onFailure = { }
            )
        }
    }

    fun reviewWikiEntry(id: Long, approved: Boolean) {
        viewModelScope.launch {
            repo.reviewWikiEntry(id, approved).fold(
                onSuccess = { _wikiReviewResult.value = if (approved) "审核通过" else "已拒绝" },
                onFailure = { _wikiReviewResult.value = it.message ?: "操作失败" }
            )
        }
    }

    fun clearWikiReviewResult() { _wikiReviewResult.value = null }

    // ===== 寄养管理 =====
    private val _fosterServices = MutableStateFlow<List<FosterServiceItem>>(emptyList())
    val fosterServices: StateFlow<List<FosterServiceItem>> = _fosterServices.asStateFlow()

    private val _fosterOrders = MutableStateFlow<List<FosterOrderItem>>(emptyList())
    val fosterOrders: StateFlow<List<FosterOrderItem>> = _fosterOrders.asStateFlow()

    private val _isFosterLoading = MutableStateFlow(false)
    val isFosterLoading: StateFlow<Boolean> = _isFosterLoading.asStateFlow()

    private val _fosterResult = MutableStateFlow<String?>(null)
    val fosterResult: StateFlow<String?> = _fosterResult.asStateFlow()

    private val _fosterServiceSaving = MutableStateFlow(false)
    val fosterServiceSaving: StateFlow<Boolean> = _fosterServiceSaving.asStateFlow()

    fun loadFosterData() {
        viewModelScope.launch {
            _isFosterLoading.value = true
            repo.fosterServiceList().fold(
                onSuccess = { _fosterServices.value = it },
                onFailure = { }
            )
            repo.fosterOrderList().fold(
                onSuccess = { _fosterOrders.value = it; _isFosterLoading.value = false },
                onFailure = { _isFosterLoading.value = false }
            )
        }
    }

    fun createFosterService(request: AddFosterServiceRequest) {
        viewModelScope.launch {
            _fosterServiceSaving.value = true
            repo.createFosterService(request).fold(
                onSuccess = {
                    _fosterServiceSaving.value = false
                    _fosterResult.value = "发布成功"
                    loadFosterData()
                },
                onFailure = {
                    _fosterServiceSaving.value = false
                    _fosterResult.value = it.message ?: "发布失败"
                }
            )
        }
    }

    fun updateFosterService(id: Long, request: UpdateFosterServiceRequest) {
        viewModelScope.launch {
            _fosterServiceSaving.value = true
            repo.updateFosterService(id, request).fold(
                onSuccess = {
                    _fosterServiceSaving.value = false
                    _fosterResult.value = "更新成功"
                    loadFosterData()
                },
                onFailure = {
                    _fosterServiceSaving.value = false
                    _fosterResult.value = it.message ?: "更新失败"
                }
            )
        }
    }

    fun deleteFosterService(id: Long) {
        viewModelScope.launch {
            repo.deleteFosterService(id).fold(
                onSuccess = {
                    _fosterResult.value = "已下架"
                    loadFosterData()
                },
                onFailure = { _fosterResult.value = it.message ?: "下架失败" }
            )
        }
    }

    fun confirmFosterOrder(id: Long) {
        viewModelScope.launch {
            repo.confirmFosterOrder(id).fold(
                onSuccess = {
                    _fosterResult.value = "已确认接单"
                    loadFosterData()
                },
                onFailure = { _fosterResult.value = it.message ?: "操作失败" }
            )
        }
    }

    fun completeFosterOrder(id: Long) {
        viewModelScope.launch {
            repo.completeFosterOrder(id).fold(
                onSuccess = {
                    _fosterResult.value = "已完成"
                    loadFosterData()
                },
                onFailure = { _fosterResult.value = it.message ?: "操作失败" }
            )
        }
    }

    fun clearFosterResult() { _fosterResult.value = null }
}