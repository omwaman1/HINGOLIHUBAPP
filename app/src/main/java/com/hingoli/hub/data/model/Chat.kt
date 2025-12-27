package com.hingoli.hub.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Conversation(
    @get:PropertyName("conversationId") @set:PropertyName("conversationId")
    var conversationId: String = "",
    
    @get:PropertyName("participantIds") @set:PropertyName("participantIds")
    var participantIds: List<Long> = emptyList(),
    
    @get:PropertyName("listingId") @set:PropertyName("listingId")
    var listingId: Long = 0,
    
    @get:PropertyName("listingTitle") @set:PropertyName("listingTitle")
    var listingTitle: String = "",
    
    @get:PropertyName("listingImage") @set:PropertyName("listingImage")
    var listingImage: String? = null,
    
    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage")
    var lastMessage: String = "",
    
    @get:PropertyName("lastMessageAt") @set:PropertyName("lastMessageAt")
    var lastMessageAt: Long = 0,
    
    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = 0,
    
    // Client-side computed fields
    var otherUserName: String = "",
    var otherUserAvatar: String? = null,
    var unreadCount: Int = 0
)

@IgnoreExtraProperties
data class ChatMessage(
    @get:PropertyName("messageId") @set:PropertyName("messageId")
    var messageId: String = "",
    
    @get:PropertyName("senderId") @set:PropertyName("senderId")
    var senderId: Long = 0,
    
    @get:PropertyName("text") @set:PropertyName("text")
    var text: String = "",
    
    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "text", // "text" or "listing_card"
    
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = 0,
    
    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false,
    
    @get:PropertyName("callDuration") @set:PropertyName("callDuration")
    var callDuration: Long = 0, // in seconds
    
    @get:PropertyName("callStatus") @set:PropertyName("callStatus")
    var callStatus: String = "" // "completed", "missed", "declined"
)

// For creating a new conversation
data class CreateConversationRequest(
    val otherUserId: Long,
    val listingId: Long,
    val listingTitle: String,
    val listingImage: String?
)
