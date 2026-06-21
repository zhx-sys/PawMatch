package com.pawmatch.app.data.model

import com.google.gson.annotations.SerializedName

// ===== 通用响应 =====
data class ApiResponse<T>(
    val code: Int = 200,
    val message: String = "",
    val data: T? = null
)

// ===== 认证 =====
data class LoginRequest(
    val account: String,
    val password: String,
    val userType: Int = 0
)

data class LoginResponse(
    val token: String = "",
    val tokenType: String = "",
    val expiresIn: Long = 0,
    val userId: Long = 0,
    val userType: Int = 0
)

data class RegisterRequest(
    val account: String = "",
    val password: String,
    val confirmPassword: String,
    val nickname: String
)

data class RegisterResponse(
    val id: Long = 0,
    val account: String = ""
)

// ===== 宠物 =====
data class Pet(
    val id: Long = 0,
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
    val status: Int = 0,
    val isFavorite: Boolean = false,
    val livingSpace: String? = null,
    val hasChildren: Int? = null,
    val hasPets: Int? = null,
    val experience: String? = null,
    val schedule: String? = null,
    val budget: Int? = null
)

data class PetDetail(
    val id: Long = 0,
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
    val status: Int = 0,
    val isFavorite: Boolean = false,
    val livingSpace: String? = null,
    val hasChildren: Int? = null,
    val hasPets: Int? = null,
    val experience: String? = null,
    val schedule: String? = null,
    val budget: Int? = null,
    val shelterInfo: ShelterInfo? = null
)

data class ShelterInfo(
    val nickname: String = "",
    val avatar: String? = null,
    val address: String? = null,
    val phone: String? = null
)

data class UserInfo(
    val id: Long = 0,
    val username: String = "",
    val nickname: String = "",
    val email: String? = null,
    val avatar: String? = null
)

// ===== 信用分 =====
data class CreditLog(
    val id: Long = 0,
    val userId: Long = 0,
    val userType: Int = 0,
    val scoreChange: Int = 0,
    val scoreAfter: Int = 0,
    val reasonType: String? = null,
    val reasonDetail: String? = null,
    val relatedId: Long? = null,
    val createTime: String? = null
)
