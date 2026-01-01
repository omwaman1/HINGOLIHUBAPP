package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

data class Reel(
    @SerializedName("reel_id") val reelId: Int,
    @SerializedName("instagram_url") val instagramUrl: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("is_liked") val isLiked: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ReelsData(
    @SerializedName("reels") val reels: List<Reel>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("has_more") val hasMore: Boolean
)

data class ReelsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ReelsData? = null
)

// Request/Response for like/watched actions
data class ReelActionRequest(
    @SerializedName("reel_id") val reelId: Int
)

data class LikeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: LikeData? = null
)

data class LikeData(
    @SerializedName("is_liked") val isLiked: Boolean,
    @SerializedName("likes_count") val likesCount: Int,
    @SerializedName("message") val message: String? = null
)
