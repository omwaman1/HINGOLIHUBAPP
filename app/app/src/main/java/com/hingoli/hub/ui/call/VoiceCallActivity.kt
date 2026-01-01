package com.hingoli.hub.ui.call

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hingoli.hub.data.repository.ChatRepository
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Lightweight voice call activity using ZegoCloud
 * Supports both outgoing and incoming calls with full-screen lock screen UI
 */
class VoiceCallActivity : AppCompatActivity() {
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ChatRepositoryEntryPoint {
        fun chatRepository(): ChatRepository
    }
    
    private lateinit var chatRepository: ChatRepository
    private var wakeLock: PowerManager.WakeLock? = null
    
    // Flag to prevent duplicate finish calls
    private var isActivityFinishing = false
    private var hasHandledCallEnd = false
    
    companion object {
        private const val TAG = "VoiceCallActivity"
        private const val EXTRA_CALL_ID = "call_id"
        private const val EXTRA_USER_ID = "user_id"
        private const val EXTRA_USER_NAME = "user_name"
        private const val EXTRA_CONVERSATION_ID = "conversation_id"
        private const val EXTRA_TARGET_USER_ID = "target_user_id"
        private const val EXTRA_IS_INCOMING = "is_incoming"
        private const val EXTRA_NOTIFICATION_ID = "notification_id"
        
        // ZegoCloud credentials (should be in BuildConfig)
        private const val APP_ID: Long = 1580953697
        private const val APP_SIGN = "96cedcc8935d2e3f55bae63ad888064704d5e0018acbd3ab213d7f65423c9403"
        
        // Maximum call duration: 5 minutes (in milliseconds)
        private const val MAX_CALL_DURATION_MS = 5 * 60 * 1000L
        
        fun start(
            context: Context,
            callId: String,
            userId: String,
            userName: String,
            conversationId: String? = null,
            targetUserId: Long = 0,
            isIncoming: Boolean = false,
            notificationId: Int = 0
        ) {
            val intent = Intent(context, VoiceCallActivity::class.java).apply {
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_USER_NAME, userName)
                putExtra(EXTRA_CONVERSATION_ID, conversationId)
                putExtra(EXTRA_TARGET_USER_ID, targetUserId)
                putExtra(EXTRA_IS_INCOMING, isIncoming)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                
                if (isIncoming) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            }
            context.startActivity(intent)
        }
    }
    
    private var callStartTime: Long = 0
    private var conversationId: String? = null
    private var isIncoming: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private var callTimeoutRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        
        // Enable showing on lock screen
        setupLockScreenFlags()
        
        // Acquire wake lock to keep screen on
        acquireWakeLock()
        
        // Initialize repository via EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ChatRepositoryEntryPoint::class.java
        )
        chatRepository = entryPoint.chatRepository()
        
        // Dismiss notification immediately when activity starts
        dismissNotification()
        
        // Stop any ringtones that might be playing
        com.hingoli.hub.service.IncomingCallReceiver.stopAllRingtones()
        
        // Simple FrameLayout for Fragment
        val container = FrameLayout(this).apply { id = android.R.id.content }
        setContentView(container)
        
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return safeFinish()
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return safeFinish()
        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "User"
        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID)
        val targetUserId = intent.getLongExtra(EXTRA_TARGET_USER_ID, 0)
        isIncoming = intent.getBooleanExtra(EXTRA_IS_INCOMING, false)
        
        callStartTime = System.currentTimeMillis()
        
        // If outgoing, send invitation to target user
        if (!isIncoming && targetUserId > 0) {
            sendCallInvitation(targetUserId, conversationId ?: "", callId, userName)
        }
        
        // Listen for call status changes (declined, ended, etc.) - for BOTH incoming and outgoing calls
        // This ensures both parties get notified when the other ends the call
        listenForCallStatus(conversationId ?: "", callId)
        
        setupCallFragment(callId, userId, userName)
        
        // Set up auto-end after 5 minutes
        setupCallTimeout()
    }
    
    private var callStatusListener: com.google.firebase.database.ValueEventListener? = null
    private var callStatusRef: com.google.firebase.database.DatabaseReference? = null
    
    /**
     * Listen for call status changes in Firebase (for outgoing calls)
     * If recipient declines, this will trigger call end
     */
    private fun listenForCallStatus(conversationId: String, callId: String) {
        if (conversationId.isEmpty()) return
        
        Log.d(TAG, "Setting up call status listener for: $conversationId/$callId")
        
        callStatusRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("calls")
            .child(conversationId)
            .child(callId)
        
        callStatusListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                Log.d(TAG, "Call status changed: $status")
                
                when (status) {
                    "declined" -> {
                        Log.d(TAG, "Call was declined by recipient")
                        handler.post {
                            Toast.makeText(
                                this@VoiceCallActivity,
                                "Call declined",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        handleCallEnd("declined")
                    }
                    "ended" -> {
                        Log.d(TAG, "Call was ended by recipient")
                        handleCallEnd("remote_hangup")
                    }
                }
            }
            
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e(TAG, "Call status listener cancelled: ${error.message}")
            }
        }
        
        callStatusRef?.addValueEventListener(callStatusListener!!)
    }
    
    private fun removeCallStatusListener() {
        callStatusListener?.let { listener ->
            callStatusRef?.removeEventListener(listener)
        }
        callStatusListener = null
        callStatusRef = null
    }
    
    /**
     * Safe finish that prevents duplicate calls
     */
    private fun safeFinish() {
        if (isActivityFinishing) {
            Log.d(TAG, "safeFinish: Already finishing, skipping")
            return
        }
        isActivityFinishing = true
        Log.d(TAG, "safeFinish: Finishing activity")
        
        // Run on main thread to prevent race conditions
        handler.post {
            if (!isFinishing && !isDestroyed) {
                finish()
            }
        }
    }
    
    /**
     * Set window flags to show activity on lock screen
     */
    private fun setupLockScreenFlags() {
        // Add keep screen on flag
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            try {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error dismissing keyguard: ${e.message}")
            }
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }
    
    /**
     * Acquire wake lock to ensure screen stays on during call
     */
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or 
                PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "HINGOLI HUB::IncomingCallWakeLock"
            )
            wakeLock?.acquire(MAX_CALL_DURATION_MS)
            Log.d(TAG, "✓ Wake lock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring wake lock: ${e.message}")
        }
    }
    
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "✓ Wake lock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock: ${e.message}")
        }
        wakeLock = null
    }
    
    private fun dismissNotification() {
        try {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (notificationId != 0) {
                notificationManager.cancel(notificationId)
                Log.d(TAG, "✓ Dismissed notification: $notificationId")
            }
            
            // Also dismiss all call notifications
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing notification: ${e.message}")
        }
    }
    
    private fun setupCallTimeout() {
        callTimeoutRunnable = Runnable {
            Log.d(TAG, "⏰ Call timeout reached (5 minutes). Ending call...")
            handler.post {
                Toast.makeText(
                    this,
                    "Call ended - Maximum duration (5 minutes) reached",
                    Toast.LENGTH_LONG
                ).show()
            }
            handleCallEnd("timeout")
        }
        
        handler.postDelayed(callTimeoutRunnable!!, MAX_CALL_DURATION_MS)
        Log.d(TAG, "⏱️ Call timeout scheduled for 5 minutes")
    }
    
    private fun cancelCallTimeout() {
        callTimeoutRunnable?.let {
            handler.removeCallbacks(it)
            Log.d(TAG, "⏱️ Call timeout cancelled")
        }
        callTimeoutRunnable = null
    }
    
    /**
     * Handle call end - save call log and finish activity
     */
    private fun handleCallEnd(reason: String) {
        if (hasHandledCallEnd) {
            Log.d(TAG, "handleCallEnd: Already handled, skipping (reason: $reason)")
            return
        }
        hasHandledCallEnd = true
        Log.d(TAG, "handleCallEnd: Processing call end (reason: $reason)")
        
        cancelCallTimeout()
        releaseWakeLock()
        
        val duration = (System.currentTimeMillis() - callStartTime) / 1000
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
        val targetUserId = intent.getLongExtra(EXTRA_TARGET_USER_ID, 0)
        
        // If this is an outgoing call and caller is ending it (local_hangup or timeout),
        // send cancellation to the recipient so their notification is dismissed
        val conversationIdLocal = conversationId
        if (!isIncoming && targetUserId > 0 && (reason == "local_hangup" || reason == "timeout")) {
            Log.d(TAG, "Sending call cancellation to recipient: $targetUserId")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    chatRepository.sendCallCancellation(
                        recipientId = targetUserId,
                        conversationId = conversationIdLocal ?: "",
                        callId = callId
                    )
                    Log.d(TAG, "✓ Call cancellation sent to recipient")
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending call cancellation: ${e.message}")
                }
            }
        }
        
        // Update Firebase call status to "ended" for BOTH incoming and outgoing calls
        // This notifies the other party that the call has ended
        if (!conversationIdLocal.isNullOrEmpty() && (reason == "local_hangup" || reason == "timeout")) {
            Log.d(TAG, "Updating Firebase call status to 'ended' for: $conversationIdLocal/$callId")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("calls")
                        .child(conversationIdLocal)
                        .child(callId)
                        .child("status")
                        .setValue("ended")
                    Log.d(TAG, "✓ Firebase call status set to 'ended'")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating Firebase call status: ${e.message}")
                }
            }
        }
        
        // Save call log to chat conversation (if conversationId is a real chat ID, not call sync ID)
        // Only save for actual chat conversations, not for call sync IDs that start with "call_"
        val shouldSaveLog = conversationId != null && 
                            !conversationId!!.startsWith("call_") && 
                            (reason == "remote_hangup" || reason == "local_hangup" || reason == "completed")
        
        Log.d(TAG, "Call log check: convId=$conversationId, duration=$duration, reason=$reason, shouldSave=$shouldSaveLog")
        
        if (shouldSaveLog) {
            val currentUserId = intent.getStringExtra(EXTRA_USER_ID)?.toLongOrNull() ?: 0
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    chatRepository.saveCallMessage(
                        conversationId = conversationId!!,
                        senderId = currentUserId,
                        duration = duration,
                        status = if (reason == "remote_hangup") "completed" else reason
                    )
                    Log.d(TAG, "✓ Call log saved: $duration seconds, reason: $reason")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving call log: ${e.message}")
                }
            }
        }
        
        safeFinish()
    }
    
    private fun sendCallInvitation(
        recipientId: Long,
        conversationId: String,
        callId: String,
        senderName: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            chatRepository.sendCallInvitation(
                recipientId = recipientId,
                conversationId = conversationId,
                callId = callId,
                senderName = senderName
            )
        }
    }
    
    private fun setupCallFragment(callId: String, userId: String, userName: String) {
        Log.d(TAG, "Setting up ZegoCloud call fragment: callId=$callId, userId=$userId")
        
        val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall().apply {
            turnOnCameraWhenJoining = false
            turnOnMicrophoneWhenJoining = true
            useSpeakerWhenJoining = true
            
            // Disable hang up confirmation dialog for instant close
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
        
        // Set listener for when only this user is left in the room (other user left)
        // This triggers when the other user hangs up
        fragment.setOnOnlySelfInRoomListener {
            Log.d(TAG, "onOnlySelfInRoom: Other user left the call")
            handler.post {
                Toast.makeText(
                    this@VoiceCallActivity,
                    "Call ended by other user",
                    Toast.LENGTH_SHORT
                ).show()
            }
            handleCallEnd("remote_hangup")
        }
        
        try {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNowAllowingStateLoss()
            Log.d(TAG, "✓ ZegoCloud fragment attached")
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching fragment: ${e.message}")
            safeFinish()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called - activity already running")
        // Activity already running, ignore duplicate intent
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy called, hasHandledCallEnd=$hasHandledCallEnd")
        
        // If handleCallEnd wasn't called yet (e.g., ZegoCloud's hang up button pressed),
        // call it now to update Firebase and notify the other user
        if (!hasHandledCallEnd) {
            handleCallEnd("local_hangup")
        }
        
        // Clean up resources
        cancelCallTimeout()
        releaseWakeLock()
        removeCallStatusListener()
        
        super.onDestroy()
    }
    
    override fun onBackPressed() {
        // Show confirmation or just end the call
        handleCallEnd("local_hangup")
    }
}

