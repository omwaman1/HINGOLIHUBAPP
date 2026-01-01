package com.hingoli.hub.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for SMS Retriever API to automatically detect OTP
 * No SMS permission required!
 */
@Singleton
class SmsRetrieverHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var smsReceiver: BroadcastReceiver? = null
    private var otpCallback: ((String) -> Unit)? = null
    
    companion object {
        private const val TAG = "SmsRetrieverHelper"
    }
    
    /**
     * Start listening for SMS containing OTP
     * @param onOtpReceived Callback when OTP is received
     */
    fun startListening(onOtpReceived: (String) -> Unit) {
        otpCallback = onOtpReceived
        
        // Start SMS Retriever client
        val client = SmsRetriever.getClient(context)
        val task = client.startSmsRetriever()
        
        task.addOnSuccessListener {
            Log.d(TAG, "✅ SMS Retriever started successfully")
            registerReceiver()
        }
        
        task.addOnFailureListener { e ->
            Log.e(TAG, "❌ Failed to start SMS Retriever: ${e.message}")
        }
    }
    
    /**
     * Register broadcast receiver to listen for SMS
     */
    private fun registerReceiver() {
        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
                    val extras = intent.extras
                    val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                    
                    when (status?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                            Log.d(TAG, "✅ SMS received: $message")
                            message?.let { extractOtp(it) }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            Log.d(TAG, "⏱️ SMS Retriever timeout")
                        }
                    }
                }
            }
        }
        
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(smsReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(smsReceiver, intentFilter)
        }
    }
    
    /**
     * Extract 6-digit OTP from SMS message
     */
    private fun extractOtp(message: String) {
        // Regex to find 6-digit OTP
        val pattern = Regex("\\b(\\d{6})\\b")
        val match = pattern.find(message)
        
        match?.let {
            val otp = it.value
            Log.d(TAG, "✅ OTP extracted: $otp")
            otpCallback?.invoke(otp)
        }
    }
    
    /**
     * Stop listening and unregister receiver
     */
    fun stopListening() {
        try {
            smsReceiver?.let {
                context.unregisterReceiver(it)
                smsReceiver = null
            }
            otpCallback = null
            Log.d(TAG, "🛑 SMS Retriever stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping receiver: ${e.message}")
        }
    }
}
