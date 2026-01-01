package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

/**
 * App configuration response for version checking and force update
 */
data class AppConfigResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: AppConfig?
)

data class AppConfig(
    @SerializedName("min_version")
    val minVersion: String,
    
    @SerializedName("latest_version")
    val latestVersion: String,
    
    @SerializedName("force_update")
    val forceUpdate: Boolean,
    
    @SerializedName("update_required")
    val updateRequired: Boolean,
    
    @SerializedName("update_message")
    val updateMessage: String,
    
    @SerializedName("update_message_mr")
    val updateMessageMr: String,
    
    @SerializedName("play_store_url")
    val playStoreUrl: String,
    
    // Call timing configuration (admin-controlled)
    @SerializedName("call_timing_enabled")
    val callTimingEnabled: Boolean = true,
    
    @SerializedName("call_start_hour")
    val callStartHour: Int = 8,  // 8 AM
    
    @SerializedName("call_end_hour")
    val callEndHour: Int = 22,   // 10 PM
    
    @SerializedName("call_timing_message")
    val callTimingMessage: String = "Call service available from 8 AM to 10 PM",
    
    @SerializedName("call_timing_message_mr")
    val callTimingMessageMr: String = "कॉल सेवा सकाळी 8 ते रात्री 10 वाजेपर्यंत उपलब्ध"
)
