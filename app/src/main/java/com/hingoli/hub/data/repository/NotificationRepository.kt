package com.hingoli.hub.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.RegisterFcmTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    private val database = FirebaseDatabase.getInstance()
    private val userTokensRef = database.getReference("userTokens")
    
    companion object {
        private const val TAG = "NotificationRepository"
        private const val PREFS_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
    
    /**
     * Get the current FCM token and save it to Firebase and Backend API
     */
    suspend fun registerFcmToken(userId: Long) {
        Log.d(TAG, "=== REGISTERING FCM TOKEN ===")
        Log.d(TAG, "For user: $userId")
        
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM token retrieved: ${token.take(20)}...")
            
            // Save token to Firebase
            Log.d(TAG, "Saving to /userTokens/$userId")
            userTokensRef.child(userId.toString()).setValue(token).await()
            Log.d(TAG, "✓ FCM token saved to Firebase successfully!")
            
            // Also register with backend API
            try {
                val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"
                val request = RegisterFcmTokenRequest(
                    fcmToken = token,
                    userId = userId,
                    deviceInfo = deviceInfo
                )
                val response = apiService.registerFcmToken(request)
                if (response.isSuccessful) {
                    Log.d(TAG, "✓ FCM token registered with backend API")
                } else {
                    Log.e(TAG, "✗ Backend API registration failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Backend API registration error: ${e.message}")
            }
            
            // Also save locally
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FCM_TOKEN, token)
                .apply()
            Log.d(TAG, "✓ FCM token saved locally")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to register FCM token: ${e.message}", e)
        }
    }
    
    /**
     * Remove FCM token when user logs out
     */
    suspend fun unregisterFcmToken(userId: Long) {
        try {
            userTokensRef.child(userId.toString()).removeValue().await()
            Log.d(TAG, "FCM token removed for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove FCM token: ${e.message}")
        }
    }
    
    /**
     * Get FCM token for a specific user (for sending notifications)
     */
    suspend fun getUserFcmToken(userId: Long): String? {
        return try {
            val snapshot = userTokensRef.child(userId.toString()).get().await()
            snapshot.getValue(String::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token for user $userId: ${e.message}")
            null
        }
    }
}
