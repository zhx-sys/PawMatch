package com.pawmatch.app.data.api

import com.pawmatch.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// 服务器地址由 ServerConfigManager 动态管理，登录页可修改

interface PawMatchApi {

    // ===== 认证 =====
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/register/user")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<RegisterResponse>

    @GET("user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>

    @PUT("user/info")
    suspend fun updateUserInfo(@Body body: UpdateUserRequest): ApiResponse<Void>

    // ===== 宠物 =====
    @GET("pet/search")
    suspend fun petList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("species") species: String? = null,
        @Query("breed") breed: String? = null,
        @Query("gender") gender: String? = null,
        @Query("sizeLevel") sizeLevel: String? = null,
        @Query("activityLevel") activityLevel: String? = null,
        @Query("minAge") minAge: Int? = null,
        @Query("maxAge") maxAge: Int? = null
    ): ApiResponse<PetListPage>

    @GET("pet/search")
    suspend fun petSearch(
        @Query("keyword") keyword: String,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PetListPage>

    @GET("pet/{id}")
    suspend fun petDetail(@Path("id") id: Long): ApiResponse<PetDetail>

    // ===== 领养 =====
    @POST("adoption/apply")
    suspend fun applyAdoption(@Body body: ApplyAdoptionRequest): ApiResponse<Long>

    @GET("adoption/my")
    suspend fun myAdoptions(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): ApiResponse<PageData<AdoptionApplication>>

    // ===== 好友 =====
    @POST("friend/request")
    suspend fun sendFriendRequest(@Body body: FriendRequest): ApiResponse<Void>

    @PUT("friend/accept/{id}")
    suspend fun acceptFriend(@Path("id") id: Long, @Query("userId") userId: Long): ApiResponse<Void>

    @PUT("friend/reject/{id}")
    suspend fun rejectFriend(@Path("id") id: Long, @Query("userId") userId: Long): ApiResponse<Void>

    @DELETE("friend/delete")
    suspend fun deleteFriend(
        @Query("userId") userId: Long,
        @Query("friendId") friendId: Long
    ): ApiResponse<Void>

    @GET("friend/list")
    suspend fun friendList(
        @Query("userId") userId: Long,
        @Query("userType") userType: Int
    ): ApiResponse<List<FriendResponse>>

    @GET("friend/pending")
    suspend fun pendingRequests(
        @Query("userId") userId: Long,
        @Query("userType") userType: Int
    ): ApiResponse<List<FriendResponse>>

    @GET("friend/search")
    suspend fun searchUsers(
        @Query("keyword") keyword: String,
        @Query("userId") userId: Long
    ): ApiResponse<List<SearchUser>>

    // ===== 私信 =====
    @POST("message/send")
    suspend fun sendMessage(@Body body: SendMessageRequest): ApiResponse<Void>

    @GET("message/conversation/{otherUserId}/{otherUserType}")
    suspend fun getConversation(
        @Path("otherUserId") otherUserId: Long,
        @Path("otherUserType") otherUserType: Int,
        @Query("userId") userId: Long,
        @Query("userType") userType: Int
    ): ApiResponse<List<Message>>

    @GET("message/conversations")
    suspend fun getConversations(
        @Query("userId") userId: Long,
        @Query("userType") userType: Int
    ): ApiResponse<List<Conversation>>

    // ===== 收藏 =====
    @POST("pet-favorite/toggle")
    suspend fun toggleFavorite(
        @Query("userId") userId: Long,
        @Query("petId") petId: Long
    ): ApiResponse<Void>

    @GET("pet-favorite/ids")
    suspend fun favoriteIds(@Query("userId") userId: Long): ApiResponse<List<Long>>

    @GET("pet-favorite/list")
    suspend fun favoriteList(@Query("userId") userId: Long): ApiResponse<List<FavoritePet>>

    // ===== 社区 =====
    @GET("community/post/list")
    suspend fun postList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PostListPage>

    @GET("community/post/{id}")
    suspend fun postDetail(@Path("id") id: Long): ApiResponse<PostDetail>

    @PUT("community/post/{id}/like")
    suspend fun likePost(@Path("id") id: Long): ApiResponse<Boolean>

    @POST("community/comment")
    suspend fun createComment(@Body body: CreateCommentRequest): ApiResponse<Long>

    // ===== 举报 =====
    @POST("report")
    suspend fun createReport(@Body body: ReportRequest): ApiResponse<Void>

    // ===== 通知 =====
    @GET("notifications")
    suspend fun notifications(
        @Query("userId") userId: Long,
        @Query("userType") userType: Int
    ): ApiResponse<List<Notification>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Long): ApiResponse<Void>

    @GET("notifications/unread-count")
    suspend fun unreadNotificationCount(
        @Query("userId") userId: Long,
        @Query("userType") userType: Int
    ): ApiResponse<Int>

    // ===== 救助站 =====
    @GET("shelter/{shelterId}/profile")
    suspend fun shelterProfile(@Path("shelterId") shelterId: Long): ApiResponse<ShelterProfileData>

    // 救助站 - 宠物管理
    @POST("pet")
    suspend fun createPet(@Body body: CreatePetRequest): ApiResponse<Long>

    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ApiResponse<String>

    @DELETE("pet/{id}")
    suspend fun deletePet(@Path("id") id: Long): ApiResponse<Void>

    // 救助站 - 领养审核
    @GET("adoption/list")
    suspend fun shelterAdoptions(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("status") status: Int? = null
    ): ApiResponse<AdoptionListPage>

    @PUT("adoption/{id}/audit")
    suspend fun auditAdoption(
        @Path("id") id: Long,
        @Body body: AuditAdoptionRequest
    ): ApiResponse<Void>

    @PUT("adoption/{id}/complete")
    suspend fun completeAdoption(@Path("id") id: Long): ApiResponse<Void>

    // ===== 救助站 - 帖子审核 =====
    @GET("community/post/review/list")
    suspend fun postReviewList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ApiResponse<PostReviewListPage>

    @PUT("community/post/{id}/review")
    suspend fun reviewPost(
        @Path("id") id: Long,
        @Body body: Map<String, Boolean>
    ): ApiResponse<Void>

    // ===== 救助站 - 举报管理 =====
    @GET("report/pending")
    suspend fun pendingReports(): ApiResponse<List<ReportItem>>

    @PUT("report/{id}/review")
    suspend fun reviewReport(
        @Path("id") id: Long,
        @Body body: Map<String, Int>
    ): ApiResponse<Void>

    // ===== 救助站 - 下架帖子 =====
    @PUT("community/post/{postId}/take-down")
    suspend fun takeDownPost(@Path("postId") postId: Long): ApiResponse<Void>

    // ===== 救助站 - 百科审核 =====
    @GET("wiki/entry/review/list")
    suspend fun wikiReviewList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ApiResponse<WikiReviewListPage>

    @PUT("wiki/entry/{id}/review")
    suspend fun reviewWikiEntry(
        @Path("id") id: Long,
        @Body body: Map<String, Boolean>
    ): ApiResponse<Void>

    // ===== 救助站 - 寄养管理 =====
    @GET("foster/service/search")
    suspend fun fosterServiceList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): ApiResponse<FosterServiceListPage>

    @POST("foster/service")
    suspend fun createFosterService(@Body body: AddFosterServiceRequest): ApiResponse<Void>

    @PUT("foster/service/{id}")
    suspend fun updateFosterService(
        @Path("id") id: Long,
        @Body body: UpdateFosterServiceRequest
    ): ApiResponse<Void>

    @DELETE("foster/service/{id}")
    suspend fun deleteFosterService(@Path("id") id: Long): ApiResponse<Void>

    @GET("foster/order/list")
    suspend fun fosterOrderList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): ApiResponse<FosterOrderListPage>

    @PUT("foster/order/{id}/confirm")
    suspend fun confirmFosterOrder(@Path("id") id: Long): ApiResponse<Void>

    @PUT("foster/order/{id}/complete")
    suspend fun completeFosterOrder(@Path("id") id: Long): ApiResponse<Void>

    // ===== 寄养（普通用户） =====
    @GET("foster/service/search")
    suspend fun searchFosterServices(
        @Query("keyword") keyword: String? = null,
        @Query("petType") petType: String? = null,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): ApiResponse<FosterServiceListPage>

    @POST("foster/order")
    suspend fun createFosterOrder(@Body body: CreateFosterOrderRequest): ApiResponse<Long>

    @GET("foster/order/my")
    suspend fun myFosterOrders(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): ApiResponse<List<FosterOrderItem>>

    @PUT("foster/order/{orderId}/cancel")
    suspend fun cancelFosterOrder(@Path("orderId") orderId: Long): ApiResponse<Void>

    @PUT("foster/order/{orderId}/review")
    suspend fun reviewFosterOrder(
        @Path("orderId") orderId: Long,
        @Body body: ReviewRequest
    ): ApiResponse<Void>

    // ===== 匹配画像 =====
    @GET("matching/profile")
    suspend fun getMatchingProfile(): ApiResponse<MatchingProfileData>

    @PUT("matching/profile")
    suspend fun saveMatchingProfile(@Body body: MatchingProfileRequest): ApiResponse<Void>

    @GET("matching/recommend")
    suspend fun getMatchingRecommend(): ApiResponse<List<MatchedPet>>

    // ===== 领养回访 =====
    @POST("followup")
    suspend fun createFollowup(@Body body: FollowupRequest): ApiResponse<FollowupItem>

    @GET("followup/adoption/{adoptionId}")
    suspend fun getFollowupsByAdoption(@Path("adoptionId") adoptionId: Long): ApiResponse<List<FollowupItem>>

    @GET("followup/shelter/{shelterId}")
    suspend fun getFollowupsByShelter(@Path("shelterId") shelterId: Long): ApiResponse<List<FollowupItem>>

    // ===== 信用分 =====
    @GET("credit/logs")
    suspend fun getCreditLogs(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PageData<CreditLog>>

    // ===== 成长激励 =====
    @POST("growth/checkin")
    suspend fun dailyCheckin(): ApiResponse<Map<String, Any>>

    @GET("growth/my-points")
    suspend fun getMyPoints(): ApiResponse<Map<String, Any>>

    @GET("growth/my-badges")
    suspend fun getMyBadges(): ApiResponse<List<BadgeInfo>>

    @GET("growth/points-log")
    suspend fun getPointsLog(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PageData<PointsLogItem>>

    // ===== 百科（普通用户） =====
    @GET("wiki/categories")
    suspend fun wikiCategories(): ApiResponse<List<WikiCategory>>

    @GET("wiki/entry/list")
    suspend fun wikiEntryList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("categoryId") categoryId: Long? = null,
        @Query("keyword") keyword: String? = null,
        @Query("sortBy") sortBy: String = "newest"
    ): ApiResponse<WikiEntryListPage>

    @GET("wiki/entry/{id}")
    suspend fun wikiEntryDetail(@Path("id") id: Long): ApiResponse<WikiEntryDetail>

    @POST("wiki/entry")
    suspend fun createWikiEntry(@Body body: WikiEntryCreateRequest): ApiResponse<Long>

    @PUT("wiki/entry/{id}")
    suspend fun editWikiEntry(@Path("id") id: Long, @Body body: WikiEntryEditRequest): ApiResponse<Void>

    // ===== 宠物收藏 =====
    @POST("pet/favorite")
    suspend fun petFavorite(
        @Query("userId") userId: Long,
        @Query("petId") petId: Long
    ): ApiResponse<Void>

    @GET("pet/favorite/list")
    suspend fun petFavoriteList(@Query("userId") userId: Long): ApiResponse<List<Pet>>

    @GET("pet/favorite/ids")
    suspend fun petFavoriteIds(@Query("userId") userId: Long): ApiResponse<List<Long>>

}

// 分页包装
data class PetListPage(
    val records: List<Pet> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

data class PostListPage(
    val records: List<Post> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

data class AdoptionListPage(
    val records: List<AdoptionReview> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

data class ShelterProfileData(
    val shelterInfo: ShelterInfo? = null,
    val stats: ShelterStats? = null,
    val recentStories: List<ShelterStory>? = null
)

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val token = TokenManager.token
            val builder = original.newBuilder()
            if (token.isNotEmpty()) {
                builder.header("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var _instance: PawMatchApi? = null

    val instance: PawMatchApi
        get() {
            if (_instance == null) rebuild(ServerConfigManager.serverUrl)
            return _instance!!
        }

    fun rebuild(baseUrl: String) {
        _instance = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PawMatchApi::class.java)
    }
}
