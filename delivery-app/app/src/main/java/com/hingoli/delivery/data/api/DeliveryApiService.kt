package com.hingoli.delivery.data.api

import com.hingoli.delivery.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface DeliveryApiService {
    
    // Auth
    @POST("delivery/register")
    suspend fun register(@Body request: SendOtpRequest): Response<ApiResponse<Any>>
    
    @POST("delivery/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<ApiResponse<Any>>
    
    @POST("delivery/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<AuthResponse>>
    
    // Orders
    @GET("delivery/available-orders")
    suspend fun getAvailableOrders(): Response<ApiResponse<List<AvailableOrder>>>
    
    @POST("delivery/accept-order")
    suspend fun acceptOrder(@Body request: AcceptOrderRequest): Response<ApiResponse<Any>>
    
    @POST("delivery/cancel-order")
    suspend fun cancelOrder(@Body request: AcceptOrderRequest): Response<ApiResponse<Any>>
    
    @GET("delivery/my-orders")
    suspend fun getMyOrders(@Query("status") status: String = "active"): Response<ApiResponse<List<MyDelivery>>>
    
    @POST("delivery/update-status")
    suspend fun updateStatus(@Body request: UpdateStatusRequest): Response<ApiResponse<Any>>
    
    // Earnings & Profile
    @GET("delivery/earnings")
    suspend fun getEarnings(): Response<ApiResponse<EarningsSummary>>
    
    @GET("delivery/profile")
    suspend fun getProfile(): Response<ApiResponse<DeliveryUser>>
    
    @PUT("delivery/profile")
    suspend fun updateProfile(@Body updates: Map<String, String>): Response<ApiResponse<Any>>
}
