package com.hingoli.hub.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver to handle declining incoming calls from notification
 */
class IncomingCallReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "IncomingCallReceiver"
        
        // Static reference to stop ringtone from ChatMessagingService
        private var activeRingtones = mutableMapOf<String, android.media.Ringtone>()
        
        fun registerRingtone(callId: String, ringtone: android.media.Ringtone) {
            activeRingtones[callId] = ringtone
        }
        
        fun stopRingtone(callId: String) {
            activeRingtones[callId]?.let { ringtone ->
                try {
                    if (ringtone.isPlaying) {
                        ringtone.stop()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping ringtone: ${e.message}")
                }
                activeRingtones.remove(callId)
            }
        }
        
        fun stopAllRingtones() {
            activeRingtones.forEach { (_, ringtone) ->
                try {
                    if (ringtone.isPlaying) {
                        ringtone.stop()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping ringtone: ${e.message}")
                }
            }
            activeRingtones.clear()
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received action: $action")
        
        if (action == "DECLINE_CALL") {
            val callId = intent.getStringExtra("call_id") ?: return
            val notificationId = intent.getIntExtra("notification_id", 0)
            val conversationId = intent.getStringExtra("conversation_id")
            
            Log.d(TAG, "Declining call: $callId")
            
            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            
            // Stop the ringtone
            stopRingtone(callId)
            stopAllRingtones() // Also stop any other ringtones
            
            // Update Firebase to mark call as declined - this will notify the caller
            if (conversationId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val callRef = FirebaseDatabase.getInstance()
                            .getReference("calls")
                            .child(conversationId)
                            .child(callId)
                        
                        callRef.child("status").setValue("declined")
                        callRef.child("endedAt").setValue(System.currentTimeMillis())
                        
                        Log.d(TAG, "Call marked as declined in Firebase")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update Firebase: ${e.message}")
                    }
                }
            }
        }
    }
}
