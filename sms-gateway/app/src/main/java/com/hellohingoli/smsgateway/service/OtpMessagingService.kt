package com.hellohingoli.smsgateway.service

import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hellohingoli.smsgateway.api.GatewayApiClient
import com.hellohingoli.smsgateway.data.AppDatabase
import com.hellohingoli.smsgateway.data.DeviceSettings
import com.hellohingoli.smsgateway.data.OtpHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * FCM Service that receives OTP requests and sends SMS
 */
class OtpMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "OtpMessagingService"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var settings: DeviceSettings
    private lateinit var apiClient: GatewayApiClient
    
    override fun onCreate() {
        super.onCreate()
        settings = DeviceSettings(applicationContext)
        apiClient = GatewayApiClient(settings.apiUrl)
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        registerGatewayToken(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Check if this is an OTP request
        val data = remoteMessage.data
        if (data["type"] == "otp_request") {
            val phone = data["phone"]
            val otp = data["otp"]
            val requestId = data["requestId"]
            
            Log.d(TAG, "OTP Request - Phone: $phone, OTP: $otp, RequestId: $requestId")
            
            if (!phone.isNullOrEmpty() && !otp.isNullOrEmpty()) {
                sendSms(phone, otp, requestId)
            }
        }
    }
    
    private fun sendSms(phone: String, otp: String, requestId: String?) {
        val db = AppDatabase.getInstance(applicationContext)
        val senderPhone = settings.getSenderPhone()
        
        try {
            // Build message from template
            val message = settings.smsTemplate.replace("{otp}", otp)
            
            // Format phone number (add country code if needed)
            val formattedPhone = if (phone.startsWith("+")) phone else "+91$phone"
            
            // Get SMS manager (with SIM selection for Android 5.1+)
            val smsManager = getSmsManager()
            smsManager.sendTextMessage(
                formattedPhone,
                null,
                message,
                null,
                null
            )
            
            Log.d(TAG, "SMS sent successfully to $formattedPhone from SIM ${settings.activeSim}")
            
            // Save to local database
            serviceScope.launch {
                db.otpHistoryDao().insert(
                    OtpHistoryEntity(
                        phone = phone,
                        otp = otp,
                        status = "sent",
                        requestId = requestId
                    )
                )
                
                // Log to TiDB
                apiClient.logOtp(
                    deviceId = settings.deviceId,
                    deviceName = settings.deviceName,
                    senderPhone = senderPhone,
                    recipientPhone = phone,
                    otpCode = otp,
                    status = "sent",
                    requestId = requestId
                )
            }
            
            // Update status in Firebase
            requestId?.let { updateStatus(it, "sent") }
            
            // Broadcast to MainActivity for UI update
            val intent = android.content.Intent("com.hellohingoli.smsgateway.SMS_SENT")
            intent.putExtra("phone", phone)
            intent.putExtra("status", "sent")
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
            
            // Save failed attempt to database
            serviceScope.launch {
                db.otpHistoryDao().insert(
                    OtpHistoryEntity(
                        phone = phone,
                        otp = otp,
                        status = "failed",
                        requestId = requestId
                    )
                )
                
                // Log to TiDB
                apiClient.logOtp(
                    deviceId = settings.deviceId,
                    deviceName = settings.deviceName,
                    senderPhone = senderPhone,
                    recipientPhone = phone,
                    otpCode = otp,
                    status = "failed",
                    errorMessage = e.message,
                    requestId = requestId
                )
            }
            
            requestId?.let { updateStatus(it, "failed", e.message) }
        }
    }
    
    @Suppress("DEPRECATION")
    private fun getSmsManager(): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            applicationContext.getSystemService(SmsManager::class.java)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Android 5.1+ with dual SIM support
            try {
                val subscriptionManager = getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                
                if (subscriptionInfoList != null && subscriptionInfoList.size >= settings.activeSim) {
                    val subscriptionInfo = subscriptionInfoList[settings.activeSim - 1]
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.subscriptionId)
                } else {
                    SmsManager.getDefault()
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "No permission to access subscription info, using default SIM")
                SmsManager.getDefault()
            }
        } else {
            SmsManager.getDefault()
        }
    }
    
    private fun updateStatus(requestId: String, status: String, error: String? = null) {
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "sentAt" to System.currentTimeMillis(),
            "sentBy" to settings.deviceId
        )
        error?.let { updates["error"] = it }
        
        FirebaseDatabase.getInstance()
            .getReference("otp_requests/$requestId")
            .updateChildren(updates)
    }
    
    private fun registerGatewayToken(token: String) {
        serviceScope.launch {
            apiClient.registerDevice(
                deviceId = settings.deviceId,
                deviceName = settings.deviceName,
                fcmToken = token,
                sim1Phone = settings.sim1Phone,
                sim2Phone = settings.sim2Phone
            )
        }
        
        FirebaseDatabase.getInstance()
            .getReference("gateway_devices/${settings.deviceId}")
            .setValue(mapOf(
                "token" to token,
                "deviceName" to settings.deviceName,
                "updatedAt" to System.currentTimeMillis(),
                "status" to "online"
            ))
    }
}
