package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

// ==================== CART ====================

data class CartResponse(
    @SerializedName("items")
    val items: List<CartItem>,
    
    @SerializedName("item_count")
    val itemCount: Int,
    
    @SerializedName("total")
    val total: Double
)

data class CartItem(
    @SerializedName("cart_item_id")
    val cartItemId: Long,
    
    @SerializedName("listing_id")
    val listingId: Long? = null,
    
    @SerializedName("product_id")
    val productId: Long? = null,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("main_image_url")
    val mainImageUrl: String?,
    
    @SerializedName("seller_id")
    val sellerId: Long,
    
    @SerializedName("seller_name")
    val sellerName: String,
    
    @SerializedName("subtotal")
    val subtotal: Double
)

data class AddToCartRequest(
    @SerializedName("listing_id")
    val listingId: Long? = null,
    
    @SerializedName("product_id")
    val productId: Long? = null,
    
    @SerializedName("quantity")
    val quantity: Int = 1
)

data class UpdateCartRequest(
    @SerializedName("quantity")
    val quantity: Int
)

// ==================== ADDRESS ====================

data class UserAddress(
    @SerializedName("address_id")
    val addressId: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("address_line1")
    val addressLine1: String,
    
    @SerializedName("address_line2")
    val addressLine2: String?,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("state")
    val state: String,
    
    @SerializedName("pincode")
    val pincode: String,
    
    @SerializedName("is_default")
    val isDefault: Boolean = false
) {
    val fullAddress: String
        get() = listOfNotNull(addressLine1, addressLine2, city, state, pincode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

data class AddAddressRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("address_line1")
    val addressLine1: String,
    
    @SerializedName("address_line2")
    val addressLine2: String? = null,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("state")
    val state: String = "Maharashtra",
    
    @SerializedName("pincode")
    val pincode: String
)

// ==================== ORDER ====================

data class Order(
    @SerializedName("order_id")
    val orderId: Long,
    
    @SerializedName("order_number")
    val orderNumber: String,
    
    @SerializedName("total_amount")
    val totalAmount: Double,
    
    @SerializedName("item_count")
    val itemCount: Int,
    
    @SerializedName("payment_method")
    val paymentMethod: String,
    
    @SerializedName("payment_status")
    val paymentStatus: String,
    
    @SerializedName("order_status")
    val orderStatus: String,
    
    @SerializedName("estimated_delivery_date")
    val estimatedDeliveryDate: String? = null,
    
    @SerializedName("delivery_time_slot")
    val deliveryTimeSlot: String? = null,
    
    @SerializedName("delivery_message")
    val deliveryMessage: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class OrderDetail(
    @SerializedName("order_id")
    val orderId: Long,
    
    @SerializedName("order_number")
    val orderNumber: String,
    
    @SerializedName("subtotal")
    val subtotal: Double,
    
    @SerializedName("shipping_fee")
    val shippingFee: Double,
    
    @SerializedName("total_amount")
    val totalAmount: Double,
    
    @SerializedName("payment_method")
    val paymentMethod: String,
    
    @SerializedName("payment_status")
    val paymentStatus: String,
    
    @SerializedName("order_status")
    val orderStatus: String,
    
    @SerializedName("estimated_delivery_date")
    val estimatedDeliveryDate: String? = null,
    
    @SerializedName("delivery_time_slot")
    val deliveryTimeSlot: String? = null,
    
    @SerializedName("delivery_message")
    val deliveryMessage: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("address")
    val address: OrderAddress,
    
    @SerializedName("items")
    val items: List<OrderItem>
)

// ==================== DELIVERY ====================

data class DeliveryEstimate(
    @SerializedName("serviceable")
    val serviceable: Boolean,
    
    @SerializedName("pincode")
    val pincode: String,
    
    @SerializedName("city_name")
    val cityName: String?,
    
    @SerializedName("delivery_days")
    val deliveryDays: Int,
    
    @SerializedName("delivery_time")
    val deliveryTime: String?,
    
    @SerializedName("shipping_fee")
    val shippingFee: Double,
    
    @SerializedName("estimated_date")
    val estimatedDate: String,
    
    @SerializedName("message")
    val message: String
)

data class OrderAddress(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("address_line1")
    val addressLine1: String,
    
    @SerializedName("address_line2")
    val addressLine2: String?,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("state")
    val state: String,
    
    @SerializedName("pincode")
    val pincode: String
)

data class OrderItem(
    @SerializedName("order_item_id")
    val orderItemId: Long,
    
    @SerializedName("listing_id")
    val listingId: Long,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("main_image_url")
    val mainImageUrl: String?,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("seller_name")
    val sellerName: String,
    
    @SerializedName("item_status")
    val itemStatus: String
)

data class CreateOrderRequest(
    @SerializedName("address_id")
    val addressId: Long,
    
    @SerializedName("payment_method")
    val paymentMethod: String = "razorpay" // or "cod"
)

data class CreateOrderResponse(
    @SerializedName("order_id")
    val orderId: Long,
    
    @SerializedName("order_number")
    val orderNumber: String,
    
    @SerializedName("total_amount")
    val totalAmount: Double,
    
    @SerializedName("payment_method")
    val paymentMethod: String,
    
    @SerializedName("razorpay_order_id")
    val razorpayOrderId: String?,
    
    @SerializedName("razorpay_error")
    val razorpayError: String?
)

data class VerifyPaymentRequest(
    @SerializedName("razorpay_payment_id")
    val razorpayPaymentId: String,
    
    @SerializedName("razorpay_signature")
    val razorpaySignature: String
)

// ==================== PRODUCT UPDATE ====================

data class UpdateProductRequest(
    @SerializedName("product_name")
    val productName: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("price")
    val price: Double? = null,
    
    @SerializedName("discounted_price")
    val discountedPrice: Double? = null,
    
    @SerializedName("condition")
    val condition: String? = null,
    
    @SerializedName("sell_online")
    val sellOnline: Boolean? = null,
    
    @SerializedName("stock_qty")
    val stockQty: Int? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null
)
