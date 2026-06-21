package com.pawmatch.app.data.model

import com.google.gson.annotations.SerializedName

// 好友
data class FriendResponse(
    val id: Long = 0,
    val friendId: Long = 0,
    val friendUserType: Int = 0,
    val nickname: String? = null,
    val status: Int = 0,
    val createTime: String? = null
)

// 私信
data class Message(
    val id: Long = 0,
    val fromUserId: Long = 0,
    val fromUserType: Int = 0,
    val toUserId: Long = 0,
    val toUserType: Int = 0,
    val content: String = "",
    val createTime: String? = null
)

data class Conversation(
    val otherUserId: Long = 0,
    val otherUserType: Int = 0,
    val nickname: String? = null,
    val lastContent: String? = null,
    val lastTime: String? = null,
    val unread: Boolean = false
)

data class SendMessageRequest(
    val fromUserId: Long,
    val fromUserType: Int,
    val toUserId: Long,
    val toUserType: Int,
    val content: String
)

// 领养
data class AdoptionApplication(
    val id: Long = 0,
    val petId: Long = 0,
    val petName: String? = null,
    val userId: Long = 0,
    val userName: String? = null,
    val status: Int = 0,
    val message: String? = null,
    val applyTime: String? = null
)

// 社区帖子
data class Post(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val userId: Long = 0,
    val userType: Int = 0,
    val nickname: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val viewCount: Int = 0,
    val liked: Boolean = false,
    val createTime: String? = null,
    val imageUrls: String? = null
)

data class Comment(
    val id: Long = 0,
    val postId: Long = 0,
    val userId: Long = 0,
    val userType: Int = 0,
    @SerializedName("userName") val nickname: String? = null,
    val content: String = "",
    val parentId: Long? = null,
    val replies: List<Comment>? = null,
    val createTime: String? = null
)

data class CreateCommentRequest(
    val postId: Long,
    val content: String,
    val parentId: Long? = null
)

// 收藏
data class FavoritePet(
    val id: Long = 0,
    val userId: Long = 0,
    val petId: Long = 0,
    val createTime: String? = null,
    // 宠物字段（后端 JOIN 返回）
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val age: Int = 0,
    val gender: String = "",
    val sizeLevel: String? = null,
    val activityLevel: String? = null,
    val healthStatus: String? = null,
    val description: String? = null,
    @SerializedName("images")
    val imageUrls: String? = null,
    val shelterId: Long = 0,
    val shelterName: String? = null,
    val status: Int = 0
)

// 社区帖子详情（扁平结构，对齐后端 PostDetailResponse）
data class PostDetail(
    val id: Long = 0,
    val userId: Long = 0,
    val userType: Int = 0,
    @SerializedName("userName") val nickname: String? = null,
    val title: String = "",
    val content: String = "",
    val category: String? = null,
    @SerializedName("images") val imageUrls: List<String>? = null,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    @SerializedName("hasLiked") val liked: Boolean = false,
    val commentCount: Int = 0,
    val comments: List<Comment> = emptyList(),
    val createTime: String? = null
)

// 通知
data class Notification(
    val id: Long = 0,
    val userId: Long = 0,
    val type: String = "",
    val title: String = "",
    val content: String = "",
    val read: Boolean = false,
    val isRead: Boolean = false,
    val createTime: String? = null
)

// 领养申请请求
data class ApplyAdoptionRequest(
    val petId: Long,
    val reason: String,
    val experience: String,
    val housingCondition: String,
    val confirmFlood: Boolean? = null
)

// 好友请求
data class FriendRequest(
    val userId: Long,
    val userType: Int,
    val friendId: Long,
    val friendUserType: Int
)

// 上传
data class UploadResult(
    val url: String = ""
)

// 搜索
data class SearchUser(
    val id: Long = 0,
    val nickname: String? = null,
    val userType: Int = 0,
    val added: Boolean = false
)

// ===== 寄养（普通用户） =====
data class CreateFosterOrderRequest(
    val serviceId: Long,
    val petName: String,
    val petType: String,
    val startDate: String,
    val endDate: String,
    val specialRequests: String? = null
)

data class ReviewRequest(
    val rating: Int,
    val comment: String? = null
)

// ===== 匹配画像 =====
data class MatchingProfileData(
    val id: Long = 0,
    val nickname: String? = null,
    val livingSpace: String? = null,
    val hasChildren: Boolean = false,
    val hasOtherPets: Boolean = false,
    val petExperience: String? = null,
    val dailyRoutine: String? = null,
    val budgetRange: String? = null,
    val petPreference: String? = null,
    val matchingProfileComplete: Boolean = false
)

data class MatchingProfileRequest(
    val livingSpace: String? = null,
    val hasChildren: Boolean? = null,
    val hasOtherPets: Boolean? = null,
    val petExperience: String? = null,
    val dailyRoutine: String? = null,
    val budgetRange: String? = null,
    val petPreference: String? = null
)

// ===== 智能推荐 =====
data class MatchedPet(
    val id: Long = 0,
    val name: String? = null,
    val type: String? = null,
    val breed: String? = null,
    val gender: String? = null,
    val images: String? = null,
    val matchScore: Double = 0.0,
    val matchDetails: Map<String, Int>? = null
)

// ===== 领养回访 =====
data class FollowupRequest(
    val adoptionId: Long,
    val userId: Long,
    val shelterId: Long = 0,
    val content: String,
    val images: String? = null
)

data class FollowupItem(
    val id: Long = 0,
    val adoptionId: Long = 0,
    val userId: Long = 0,
    val content: String? = null,
    val images: String? = null,
    val createTime: String? = null,
    val petName: String? = null,
    val userName: String? = null
)

// ===== 成长激励 =====
data class BadgeInfo(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    val icon: String? = null,
    val earned: Boolean = false
)

data class PointsLogItem(
    val id: Long = 0,
    val userId: Long = 0,
    val points: Int = 0,
    val action: String? = null,
    val description: String? = null,
    val createTime: String? = null
)

data class PageData<T>(
    val records: List<T> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

// ===== 救助站主页扩展 =====
data class ShelterStory(
    val id: Long = 0,
    val title: String = "",
    val viewCount: Int = 0,
    val createTime: String? = null
)

// ===== 百科创建/编辑 =====
data class WikiEntryCreateRequest(
    val title: String,
    val content: String,
    val summary: String? = null,
    val categoryId: Long? = null
)

data class WikiEntryEditRequest(
    val title: String,
    val content: String,
    val summary: String? = null,
    val categoryId: Long? = null,
    val editSummary: String? = null
)

// ===== 举报 =====
data class ReportRequest(
    val reporterId: Long,
    val targetType: String,
    val targetId: Long,
    val reason: String
)

data class ReportItem(
    val id: Long = 0,
    val reporterId: Long = 0,
    val reporterName: String? = null,
    val targetType: String? = null,
    val targetId: Long = 0,
    val targetTitle: String? = null,
    val targetContent: String? = null,
    val reason: String? = null,
    val status: Int = 0,
    val createTime: String? = null
)

// 修改密码
data class UpdateUserRequest(
    val oldPassword: String,
    val newPassword: String
)
