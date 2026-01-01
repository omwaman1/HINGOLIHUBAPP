package com.hingoli.delivery.data.repository

import com.hingoli.delivery.data.api.DeliveryApiService
import com.hingoli.delivery.data.model.*
import com.hingoli.delivery.di.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class DeliveryRepository @Inject constructor(
    private val api: DeliveryApiService,
    private val tokenManager: TokenManager
) {
    
    // Auth
    suspend fun register(phone: String, name: String, vehicleType: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(SendOtpRequest(phone, name, vehicleType))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.message ?: "OTP sent")
            } else {
                Result.Error(response.body()?.message ?: "Registration failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun sendOtp(phone: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendOtp(SendOtpRequest(phone))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data as? Map<*, *>
                val isNewUser = data?.get("is_new_user") as? Boolean ?: false
                val result = mutableMapOf<String, Any>(
                    "message" to (response.body()?.message ?: "OTP sent"),
                    "is_new_user" to isNewUser
                )
                Result.Success(result)
            } else {
                Result.Error(response.body()?.message ?: "Failed to send OTP")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun verifyOtp(phone: String, otp: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyOtp(VerifyOtpRequest(phone, otp))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { auth ->
                    tokenManager.saveTokens(auth.accessToken, auth.refreshToken)
                    auth.user?.let { user ->
                        tokenManager.saveUser(user.id, user.name, user.phone)
                    }
                    Result.Success(auth)
                } ?: Result.Error("Invalid response")
            } else {
                Result.Error(response.body()?.message ?: "Verification failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    
    suspend fun logout() = tokenManager.clearTokens()
    
    // Orders
    suspend fun getAvailableOrders(): Result<List<AvailableOrder>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAvailableOrders()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data ?: emptyList())
            } else {
                Result.Error(response.body()?.message ?: "Failed to fetch orders")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun acceptOrder(orderId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.acceptOrder(AcceptOrderRequest(orderId))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.message ?: "Order accepted")
            } else {
                Result.Error(response.body()?.message ?: "Failed to accept order")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun cancelOrder(orderId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.cancelOrder(AcceptOrderRequest(orderId))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.message ?: "Order cancelled")
            } else {
                Result.Error(response.body()?.message ?: "Failed to cancel order")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun getMyOrders(status: String = "active"): Result<List<MyDelivery>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyOrders(status)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data ?: emptyList())
            } else {
                Result.Error(response.body()?.message ?: "Failed to fetch deliveries")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun updateStatus(orderId: Long, status: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateStatus(UpdateStatusRequest(orderId, status))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.message ?: "Status updated")
            } else {
                Result.Error(response.body()?.message ?: "Failed to update status")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    // Earnings
    suspend fun getEarnings(): Result<EarningsSummary> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEarnings()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.Success(it)
                } ?: Result.Error("No earnings data")
            } else {
                Result.Error(response.body()?.message ?: "Failed to fetch earnings")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    // Profile
    suspend fun getProfile(): Result<DeliveryUser> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.Success(it)
                } ?: Result.Error("No profile data")
            } else {
                Result.Error(response.body()?.message ?: "Failed to fetch profile")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun updateProfile(updates: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProfile(updates)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.message ?: "Profile updated")
            } else {
                Result.Error(response.body()?.message ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
