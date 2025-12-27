package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("listing_count") val listingCount: Int,
    @SerializedName("avg_rating") val avgRating: Float,
    @SerializedName("created_at") val createdAt: String
)

data class UpdateProfileRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("password") val password: String? = null
)

data class RegisterFcmTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("device_info") val deviceInfo: String? = null
)
