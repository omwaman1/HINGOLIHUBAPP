package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")
    val userId: Long,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    
    @SerializedName("listing_count")
    val listingCount: Int = 0,
    
    @SerializedName("avg_rating")
    val avgRating: Double = 0.0,
    
    @SerializedName("review_count")
    val reviewCount: Int = 0,
    
    @SerializedName("response_rate")
    val responseRate: Double = 0.0,
    
    @SerializedName("last_active_at")
    val lastActiveAt: String? = null
)

data class LoginRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: LoginData? = null
)

data class LoginData(
    @SerializedName("user")
    val user: User,
    
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("refresh_token")
    val refreshToken: String,
    
    @SerializedName("expires_in")
    val expiresIn: Long
)

// ==================== OTP Authentication Models ====================

data class CheckPhoneRequest(
    @SerializedName("phone")
    val phone: String
)

data class CheckPhoneData(
    @SerializedName("exists")
    val exists: Boolean,
    
    @SerializedName("username")
    val username: String? = null
)

data class SendOtpRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("purpose")
    val purpose: String = "signup" // "signup" or "reset_password"
)

data class SendOtpData(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("expires_in")
    val expiresIn: Int
)

data class VerifyOtpRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("otp")
    val otp: String
)

data class OtpLoginResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: OtpLoginData? = null
)

data class OtpLoginData(
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("access_token")
    val accessToken: String? = null,
    
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    
    @SerializedName("expires_in")
    val expiresIn: Long? = null,
    
    @SerializedName("is_new_user")
    val isNewUser: Boolean = false,
    
    // For password reset flow
    @SerializedName("verified")
    val verified: Boolean = false,
    
    @SerializedName("purpose")
    val purpose: String? = null,
    
    @SerializedName("reset_token")
    val resetToken: String? = null
)

data class ResetPasswordRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("reset_token")
    val resetToken: String,
    
    @SerializedName("new_password")
    val newPassword: String
)

data class ResetPasswordData(
    @SerializedName("success")
    val success: Boolean
)

// ==================== Signup with OTP Models ====================

data class SignupWithOtpRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("otp")
    val otp: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null
)
