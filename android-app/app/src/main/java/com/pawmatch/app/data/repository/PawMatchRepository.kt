package com.pawmatch.app.data.repository

import com.pawmatch.app.data.api.ApiClient
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PawMatchRepository {
    private val api get() = ApiClient.instance

    // Auth
    suspend fun login(account: String, password: String, userType: Int = 0): Result<LoginResponse> = runCatching {
        val res = api.login(LoginRequest(account, password, userType))
        if (res.code == 200 && res.data != null) {
            TokenManager.save(res.data)
            res.data
        } else throw Exception(res.message.ifEmpty { "登录失败" })
    }

    suspend fun getUserInfo() = runCatching {
        api.getUserInfo().data ?: UserInfo()
    }

    suspend fun register(password: String, confirmPassword: String, nickname: String): Result<RegisterResponse> = runCatching {
        val res = api.register(RegisterRequest(password = password, confirmPassword = confirmPassword, nickname = nickname))
        if (res.code == 200 && res.data != null) res.data
        else throw Exception(res.message.ifEmpty { "注册失败" })
    }

    suspend fun logout() { TokenManager.clear() }

    suspend fun updateUserInfo(oldPassword: String, newPassword: String): Result<Unit> = runCatching {
        val res = api.updateUserInfo(UpdateUserRequest(oldPassword, newPassword))
        if (res.code == 200) Unit
        else throw Exception(res.message.ifEmpty { "修改失败" })
    }

    // Pets
    suspend fun petList(page: Int = 1, species: String? = null, breed: String? = null,
                        gender: String? = null, sizeLevel: String? = null,
                        activityLevel: String? = null, minAge: Int? = null, maxAge: Int? = null) =
        runCatching {
            api.petList(page, 20, species, breed, gender, sizeLevel, activityLevel, minAge, maxAge).data?.records ?: emptyList()
        }

    suspend fun petSearch(keyword: String, page: Int = 1) =
        runCatching { api.petSearch(keyword, page).data?.records ?: emptyList() }

    suspend fun petDetail(id: Long) =
        runCatching { api.petDetail(id).data }

    // Adoption
    suspend fun applyAdoption(petId: Long, reason: String, experience: String, housingCondition: String, confirmFlood: Boolean) = runCatching {
        api.applyAdoption(ApplyAdoptionRequest(petId, reason, experience, housingCondition, confirmFlood)).let { }
    }

    suspend fun myAdoptions() = runCatching { api.myAdoptions().data?.records ?: emptyList() }

    // Friends
    suspend fun friendList() = runCatching {
        api.friendList(TokenManager.userId, TokenManager.userType).data ?: emptyList()
    }

    suspend fun pendingRequests() = runCatching {
        api.pendingRequests(TokenManager.userId, TokenManager.userType).data ?: emptyList()
    }

    suspend fun sendFriendRequest(friendId: Long, friendUserType: Int) = runCatching {
        api.sendFriendRequest(FriendRequest(
            TokenManager.userId, TokenManager.userType, friendId, friendUserType
        )).let { }
    }

    suspend fun acceptFriend(id: Long) = runCatching {
        api.acceptFriend(id, TokenManager.userId).let { }
    }

    suspend fun rejectFriend(id: Long) = runCatching {
        api.rejectFriend(id, TokenManager.userId).let { }
    }

    suspend fun deleteFriend(friendId: Long) = runCatching {
        api.deleteFriend(TokenManager.userId, friendId).let { }
    }

    suspend fun searchUsers(keyword: String) = runCatching {
        api.searchUsers(keyword, TokenManager.userId).data ?: emptyList()
    }

    // Messages
    suspend fun sendMessage(toUserId: Long, toUserType: Int, content: String) = runCatching {
        api.sendMessage(SendMessageRequest(
            TokenManager.userId, TokenManager.userType, toUserId, toUserType, content
        )).let { }
    }

    suspend fun getConversation(otherUserId: Long, otherUserType: Int) = runCatching {
        api.getConversation(otherUserId, otherUserType, TokenManager.userId, TokenManager.userType).data ?: emptyList()
    }

    suspend fun getConversations() = runCatching {
        api.getConversations(TokenManager.userId, TokenManager.userType).data ?: emptyList()
    }

    // Favorites
    suspend fun toggleFavorite(petId: Long) = runCatching {
        api.toggleFavorite(TokenManager.userId, petId).let { }
    }

    suspend fun favoriteIds() = runCatching {
        (api.favoriteIds(TokenManager.userId).data ?: emptyList()).toSet()
    }

    suspend fun favoriteList() = runCatching {
        api.favoriteList(TokenManager.userId).data ?: emptyList()
    }

    // Pet Favorites v2 (pet/favorite endpoint)
    suspend fun petFavorite(petId: Long, userId: Long = TokenManager.userId) = runCatching {
        api.petFavorite(userId, petId).let { }
    }

    suspend fun petFavoriteList(userId: Long = TokenManager.userId) = runCatching {
        api.petFavoriteList(userId).data ?: emptyList()
    }

    suspend fun petFavoriteIds(userId: Long = TokenManager.userId) = runCatching {
        api.petFavoriteIds(userId).data ?: emptyList()
    }

    // Community
    suspend fun postList(page: Int = 1) = runCatching {
        api.postList(page).data?.records ?: emptyList()
    }

    suspend fun postDetail(id: Long) = runCatching { api.postDetail(id).data }

    suspend fun likePost(id: Long) = runCatching { api.likePost(id).let { } }

    suspend fun createComment(request: CreateCommentRequest) = runCatching {
        api.createComment(request).let { }
    }

    // Report
    suspend fun createReport(request: ReportRequest) = runCatching { api.createReport(request).let {} }
    suspend fun pendingReports(): Result<List<ReportItem>> = runCatching { api.pendingReports().data ?: emptyList() }
    suspend fun reviewReport(id: Long, status: Int) = runCatching { api.reviewReport(id, mapOf("status" to status)).let {} }
    // Take down
    suspend fun takeDownPost(postId: Long) = runCatching { api.takeDownPost(postId).let {} }

    // Notifications
    suspend fun notifications() = runCatching {
        api.notifications(TokenManager.userId, TokenManager.userType).data ?: emptyList()
    }

    suspend fun markNotificationRead(id: Long) = runCatching {
        api.markNotificationRead(id).let { }
    }

    suspend fun unreadNotificationCount(): Int {
        return api.unreadNotificationCount(TokenManager.userId, TokenManager.userType).data ?: 0
    }

    // Shelter
    suspend fun shelterProfile(shelterId: Long) = runCatching {
        api.shelterProfile(shelterId).data
    }

    // 救助站 - 宠物管理
    suspend fun createPet(request: CreatePetRequest) = runCatching {
        api.createPet(request)
    }

    suspend fun uploadImage(file: File): Result<String> = runCatching {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        api.uploadImage(part).data ?: throw Exception("上传失败")
    }

    suspend fun deletePet(petId: Long) = runCatching {
        api.deletePet(petId).let { }
    }

    // 救助站 - 领养审核
    suspend fun shelterAdoptions(pageNum: Int = 1, pageSize: Int = 10, status: Int? = null) = runCatching {
        api.shelterAdoptions(pageNum, pageSize, status).let { resp ->
            resp.data?.records ?: emptyList()
        }
    }

    suspend fun shelterAdoptionTotal(pageNum: Int = 1, pageSize: Int = 10, status: Int? = null) = runCatching {
        api.shelterAdoptions(pageNum, pageSize, status).let { resp ->
            (resp.data?.total ?: 0).toInt()
        }
    }

    suspend fun auditAdoption(id: Long, status: Int, rejectReason: String? = null) = runCatching {
        api.auditAdoption(id, AuditAdoptionRequest(status, rejectReason)).let { }
    }

    suspend fun completeAdoption(id: Long) = runCatching {
        api.completeAdoption(id).let { }
    }

    // 救助站 - 帖子审核
    suspend fun postReviewList(pageNum: Int = 1, pageSize: Int = 10) = runCatching {
        api.postReviewList(pageNum, pageSize).let { resp ->
            resp.data?.records ?: emptyList()
        }
    }

    suspend fun postReviewTotal(pageNum: Int = 1, pageSize: Int = 10) = runCatching {
        api.postReviewList(pageNum, pageSize).let { resp ->
            (resp.data?.total ?: 0).toInt()
        }
    }

    suspend fun reviewPost(id: Long, approved: Boolean) = runCatching {
        api.reviewPost(id, mapOf("approved" to approved)).let { }
    }

    // 救助站 - 百科审核
    suspend fun wikiReviewList(pageNum: Int = 1, pageSize: Int = 10) = runCatching {
        api.wikiReviewList(pageNum, pageSize).let { resp ->
            resp.data?.records ?: emptyList()
        }
    }

    suspend fun wikiReviewTotal(pageNum: Int = 1, pageSize: Int = 10) = runCatching {
        api.wikiReviewList(pageNum, pageSize).let { resp ->
            (resp.data?.total ?: 0).toInt()
        }
    }

    suspend fun reviewWikiEntry(id: Long, approved: Boolean) = runCatching {
        api.reviewWikiEntry(id, mapOf("approved" to approved)).let { }
    }

    // 救助站 - 寄养服务
    suspend fun fosterServiceList(pageNum: Int = 1, pageSize: Int = 100) = runCatching {
        api.fosterServiceList(pageNum, pageSize).let { resp ->
            resp.data?.records ?: emptyList()
        }
    }

    suspend fun createFosterService(request: AddFosterServiceRequest) = runCatching {
        api.createFosterService(request).let { }
    }

    suspend fun updateFosterService(id: Long, request: UpdateFosterServiceRequest) = runCatching {
        api.updateFosterService(id, request).let { }
    }

    suspend fun deleteFosterService(id: Long) = runCatching {
        api.deleteFosterService(id).let { }
    }

    // 救助站 - 寄养订单
    suspend fun fosterOrderList(pageNum: Int = 1, pageSize: Int = 100) = runCatching {
        api.fosterOrderList(pageNum, pageSize).let { resp ->
            resp.data?.records ?: emptyList()
        }
    }

    suspend fun confirmFosterOrder(id: Long) = runCatching {
        api.confirmFosterOrder(id).let { }
    }

    suspend fun completeFosterOrder(id: Long) = runCatching {
        api.completeFosterOrder(id).let { }
    }

    // ===== 寄养（普通用户） =====
    suspend fun searchFosterServices(pageNum: Int = 1, pageSize: Int = 100, keyword: String? = null, petType: String? = null) = runCatching {
        // Use the existing fosterServiceList which calls foster/service/search
        api.fosterServiceList(pageNum, pageSize).let { resp ->
            resp.data?.records ?: emptyList()
        }
    }

    suspend fun createFosterOrder(request: CreateFosterOrderRequest) = runCatching {
        api.createFosterOrder(request)
    }

    suspend fun myFosterOrders(pageNum: Int = 1, pageSize: Int = 50) = runCatching {
        api.myFosterOrders(pageNum, pageSize).data ?: emptyList()
    }

    suspend fun cancelFosterOrder(orderId: Long) = runCatching {
        api.cancelFosterOrder(orderId).let { }
    }

    suspend fun reviewFosterOrder(orderId: Long, request: ReviewRequest) = runCatching {
        api.reviewFosterOrder(orderId, request).let { }
    }

    // ===== 匹配画像 =====
    suspend fun getMatchingProfile() = runCatching {
        api.getMatchingProfile().data
    }

    suspend fun saveMatchingProfile(request: MatchingProfileRequest) = runCatching {
        api.saveMatchingProfile(request).let { }
    }

    suspend fun getMatchingRecommend() = runCatching { api.getMatchingRecommend().data ?: emptyList() }

    // ===== 领养回访 =====
    suspend fun createFollowup(request: FollowupRequest) = runCatching {
        api.createFollowup(request)
    }

    suspend fun getFollowupsByAdoption(adoptionId: Long) = runCatching {
        api.getFollowupsByAdoption(adoptionId).data ?: emptyList()
    }

    suspend fun getFollowupsByShelter(shelterId: Long) = runCatching {
        api.getFollowupsByShelter(shelterId).data ?: emptyList()
    }

    // ===== 信用分 =====
    suspend fun getCreditLogs(pageNum: Int = 1, pageSize: Int = 20) = runCatching {
        api.getCreditLogs(pageNum, pageSize).data?.records ?: emptyList()
    }

    // ===== 成长激励 =====
    suspend fun dailyCheckin() = runCatching {
        api.dailyCheckin().data ?: emptyMap()
    }

    suspend fun getMyPoints() = runCatching {
        api.getMyPoints().data ?: emptyMap()
    }

    suspend fun getMyBadges() = runCatching {
        api.getMyBadges().data ?: emptyList()
    }

    suspend fun getPointsLog(pageNum: Int = 1, pageSize: Int = 20) = runCatching {
        api.getPointsLog(pageNum, pageSize).data
    }

    // ===== 百科（普通用户） =====
    suspend fun wikiCategories() = runCatching {
        api.wikiCategories().data ?: emptyList()
    }

    suspend fun wikiEntryList(
        pageNum: Int = 1, pageSize: Int = 10,
        categoryId: Long? = null, keyword: String? = null,
        sortBy: String = "newest"
    ) = runCatching {
        api.wikiEntryList(pageNum, pageSize, categoryId, keyword, sortBy).data
    }

    suspend fun wikiEntryDetail(id: Long) = runCatching {
        api.wikiEntryDetail(id).data
    }

    suspend fun createWikiEntry(request: WikiEntryCreateRequest) = runCatching {
        api.createWikiEntry(request).data ?: -1L
    }

    suspend fun editWikiEntry(id: Long, request: WikiEntryEditRequest) = runCatching {
        api.editWikiEntry(id, request).let { }
    }
}
