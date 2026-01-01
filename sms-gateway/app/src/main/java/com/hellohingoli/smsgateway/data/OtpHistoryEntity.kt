package com.hellohingoli.smsgateway.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a sent OTP message
 */
@Entity(tableName = "otp_history")
data class OtpHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phone: String,
    val otp: String,
    val status: String, // "sent", "failed", "pending"
    val sentAt: Long = System.currentTimeMillis(),
    val requestId: String? = null
)
