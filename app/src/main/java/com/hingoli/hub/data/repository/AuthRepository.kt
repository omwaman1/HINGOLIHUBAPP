package com.hingoli.hub.data.repository

import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.api.TokenManager
import com.hingoli.hub.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class OtpResult {
    data class LoginSuccess(val user: User, val isNewUser: Boolean) : OtpResult()
    data class ResetTokenReceived(val resetToken: String) : OtpResult()
    data class Error(val message: String) : OtpResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val notificationRepository: NotificationRepository
) {
    
    suspend fun login(phone: String, password: String): AuthResult {
        return try {
            val response = apiService.login(LoginRequest(phone, password))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                    tokenManager.saveUser(data.user.userId, data.user.username, data.user.phone)
                    
                    // Register FCM token for push notifications
                    notificationRepository.registerFcmToken(data.user.userId)
                    
                    AuthResult.Success(data.user)
                } else {
                    AuthResult.Error("Invalid response from server")
                }
            } else {
                AuthResult.Error(response.body()?.message ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun checkPhone(phone: String): Result<CheckPhoneData> {
        return try {
            val response = apiService.checkPhone(CheckPhoneRequest(phone))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { Result.success(it) }
                    ?: Result.failure(Exception("Invalid response"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendOtp(phone: String, purpose: String = "signup"): Result<SendOtpData> {
        return try {
            val response = apiService.sendOtp(SendOtpRequest(phone, purpose))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { Result.success(it) }
                    ?: Result.failure(Exception("Invalid response"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to send OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyOtp(phone: String, otp: String): OtpResult {
        return try {
            val response = apiService.verifyOtp(VerifyOtpRequest(phone, otp))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                
                if (data?.purpose == "reset_password" && data.resetToken != null) {
                    // Password reset flow
                    OtpResult.ResetTokenReceived(data.resetToken)
                } else if (data?.accessToken != null && data.refreshToken != null) {
                    // Login/signup flow
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                    
                    val user = data.user ?: User(
                        userId = 0,
                        username = "User",
                        phone = phone
                    )
                    tokenManager.saveUser(user.userId, user.username, user.phone)
                    
                    // Register FCM token
                    notificationRepository.registerFcmToken(user.userId)
                    
                    OtpResult.LoginSuccess(user, data.isNewUser)
                } else {
                    OtpResult.Error("Invalid response from server")
                }
            } else {
                OtpResult.Error(response.body()?.message ?: "OTP verification failed")
            }
        } catch (e: Exception) {
            OtpResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun resetPassword(phone: String, resetToken: String, newPassword: String): Result<Boolean> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(phone, resetToken, newPassword))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Password reset failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signupWithOtp(
        phone: String,
        otp: String,
        username: String,
        email: String?,
        password: String,
        gender: String?,
        dateOfBirth: String?
    ): OtpResult {
        return try {
            val request = SignupWithOtpRequest(
                phone = phone,
                otp = otp,
                username = username,
                email = email,
                password = password,
                gender = gender,
                dateOfBirth = dateOfBirth
            )
            val response = apiService.signupWithOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                
                if (data?.accessToken != null && data.refreshToken != null) {
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                    
                    val user = data.user ?: User(
                        userId = 0,
                        username = username,
                        phone = phone,
                        email = email
                    )
                    tokenManager.saveUser(user.userId, user.username, user.phone)
                    
                    // Register FCM token
                    notificationRepository.registerFcmToken(user.userId)
                    
                    OtpResult.LoginSuccess(user, true)
                } else {
                    OtpResult.Error("Invalid response from server")
                }
            } else {
                OtpResult.Error(response.body()?.message ?: "Signup failed")
            }
        } catch (e: Exception) {
            OtpResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    suspend fun logout() {
        // Unregister FCM token before logout
        tokenManager.getUserId()?.let { userId ->
            notificationRepository.unregisterFcmToken(userId)
        }
        tokenManager.clearTokens()
    }
    
    suspend fun getCurrentUserId(): Long? {
        return tokenManager.getUserId()
    }
    
    suspend fun getCurrentUserName(): String? {
        return tokenManager.getUserName()
    }
    
    suspend fun getCurrentUserPhone(): String? {
        return tokenManager.getUserPhone()
    }
}
