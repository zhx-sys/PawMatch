package com.pawmatch.app.data.model

import com.google.gson.annotations.SerializedName

// 救助站数据统计
data class ShelterStats(
    val totalAdopted: Int = 0,
    val currentPets: Int = 0,
    val successRate: Double = 0.0,
    val avgResponseHours: Double = 0.0
)

// 领养申请（救助站视角，比普通用户看到的字段更多）
data class AdoptionReview(
    val id: Long = 0,
    val petId: Long = 0,
    val petName: String? = null,
    val petType: String? = null,
    val userId: Long = 0,
    val userName: String? = null,
    val reason: String? = null,
    val experience: String? = null,
    val housingCondition: String? = null,
    val status: Int = 0,
    val applyTime: String? = null,
    val rejectReason: String? = null
)

// 审核请求
data class AuditAdoptionRequest(
    val status: Int,
    val rejectReason: String? = null
)

// 创建宠物请求（FormData 模式，这里部分字段用于展示）
data class CreatePetRequest(
    val name: String,
    val type: String = "狗",
    val breed: String = "",
    val gender: String = "公",
    val age: Int = 1,
    val color: String = "",
    val weight: Double = 5.0,
    val healthStatus: String = "健康",
    val vaccinated: Boolean = true,
    val sterilized: Boolean = false,
    val description: String = "",
    val sizeLevel: String = "",
    val activityLevel: String = "",
    val beginnerFriendly: Boolean = false,
    val goodWithKids: Boolean = false,
    val goodWithPets: Boolean = false,
    val images: List<String> = emptyList()
)

// 审核领养响应
data class AuditResponse(
    val message: String = ""
)

// ===== 帖子审核 =====
data class PostReviewItem(
    val id: Long = 0,
    val userId: Long = 0,
    val userType: Int = 0,
    val userName: String? = null,
    val title: String? = null,
    val content: String? = null,
    val category: String? = null,
    val createTime: String? = null
)

data class PostReviewListPage(
    val records: List<PostReviewItem> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

// ===== 百科审核 =====
data class WikiReviewItem(
    val id: Long = 0,
    val title: String? = null,
    val summary: String? = null,
    val content: String? = null,
    val categoryName: String? = null,
    val authorName: String? = null,
    val createTime: String? = null
)

data class WikiReviewListPage(
    val records: List<WikiReviewItem> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

// ===== 寄养管理 =====
data class FosterServiceItem(
    val id: Long = 0,
    val title: String? = null,
    val description: String? = null,
    val petType: String? = null,
    val pricePerDay: Double = 0.0,
    val maxCapacity: Int = 1,
    val images: String? = null,
    val shelterId: Long = 0,
    val shelterName: String? = null,
    val status: Int = 1,
    val createTime: String? = null
)

data class FosterServiceListPage(
    val records: List<FosterServiceItem> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

data class FosterOrderItem(
    val id: Long = 0,
    val serviceId: Long = 0,
    val serviceName: String? = null,
    val shelterId: Long = 0,
    val shelterName: String? = null,
    val petName: String? = null,
    val petType: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val totalDays: Int = 0,
    val totalPrice: Double = 0.0,
    val specialRequests: String? = null,
    val status: Int = 0,
    val rating: Int? = null,
    val comment: String? = null,
    val createTime: String? = null
)

data class FosterOrderListPage(
    val records: List<FosterOrderItem> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)

// 寄养服务请求
data class AddFosterServiceRequest(
    val title: String,
    val description: String,
    val petType: String,
    val pricePerDay: Double,
    val maxCapacity: Int
)

data class UpdateFosterServiceRequest(
    val title: String? = null,
    val description: String? = null,
    val petType: String? = null,
    val pricePerDay: Double? = null,
    val maxCapacity: Int? = null
)

// ===== 百科（普通用户侧） =====
data class WikiCategory(
    val id: Long = 0,
    val name: String = "",
    val children: List<WikiCategory> = emptyList()
)

data class WikiEntryItem(
    val id: Long = 0,
    val title: String = "",
    val summary: String? = null,
    val categoryName: String? = null,
    val authorName: String? = null,
    val viewCount: Int = 0,
    val helpfulCount: Int = 0,
    val createTime: String? = null
)

data class WikiEntryDetail(
    val id: Long = 0,
    val title: String = "",
    val summary: String? = null,
    val content: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val authorId: Long = 0,
    val authorName: String? = null,
    val viewCount: Int = 0,
    val helpfulCount: Int = 0,
    val status: Int = 0,
    val createTime: String? = null,
    val updateTime: String? = null
)

data class WikiEntryListPage(
    val records: List<WikiEntryItem> = emptyList(),
    val total: Long = 0,
    val pages: Long = 0
)