package com.hellohingoli.smsgateway.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * DAO for OTP history operations
 */
@Dao
interface OtpHistoryDao {
    
    @Insert
    suspend fun insert(history: OtpHistoryEntity): Long
    
    @Query("SELECT * FROM otp_history ORDER BY sentAt DESC LIMIT 100")
    suspend fun getRecentHistory(): List<OtpHistoryEntity>
    
    @Query("SELECT COUNT(*) FROM otp_history WHERE status = 'sent'")
    suspend fun getSentCount(): Int
    
    @Query("SELECT COUNT(*) FROM otp_history WHERE status = 'failed'")
    suspend fun getFailedCount(): Int
    
    @Query("DELETE FROM otp_history WHERE sentAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
