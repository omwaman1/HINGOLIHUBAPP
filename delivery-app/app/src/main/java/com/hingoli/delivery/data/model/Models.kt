package com.hingoli.delivery.data.model

import com.google.gson.annotations.SerializedName

// API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

// Auth models
data class SendOtpRequest(val phone: String, val name: String? = null, val vehicle_type: String? = null)
data class VerifyOtpRequest(val phone: String, val otp: String)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("is_new_user") val isNewUser: Boolean,
    val user: DeliveryUser?
)

data class DeliveryUser(
    @SerializedName("delivery_user_id") val id: Long,
    val phone: String,
    val name: String,
    val email: String? = null,
    val address: String? = null,
    @SerializedName("upi_id") val upiId: String? = null,
    @SerializedName("vehicle_type") val vehicleType: String? = null,
    @SerializedName("vehicle_number") val vehicleNumber: String? = null,
    val status: String? = null,
    @SerializedName("total_deliveries") val totalDeliveries: Int = 0,
    @SerializedName("total_earnings") val totalEarnings: Double = 0.0,
    @SerializedName("member_since") val memberSince: String? = null
)

// Order models
data class CustomerInfo(
    @SerializedName("user_id") val userId: Long? = null,
    val name: String?,
    val phone: String?
)

data class AddressInfo(
    val line1: String?,
    val line2: String?,
    val city: String?,
    @SerializedName("postal_code") val postalCode: String?
)

data class PickupAddressInfo(
    val label: String?,
    val line1: String?,
    val line2: String?,
    val city: String?,
    @SerializedName("postal_code") val postalCode: String?,
    val phone: String?
)

data class OrderItem(
    @SerializedName("product_name") val productName: String,
    val quantity: Int,
    val price: Double
)

data class AvailableOrder(
    @SerializedName("order_id") val orderId: Long,
    @SerializedName("order_number") val orderNumber: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("delivery_earnings") val deliveryEarnings: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("order_status") val orderStatus: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("estimated_delivery_date") val estimatedDeliveryDate: String?,
    @SerializedName("delivery_time_slot") val deliveryTimeSlot: String?,
    val customer: CustomerInfo,
    @SerializedName("pickup_address") val pickupAddress: PickupAddressInfo?,
    @SerializedName("delivery_address") val deliveryAddress: AddressInfo?,
    // Keep old field for backward compatibility
    val address: AddressInfo? = null,
    val items: List<OrderItem>?
)

data class MyDelivery(
    @SerializedName("order_id") val orderId: Long,
    @SerializedName("order_number") val orderNumber: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("delivery_earnings") val deliveryEarnings: Double,
    @SerializedName("order_status") val orderStatus: String,
    @SerializedName("payment_status") val paymentStatus: String?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("accepted_at") val acceptedAt: String?,
    @SerializedName("picked_at") val pickedAt: String?,
    @SerializedName("delivered_at") val deliveredAt: String?,
    val customer: CustomerInfo,
    @SerializedName("pickup_address") val pickupAddress: PickupAddressInfo?,
    @SerializedName("delivery_address") val deliveryAddress: AddressInfo?,
    // Keep old field for backward compatibility
    val address: AddressInfo? = null,
    @SerializedName("items_count") val itemsCount: Int
)

// Earnings models
data class EarningsPeriod(
    val deliveries: Int,
    val earnings: Double
)

data class EarningsSummary(
    val total: EarningsPeriod,
    val today: EarningsPeriod,
    @SerializedName("this_week") val thisWeek: EarningsPeriod,
    @SerializedName("this_month") val thisMonth: EarningsPeriod
)

// Request models
data class AcceptOrderRequest(@SerializedName("order_id") val orderId: Long)
data class UpdateStatusRequest(
    @SerializedName("order_id") val orderId: Long,
    val status: String
)
