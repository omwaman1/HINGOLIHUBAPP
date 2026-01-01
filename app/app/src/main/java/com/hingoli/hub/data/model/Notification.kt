package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

data class NotificationItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("type") val type: String,
    @SerializedName("deep_link") val deepLink: String?,
    @SerializedName("listing_id") val listingId: Long?,
    @SerializedName("is_read") val isReadInt: Int,  // API returns 0/1
    @SerializedName("created_at") val createdAt: String
) {
    val isRead: Boolean get() = isReadInt == 1
}

data class NotificationHistoryData(
    @SerializedName("notifications") val notifications: List<NotificationItem>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("has_more") val hasMore: Boolean
)

data class NotificationHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: NotificationHistoryData?
)

data class UnreadCountData(
    @SerializedName("notifications") val notifications: Int,
    @SerializedName("chats") val chats: Int
)

data class UnreadCountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: UnreadCountData?
)

data class MarkReadRequest(
    @SerializedName("notification_ids") val notificationIds: List<Long>? = null
)
