package com.hellohingoli.smsgateway.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.UUID

/**
 * Manages device settings and preferences
 */
class DeviceSettings(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("gateway_settings", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_ACTIVE_SIM = "active_sim"
        private const val KEY_SMS_TEMPLATE = "sms_template"
        private const val KEY_SIM1_PHONE = "sim1_phone"
        private const val KEY_SIM2_PHONE = "sim2_phone"
        private const val KEY_API_URL = "api_url"
        
        // SMS template must start with <#> and end with app hash for auto-detection
        const val DEFAULT_TEMPLATE = "<#> Your HelloHingoli OTP is {otp}. Do not share.\nTQp93m8T4ZW"
        const val DEFAULT_API_URL = "https://hellohingoli.com/api/gateway.php"
    }
    
    // Device ID - unique per installation
    var deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: generateDeviceId()
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()
    
    // Device name for identification
    var deviceName: String
        get() = prefs.getString(KEY_DEVICE_NAME, null) ?: "${Build.MANUFACTURER} ${Build.MODEL}"
        set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value).apply()
    
    // Active SIM (1 or 2)
    var activeSim: Int
        get() = prefs.getInt(KEY_ACTIVE_SIM, 1)
        set(value) = prefs.edit().putInt(KEY_ACTIVE_SIM, value).apply()
    
    // SMS template with {otp} placeholder
    var smsTemplate: String
        get() = prefs.getString(KEY_SMS_TEMPLATE, DEFAULT_TEMPLATE) ?: DEFAULT_TEMPLATE
        set(value) = prefs.edit().putString(KEY_SMS_TEMPLATE, value).apply()
    
    // SIM 1 phone number
    var sim1Phone: String?
        get() = prefs.getString(KEY_SIM1_PHONE, null)
        set(value) = prefs.edit().putString(KEY_SIM1_PHONE, value).apply()
    
    // SIM 2 phone number
    var sim2Phone: String?
        get() = prefs.getString(KEY_SIM2_PHONE, null)
        set(value) = prefs.edit().putString(KEY_SIM2_PHONE, value).apply()
    
    // API URL
    var apiUrl: String
        get() = prefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
        set(value) = prefs.edit().putString(KEY_API_URL, value).apply()
    
    private fun generateDeviceId(): String {
        val id = "device_${UUID.randomUUID().toString().take(8)}"
        deviceId = id
        return id
    }
    
    fun getSenderPhone(): String? {
        return if (activeSim == 1) sim1Phone else sim2Phone
    }
}
