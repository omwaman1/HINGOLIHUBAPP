package com.hingoli.delivery.ui.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Voice call activity for delivery boys to call customers
 * Integrates with the customer's main app via Firebase + ZegoCloud
 */
class VoiceCallActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "VoiceCallActivity"
        private const val EXTRA_CALL_ID = "call_id"
        private const val EXTRA_USER_ID = "user_id"
        private const val EXTRA_USER_NAME = "user_name"
        private const val EXTRA_CUSTOMER_NAME = "customer_name"
        private const val EXTRA_CUSTOMER_USER_ID = "customer_user_id"
        private const val EXTRA_ORDER_ID = "order_id"
        
        // ZegoCloud credentials (same as main app)
        private const val APP_ID: Long = 1580953697
        private const val APP_SIGN = "96cedcc8935d2e3f55bae63ad888064704d5e0018acbd3ab213d7f65423c9403"
        
        // Maximum call duration: 5 minutes
        private const val MAX_CALL_DURATION_MS = 5 * 60 * 1000L
        
        /**
         * Start a voice call to a customer
         * @param customerUserId The customer's user_id in the main app system
         */
        fun start(
            context: Context,
            orderId: Long,
            deliveryUserId: Long,
            deliveryUserName: String,
            customerName: String,
            customerUserId: Long
        ) {
            // Create unique call ID - both parties will use this to join the same ZegoCloud room
            val callId = "delivery_${orderId}_${System.currentTimeMillis()}"
            
            val intent = Intent(context, VoiceCallActivity::class.java).apply {
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_USER_ID, "delivery_$deliveryUserId") // Prefix to avoid collision with customer IDs
                putExtra(EXTRA_USER_NAME, deliveryUserName)
                putExtra(EXTRA_CUSTOMER_NAME, customerName)
                putExtra(EXTRA_CUSTOMER_USER_ID, customerUserId)
                putExtra(EXTRA_ORDER_ID, orderId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private var callTimeoutRunnable: Runnable? = null
    private var hasHandledCallEnd = false
    private var customerUserId: Long = 0
    private var callId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        
        // Keep screen on during call
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Simple FrameLayout for Fragment
        val container = FrameLayout(this).apply { id = android.R.id.content }
        setContentView(container)
        
        callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return finish()
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return finish()
        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "Delivery Partner"
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: "Customer"
        customerUserId = intent.getLongExtra(EXTRA_CUSTOMER_USER_ID, 0)
        val orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0)
        
        Toast.makeText(this, "Calling $customerName...", Toast.LENGTH_SHORT).show()
        
        // Send call invitation to customer via Firebase
        // Customer's app will receive this via FCM and show incoming call notification
        if (customerUserId > 0) {
            sendCallInvitation(customerUserId, callId, userName, orderId)
        }
        
        setupCallFragment(callId, userId, userName)
        setupCallTimeout()
    }
    
    /**
     * Send call invitation to customer via Firebase
     * This triggers FCM notification on customer's main app
     */
    private fun sendCallInvitation(customerId: Long, callId: String, senderName: String, orderId: Long) {
        Log.d(TAG, "=== SENDING CALL INVITATION TO CUSTOMER ===" )
        Log.d(TAG, "Customer ID: $customerId, Call ID: $callId, Sender: $senderName")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = FirebaseDatabase.getInstance()
                
                // Create a conversation ID for this delivery call
                val conversationId = "delivery_order_$orderId"
                
                // Store call status in Firebase (for tracking)
                val callRef = database.getReference("calls")
                    .child(conversationId)
                    .child(callId)
                
                callRef.setValue(mapOf(
                    "status" to "ringing",
                    "callerId" to "delivery",
                    "startedAt" to System.currentTimeMillis()
                )).await()
                
                // Send notification to customer
                // This is picked up by Firebase Cloud Functions and delivered via FCM
                val notificationRef = database.getReference("notifications")
                    .child(customerId.toString())
                    .push()
                
                val notification = mapOf(
                    "title" to "Delivery Partner Calling",
                    "body" to "Your delivery partner is calling you",
                    "conversationId" to conversationId,
                    "type" to "call_invitation",
                    "callId" to callId,
                    "senderName" to senderName,
                    "callerId" to "delivery",
                    "timestamp" to System.currentTimeMillis()
                )
                
                notificationRef.setValue(notification).await()
                Log.d(TAG, "✓ Call invitation sent to customer $customerId")
                
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to send call invitation: ${e.message}", e)
                handler.post {
                    Toast.makeText(
                        this@VoiceCallActivity,
                        "Failed to notify customer. They may not receive the call.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Send call cancellation when delivery person ends call
     */
    private fun sendCallCancellation() {
        if (customerUserId <= 0 || callId.isEmpty()) return
        
        Log.d(TAG, "Sending call cancellation to customer $customerUserId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = FirebaseDatabase.getInstance()
                val orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0)
                val conversationId = "delivery_order_$orderId"
                
                // Update call status
                database.getReference("calls")
                    .child(conversationId)
                    .child(callId)
                    .child("status")
                    .setValue("ended")
                    .await()
                
                // Send cancellation notification
                val notificationRef = database.getReference("notifications")
                    .child(customerUserId.toString())
                    .push()
                
                notificationRef.setValue(mapOf(
                    "title" to "Call Ended",
                    "body" to "Call was ended",
                    "type" to "call_cancelled",
                    "callId" to callId,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                
                Log.d(TAG, "✓ Call cancellation sent")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send cancellation: ${e.message}")
            }
        }
    }
    
    private fun setupCallTimeout() {
        callTimeoutRunnable = Runnable {
            Log.d(TAG, "Call timeout reached (5 minutes)")
            Toast.makeText(this, "Call ended - Maximum duration reached", Toast.LENGTH_LONG).show()
            handleCallEnd()
        }
        handler.postDelayed(callTimeoutRunnable!!, MAX_CALL_DURATION_MS)
    }
    
    private fun cancelCallTimeout() {
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }
        callTimeoutRunnable = null
    }
    
    private fun handleCallEnd() {
        if (hasHandledCallEnd) return
        hasHandledCallEnd = true
        
        cancelCallTimeout()
        sendCallCancellation()
        finish()
    }
    
    private fun setupCallFragment(callId: String, userId: String, userName: String) {
        Log.d(TAG, "Setting up ZegoCloud call: callId=$callId, userId=$userId")
        
        val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall().apply {
            turnOnCameraWhenJoining = false
            turnOnMicrophoneWhenJoining = true
            useSpeakerWhenJoining = true
            hangUpConfirmDialogInfo = null
        }
        
        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            APP_ID,
            APP_SIGN,
            userId,
            userName,
            callId,
            config
        )
        
        // When only self in room (customer left or didn't join)
        fragment.setOnOnlySelfInRoomListener {
            Log.d(TAG, "Other user left the call")
            handler.post {
                Toast.makeText(this@VoiceCallActivity, "Call ended", Toast.LENGTH_SHORT).show()
            }
            handleCallEnd()
        }
        
        try {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNowAllowingStateLoss()
            Log.d(TAG, "ZegoCloud fragment attached")
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching fragment: ${e.message}")
            finish()
        }
    }
    
    override fun onDestroy() {
        if (!hasHandledCallEnd) {
            handleCallEnd()
        }
        super.onDestroy()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleCallEnd()
    }
}
