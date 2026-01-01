package com.hingoli.hub.data.repository

import android.content.Context
import com.google.firebase.database.*
import com.hingoli.hub.data.api.TokenManager
import com.hingoli.hub.data.model.ChatMessage
import com.hingoli.hub.data.model.Conversation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {
    private val database = FirebaseDatabase.getInstance()
    private val conversationsRef = database.getReference("conversations")
    private val messagesRef = database.getReference("messages")
    private val userConversationsRef = database.getReference("userConversations")
    private val userTokensRef = database.getReference("userTokens")
    
    /**
     * Get all conversations for the current user as a Flow
     */
    fun getConversations(userId: Long): Flow<List<Conversation>> = callbackFlow {
        val userConvsRef = userConversationsRef.child(userId.toString())
        
        android.util.Log.d("ChatRepository", "Setting up listener for userConversations/$userId")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("ChatRepository", "onDataChange triggered, exists=${snapshot.exists()}, childrenCount=${snapshot.childrenCount}")
                
                val conversationIds = snapshot.children.mapNotNull { it.key }
                
                if (conversationIds.isEmpty()) {
                    android.util.Log.d("ChatRepository", "No conversations found, sending empty list")
                    trySend(emptyList())
                    return
                }
                
                android.util.Log.d("ChatRepository", "Found ${conversationIds.size} conversation IDs: $conversationIds")
                
                // Fetch each conversation
                val conversations = mutableListOf<Conversation>()
                var fetchedCount = 0
                
                conversationIds.forEach { convId ->
                    conversationsRef.child(convId).get()
                        .addOnSuccessListener { convSnapshot ->
                            android.util.Log.d("ChatRepository", "Fetched conversation $convId, exists=${convSnapshot.exists()}")
                            
                            val conv = convSnapshot.getValue(Conversation::class.java)
                            android.util.Log.d("ChatRepository", "Parsed conversation: $conv")
                            
                            if (conv != null) {
                                conv.conversationId = convId
                                
                                // Get the other user's ID and name
                                val otherUserId = conv.participantIds.firstOrNull { it != userId }
                                if (otherUserId != null) {
                                    // Try to get stored name from conversation
                                    val storedName = convSnapshot.child("participant_${otherUserId}_name")
                                        .getValue(String::class.java)
                                    conv.otherUserName = storedName ?: "User $otherUserId"
                                }
                                
                                // Calculate unread count (fetch all messages and filter in code)
                                messagesRef.child(convId).get()
                                    .addOnSuccessListener { msgSnapshot ->
                                        conv.unreadCount = msgSnapshot.children.count { msg ->
                                            val isRead = msg.child("isRead").getValue(Boolean::class.java) ?: true
                                            val senderId = msg.child("senderId").getValue(Long::class.java)
                                            !isRead && senderId != userId
                                        }
                                        conversations.add(conv)
                                        fetchedCount++
                                        
                                        if (fetchedCount == conversationIds.size) {
                                            android.util.Log.d("ChatRepository", "All conversations fetched, sending ${conversations.size}")
                                            trySend(conversations.sortedByDescending { it.lastMessageAt })
                                        }
                                    }.addOnFailureListener { e ->
                                        android.util.Log.e("ChatRepository", "Failed to fetch messages for $convId: ${e.message}")
                                        // Still add the conversation even if messages fail
                                        conversations.add(conv)
                                        fetchedCount++
                                        if (fetchedCount == conversationIds.size) {
                                            trySend(conversations.sortedByDescending { it.lastMessageAt })
                                        }
                                    }
                            } else {
                                android.util.Log.e("ChatRepository", "Failed to parse conversation $convId, raw data: ${convSnapshot.value}")
                                fetchedCount++
                                if (fetchedCount == conversationIds.size) {
                                    trySend(conversations.sortedByDescending { it.lastMessageAt })
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("ChatRepository", "Failed to fetch conversation $convId: ${e.message}")
                            fetchedCount++
                            if (fetchedCount == conversationIds.size) {
                                trySend(conversations.sortedByDescending { it.lastMessageAt })
                            }
                        }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepository", "Firebase error: ${error.message}")
                close(error.toException())
            }
        }
        
        userConvsRef.addValueEventListener(listener)
        awaitClose { userConvsRef.removeEventListener(listener) }
    }
    
    /**
     * Get messages for a conversation as a Flow
     */
    fun getMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val msgsRef = messagesRef.child(conversationId)
        
        android.util.Log.d("ChatRepository", "Setting up messages listener for $conversationId")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("ChatRepository", "Messages onDataChange: ${snapshot.childrenCount} messages")
                
                val messages = snapshot.children.mapNotNull { msgSnapshot ->
                    msgSnapshot.getValue(ChatMessage::class.java)?.apply {
                        messageId = msgSnapshot.key ?: ""
                    }
                }.sortedBy { it.timestamp }
                
                trySend(messages)
            }
            
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChatRepository", "Messages listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        
        msgsRef.addValueEventListener(listener)
        awaitClose { 
            android.util.Log.d("ChatRepository", "Removing messages listener for $conversationId")
            msgsRef.removeEventListener(listener) 
        }
    }
    
    /**
     * Find or create a conversation between two users.
     * One conversation per user pair (not per listing).
     * Still sends listing card when starting from a listing.
     */
    suspend fun getOrCreateConversation(
        currentUserId: Long,
        otherUserId: Long,
        listingId: Long,
        listingTitle: String,
        listingImage: String?,
        otherUserName: String? = null
    ): String {
        // Check if conversation already exists between these users (regardless of listing)
        val existingConvId = findExistingConversation(currentUserId, otherUserId)
        
        if (existingConvId != null) {
            // Conversation exists - just send listing card message
            sendListingCardMessage(existingConvId, currentUserId, listingId, listingTitle, listingImage)
            return existingConvId
        }
        
        // Create new conversation
        val convId = conversationsRef.push().key ?: throw Exception("Failed to create conversation")
        val now = System.currentTimeMillis()
        
        // Get current user's name to store in conversation
        val currentUserName = tokenManager.getUserName() ?: "User"
        
        val conversation = mapOf(
            "participantIds" to listOf(currentUserId, otherUserId),
            "participant_${currentUserId}_name" to currentUserName, // Store current user's name under their own key
            "participant_${otherUserId}_name" to (otherUserName ?: "User"), // Store other user's name under their key
            "lastMessage" to "",
            "lastMessageAt" to now,
            "createdAt" to now
        )
        
        // Create conversation and link to both users
        val updates = mapOf(
            "/conversations/$convId" to conversation,
            "/userConversations/$currentUserId/$convId" to true,
            "/userConversations/$otherUserId/$convId" to true
        )
        
        database.reference.updateChildren(updates).await()
        
        // Send listing card as first message
        sendListingCardMessage(convId, currentUserId, listingId, listingTitle, listingImage)
        
        return convId
    }
    
    /**
     * Find existing conversation between two users (regardless of listing)
     */
    private suspend fun findExistingConversation(
        userId1: Long,
        userId2: Long
    ): String? {
        val userConvs = userConversationsRef.child(userId1.toString()).get().await()
        
        for (convSnapshot in userConvs.children) {
            val convId = convSnapshot.key ?: continue
            val conv = conversationsRef.child(convId).get().await()
            val participants = conv.child("participantIds").children.mapNotNull { 
                it.getValue(Long::class.java) 
            }
            
            // Check if both users are participants (regardless of listing)
            if (participants.containsAll(listOf(userId1, userId2))) {
                return convId
            }
        }
        return null
    }
    
    /**
     * Get or create a support/help conversation with admin.
     * No listing involved - just a direct chat with support.
     */
    suspend fun getOrCreateSupportConversation(
        currentUserId: Long,
        adminUserId: Long = 450002, // Default admin user ID
        adminName: String = "HINGOLI HUB Support"
    ): String {
        // Check if conversation already exists with admin
        val existingConvId = findExistingConversation(currentUserId, adminUserId)
        
        if (existingConvId != null) {
            return existingConvId
        }
        
        // Create new support conversation
        val convId = conversationsRef.push().key ?: throw Exception("Failed to create conversation")
        val now = System.currentTimeMillis()
        
        val currentUserName = tokenManager.getUserName() ?: "User"
        
        val conversation = mapOf(
            "participantIds" to listOf(currentUserId, adminUserId),
            "participant_${currentUserId}_name" to currentUserName,
            "participant_${adminUserId}_name" to adminName,
            "lastMessage" to "Support chat started",
            "lastMessageAt" to now,
            "createdAt" to now,
            "isSupport" to true // Flag to identify support chats
        )
        
        val updates = mapOf(
            "/conversations/$convId" to conversation,
            "/userConversations/$currentUserId/$convId" to true,
            "/userConversations/$adminUserId/$convId" to true
        )
        
        database.reference.updateChildren(updates).await()
        
        // Send welcome message
        val welcomeMsgId = messagesRef.child(convId).push().key
        if (welcomeMsgId != null) {
            val welcomeMessage = mapOf(
                "senderId" to adminUserId,
                "text" to "Welcome to HINGOLI HUB Support! How can we help you today?",
                "type" to "text",
                "timestamp" to now,
                "isRead" to false
            )
            database.reference.child("/messages/$convId/$welcomeMsgId").setValue(welcomeMessage).await()
        }
        
        return convId
    }
    
    private suspend fun sendListingCardMessage(
        conversationId: String,
        senderId: Long,
        listingId: Long,
        listingTitle: String,
        listingImage: String?
    ) {
        val msgId = messagesRef.child(conversationId).push().key ?: return
        val now = System.currentTimeMillis()
        
        val message = mapOf(
            "senderId" to senderId,
            "text" to "Inquiry about: $listingTitle",
            "type" to "listing_card",
            "timestamp" to now,
            "isRead" to false,
            "listingId" to listingId,
            "listingTitle" to listingTitle,
            "listingImage" to listingImage
        )
        
        val updates = mapOf(
            "/messages/$conversationId/$msgId" to message,
            "/conversations/$conversationId/lastMessage" to "Inquiry about: $listingTitle",
            "/conversations/$conversationId/lastMessageAt" to now
        )
        
        database.reference.updateChildren(updates).await()
    }
    
    /**
     * Send a text message
     */
    suspend fun sendMessage(
        conversationId: String,
        senderId: Long,
        text: String
    ) {
        val msgId = messagesRef.child(conversationId).push().key ?: return
        val now = System.currentTimeMillis()
        
        val message = mapOf(
            "senderId" to senderId,
            "text" to text,
            "type" to "text",
            "timestamp" to now,
            "isRead" to false
        )
        
        val updates = mapOf(
            "/messages/$conversationId/$msgId" to message,
            "/conversations/$conversationId/lastMessage" to text,
            "/conversations/$conversationId/lastMessageAt" to now
        )
        
        database.reference.updateChildren(updates).await()
        
        // Get recipient and send push notification
        val conv = conversationsRef.child(conversationId).get().await()
        val participants = conv.child("participantIds").children.mapNotNull { 
            it.getValue(Long::class.java) 
        }
        val recipientId = participants.firstOrNull { it != senderId }
        
        if (recipientId != null) {
            // Get sender name for notification
            val senderName = tokenManager.getUserName() ?: "Someone"
            sendPushNotification(recipientId, senderName, text, conversationId)
        }
    }
    
    /**
     * Send push notification to a user via Firebase Cloud Messaging
     * Note: This uses a simple approach - for production, use Firebase Cloud Functions
     */
    private fun sendPushNotification(
        recipientId: Long,
        senderName: String,
        messageText: String,
        conversationId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get recipient's FCM token from Firebase
                val tokenSnapshot = userTokensRef.child(recipientId.toString()).get().await()
                val recipientToken = tokenSnapshot.getValue(String::class.java)
                
                if (recipientToken != null) {
                    android.util.Log.d("ChatRepository", "Sending push notification to $recipientId")
                    
                    // Store notification in Firebase for later delivery
                    // The recipient's device will receive it via FCM when online
                    val notificationRef = database.getReference("notifications")
                        .child(recipientId.toString())
                        .push()
                    
                    val notification = mapOf(
                        "title" to senderName,
                        "body" to messageText,
                        "conversationId" to conversationId,
                        "timestamp" to System.currentTimeMillis(),
                        "read" to false
                    )
                    
                    notificationRef.setValue(notification).await()
                    android.util.Log.d("ChatRepository", "Notification stored for user $recipientId")
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Failed to send push notification: ${e.message}")
            }
        }
    }
    
    /**
     * Save a call record message to the conversation
     */
    suspend fun saveCallMessage(
        conversationId: String,
        senderId: Long,
        duration: Long,
        status: String // "completed", "missed", "declined"
    ) {
        val msgId = messagesRef.child(conversationId).push().key ?: return
        val now = System.currentTimeMillis()
        
        val message = mapOf(
            "senderId" to senderId,
            "text" to if (status == "missed") "Missed voice call" else "Voice call",
            "type" to "call",
            "timestamp" to now,
            "isRead" to false,
            "callDuration" to duration,
            "callStatus" to status
        )
        
        val updates = mapOf(
            "/messages/$conversationId/$msgId" to message,
            "/conversations/$conversationId/lastMessage" to if (status == "missed") "Missed voice call" else "Voice call",
            "/conversations/$conversationId/lastMessageAt" to now
        )
        
        database.reference.updateChildren(updates).await()
    }
    
    /**
     * Send call invitation notification
     */
    suspend fun sendCallInvitation(
        recipientId: Long,
        conversationId: String,
        callId: String,
        senderName: String
    ) {
        android.util.Log.d("ChatRepository", "=== SENDING CALL INVITATION ===")
        android.util.Log.d("ChatRepository", "To: $recipientId, CallId: $callId, Sender: $senderName")
        
        try {
            val notificationRef = database.getReference("notifications")
                .child(recipientId.toString())
                .push()
            
            val notification = mapOf(
                "title" to senderName,
                "body" to "Incoming voice call",
                "conversationId" to conversationId,
                "type" to "call_invitation",
                "callId" to callId,
                "senderName" to senderName,
                "timestamp" to System.currentTimeMillis()
            )
            
            android.util.Log.d("ChatRepository", "Writing to: ${notificationRef.path}")
            notificationRef.setValue(notification).await()
            android.util.Log.d("ChatRepository", "✓ Call invitation written to Firebase successfully!")
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "✗ Failed to send call invitation: ${e.message}", e)
        }
    }
    
    /**
     * Send call cancellation notification when caller ends call before receiver answers
     */
    suspend fun sendCallCancellation(
        recipientId: Long,
        conversationId: String,
        callId: String
    ) {
        android.util.Log.d("ChatRepository", "=== SENDING CALL CANCELLATION ===")
        android.util.Log.d("ChatRepository", "To: $recipientId, CallId: $callId")
        
        try {
            // Update call status in Firebase
            if (conversationId.isNotEmpty()) {
                val callRef = database.getReference("calls")
                    .child(conversationId)
                    .child(callId)
                
                callRef.child("status").setValue("cancelled")
                callRef.child("endedAt").setValue(System.currentTimeMillis())
            }
            
            // Send cancellation notification to dismiss the incoming call notification
            val notificationRef = database.getReference("notifications")
                .child(recipientId.toString())
                .push()
            
            val notification = mapOf(
                "title" to "Call Cancelled",
                "body" to "Call was cancelled",
                "conversationId" to conversationId,
                "type" to "call_cancelled",
                "callId" to callId,
                "timestamp" to System.currentTimeMillis()
            )
            
            android.util.Log.d("ChatRepository", "Writing cancellation to: ${notificationRef.path}")
            notificationRef.setValue(notification).await()
            android.util.Log.d("ChatRepository", "✓ Call cancellation written to Firebase successfully!")
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "✗ Failed to send call cancellation: ${e.message}", e)
        }
    }

    
    /**
     * Mark all messages from other user as read
     */
    suspend fun markMessagesAsRead(conversationId: String, currentUserId: Long) {
        android.util.Log.d("ChatRepository", "markMessagesAsRead called for $conversationId, userId=$currentUserId")
        
        val msgs = messagesRef.child(conversationId).get().await()
        
        val updates = mutableMapOf<String, Any>()
        msgs.children.forEach { msgSnapshot ->
            val senderId = msgSnapshot.child("senderId").getValue(Long::class.java)
            val isRead = msgSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
            
            android.util.Log.d("ChatRepository", "Message ${msgSnapshot.key}: senderId=$senderId, isRead=$isRead")
            
            if (senderId != currentUserId && !isRead) {
                updates["/messages/$conversationId/${msgSnapshot.key}/isRead"] = true
            }
        }
        
        android.util.Log.d("ChatRepository", "Updating ${updates.size} messages to read")
        
        if (updates.isNotEmpty()) {
            database.reference.updateChildren(updates).await()
            android.util.Log.d("ChatRepository", "Messages marked as read successfully")
        }
    }
    
    /**
     * Get total unread count for user
     */
    fun getTotalUnreadCount(userId: Long): Flow<Int> = callbackFlow {
        val userConvsRef = userConversationsRef.child(userId.toString())
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversationIds = snapshot.children.mapNotNull { it.key }
                
                if (conversationIds.isEmpty()) {
                    trySend(0)
                    return
                }
                
                var totalUnread = 0
                var fetchedCount = 0
                
                conversationIds.forEach { convId ->
                    messagesRef.child(convId).orderByChild("isRead").equalTo(false)
                        .get().addOnSuccessListener { msgSnapshot ->
                            totalUnread += msgSnapshot.children.count { msg ->
                                msg.child("senderId").getValue(Long::class.java) != userId
                            }
                            fetchedCount++
                            
                            if (fetchedCount == conversationIds.size) {
                                trySend(totalUnread)
                            }
                        }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        userConvsRef.addValueEventListener(listener)
        awaitClose { userConvsRef.removeEventListener(listener) }
    }
}
