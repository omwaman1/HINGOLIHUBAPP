package com.hingoli.hub.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hingoli.hub.MainActivity
import com.hingoli.hub.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "ChatMessagingService"
        const val CHANNEL_ID = "chat_notifications"
        const val CHANNEL_NAME = "Chat Messages"
        const val LISTING_CHANNEL_ID = "listing_notifications"  // Must match admin.php channel_id
        const val LISTING_CHANNEL_NAME = "New Listings"
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Save token to Firebase for the current user
        // This will be called when token is refreshed
        saveTokenToFirebase(token)
    }
    
    private fun saveTokenToFirebase(token: String) {
        // We'll update this when user logs in
        // For now, store in SharedPreferences
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
        
        Log.d(TAG, "FCM token saved locally")
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "=== FCM MESSAGE RECEIVED ===")
        Log.d(TAG, "From: ${message.from}")
        Log.d(TAG, "Data: ${message.data}")
        Log.d(TAG, "Notification: ${message.notification?.title} - ${message.notification?.body}")
        
        // Extract notification data
        val title = message.notification?.title 
            ?: message.data["title"] 
            ?: "New Message"
        val body = message.notification?.body 
            ?: message.data["body"] 
            ?: "You have a new message"
        val conversationId = message.data["conversationId"]
        
        // Handle call invitation specifically
        val type = message.data["type"]
        Log.d(TAG, "Notification type: $type")
        
        if (type == "call_invitation") {
            val callId = message.data["callId"] ?: return
            val senderName = message.data["senderName"] ?: "Unknown"
            val callerId = message.data["callerId"] ?: "0"
            Log.d(TAG, "CALL INVITATION: callId=$callId, senderName=$senderName")
            
            showCallNotification(title, body, conversationId, callId, senderName, callerId)
            return
        }
        
        // Handle call cancellation - caller ended before receiver answered
        if (type == "call_cancelled") {
            val callId = message.data["callId"] ?: return
            Log.d(TAG, "CALL CANCELLED: callId=$callId")
            
            // Cancel the notification
            val notificationId = callId.hashCode()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            
            // Stop the ringtone
            IncomingCallReceiver.stopRingtone(callId)
            IncomingCallReceiver.stopAllRingtones()
            
            Log.d(TAG, "✓ Call notification dismissed and ringtone stopped for cancelled call")
            return
        }
        
        // Handle listing/broadcast notifications with deep linking
        if (type == "listing_approved" || type == "admin_broadcast") {
            val listingId = message.data["listing_id"]
            val deepLink = message.data["deep_link"]
            Log.d(TAG, "LISTING/BROADCAST: listingId=$listingId, deepLink=$deepLink")
            
            showNotification(title, body, null, listingId, deepLink, type)
            return
        }
        
        // Handle admin panel push notifications
        if (type == "admin_notification") {
            Log.d(TAG, "ADMIN NOTIFICATION: $title - $body")
            showNotification(title, body, null, null, null, "admin_notification")
            return
        }
        
        // Show regular chat notification
        showNotification(title, body, conversationId)
    }
    
    private fun showCallNotification(
        title: String, 
        body: String, 
        conversationId: String?, 
        callId: String,
        senderName: String,
        callerId: String
    ) {
        // Intent to launch VoiceCallActivity directly
        val notificationId = callId.hashCode()
        
        // Answer intent - opens VoiceCallActivity
        val answerIntent = Intent(this, com.hingoli.hub.ui.call.VoiceCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("call_id", callId)
            putExtra("notification_id", notificationId)
            
            val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val currentUserId = prefs.getLong("user_id", 0).toString()
            val currentUserName = prefs.getString("user_name", "User") ?: "User"
            
            putExtra("user_id", currentUserId) 
            putExtra("user_name", currentUserName)
            putExtra("conversation_id", conversationId)
            putExtra("is_incoming", true)
            putExtra("action", "answer")
        }
        
        val answerPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            answerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Decline intent - broadcasts to decline the call
        val declineIntent = Intent(this, IncomingCallReceiver::class.java).apply {
            action = "DECLINE_CALL"
            putExtra("call_id", callId)
            putExtra("notification_id", notificationId)
            putExtra("conversation_id", conversationId)
        }
        
        val declinePendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 1,
            declineIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        val notificationBuilder = NotificationCompat.Builder(this, "call_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📞 $title")
            .setContentText(body)
            .setAutoCancel(false)
            .setOngoing(true) // Make it persistent until answered/declined
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000)) // Vibration pattern
            .setFullScreenIntent(answerPendingIntent, true) // High priority heads-up
            .setContentIntent(answerPendingIntent)
            .setTimeoutAfter(60000) // Auto-dismiss after 60 seconds
            
        // Add "Decline" action button (left)
        val declineAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "✕ Decline",
            declinePendingIntent
        ).build()
        notificationBuilder.addAction(declineAction)
        
        // Add "Answer" action button (right)
        val answerAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_call,
            "✓ Answer",
            answerPendingIntent
        ).build()
        notificationBuilder.addAction(answerAction)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create specific channel for calls - IMPORTANCE_HIGH with custom sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
                
            val channel = NotificationChannel(
                "call_channel",
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming voice call notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true) // Bypass Do Not Disturb for calls
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        // Start ringtone service for continuous ringing
        startRingtoneService(callId, notificationId)
    }
    
    private fun startRingtoneService(callId: String, notificationId: Int) {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
            
            // Register ringtone with IncomingCallReceiver so it can stop it on decline
            IncomingCallReceiver.registerRingtone(callId, ringtone)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.isLooping = true
            }
            ringtone.play()
            
            // Auto-stop after 60 seconds if not answered
            CoroutineScope(Dispatchers.Main).launch {
                kotlinx.coroutines.delay(60000)
                IncomingCallReceiver.stopRingtone(callId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play ringtone: ${e.message}")
        }
    }
    
    private fun showNotification(title: String, body: String, conversationId: String?, listingId: String? = null, deepLink: String? = null, type: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            conversationId?.let { putExtra("conversationId", it) }
            listingId?.let { putExtra("listingId", it) }
            deepLink?.let { putExtra("deepLink", it) }
            type?.let { putExtra("notificationType", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Choose channel based on type
        val channelId = when (type) {
            "listing_approved", "admin_broadcast", "admin_notification" -> LISTING_CHANNEL_ID
            else -> CHANNEL_ID
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channels for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Chat channel
            val chatChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat message notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(chatChannel)
            
            // Listing/broadcast channel
            val listingChannel = NotificationChannel(
                LISTING_CHANNEL_ID,
                LISTING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "New listings and announcements"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(listingChannel)
        }
        
        // Use unique notification ID
        val notificationId = (conversationId ?: listingId ?: System.currentTimeMillis().toString()).hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
