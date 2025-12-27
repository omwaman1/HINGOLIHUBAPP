package com.hingoli.hub.payment

import android.app.Activity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

/**
 * Helper object for Razorpay payment integration
 */
object RazorpayPaymentHelper {
    
    // Razorpay Key ID - Live
    private const val RAZORPAY_KEY_ID = "rzp_live_RrqH1rKPqejvOQ"
    
    /**
     * Initialize Razorpay SDK - call this in Application or MainActivity
     */
    fun init(activity: Activity) {
        Checkout.preload(activity.applicationContext)
    }
    
    /**
     * Start Razorpay payment flow
     *
     * @param activity The activity to attach the payment flow
     * @param razorpayOrderId Order ID from Razorpay backend
     * @param amount Amount in rupees (will be converted to paise)
     * @param userName User's name
     * @param userEmail User's email (optional)
     * @param userPhone User's phone
     * @param description Payment description
     */
    fun startPayment(
        activity: Activity,
        razorpayOrderId: String,
        amount: Double,
        userName: String,
        userEmail: String? = null,
        userPhone: String,
        description: String = "HINGOLI HUB Order Payment"
    ) {
        val checkout = Checkout()
        checkout.setKeyID(RAZORPAY_KEY_ID)
        
        try {
            val options = JSONObject().apply {
                put("name", "HINGOLI HUB")
                put("description", description)
                put("order_id", razorpayOrderId)
                put("currency", "INR")
                put("amount", (amount * 100).toLong()) // Convert to paise
                
                // Prefill user details
                put("prefill", JSONObject().apply {
                    put("name", userName)
                    put("contact", userPhone)
                    userEmail?.let { put("email", it) }
                })
                
                // Theme
                put("theme", JSONObject().apply {
                    put("color", "#6366F1") // Primary color
                })
                
                // Payment methods
                put("method", JSONObject().apply {
                    put("upi", true)
                    put("card", true)
                    put("netbanking", true)
                    put("wallet", true)
                })
            }
            
            checkout.open(activity, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
