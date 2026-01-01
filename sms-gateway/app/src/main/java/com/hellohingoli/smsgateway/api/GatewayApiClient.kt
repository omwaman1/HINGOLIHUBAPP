package com.hellohingoli.smsgateway.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * API client for gateway backend
 */
class GatewayApiClient(private val baseUrl: String) {
    
    companion object {
        private const val TAG = "GatewayApiClient"
    }
    
    suspend fun registerDevice(
        deviceId: String,
        deviceName: String,
        fcmToken: String,
        sim1Phone: String?,
        sim2Phone: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("device_id", deviceId)
                put("device_name", deviceName)
                put("fcm_token", fcmToken)
                put("sim1_phone", sim1Phone ?: JSONObject.NULL)
                put("sim2_phone", sim2Phone ?: JSONObject.NULL)
            }
            
            val response = post("$baseUrl?action=register", json.toString())
            response?.optBoolean("success", false) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register device", e)
            false
        }
    }
    
    suspend fun logOtp(
        deviceId: String,
        deviceName: String,
        senderPhone: String?,
        recipientPhone: String,
        otpCode: String,
        status: String,
        errorMessage: String? = null,
        requestId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("device_id", deviceId)
                put("device_name", deviceName)
                put("sender_phone", senderPhone ?: JSONObject.NULL)
                put("recipient_phone", recipientPhone)
                put("otp_code", otpCode)
                put("status", status)
                put("error_message", errorMessage ?: JSONObject.NULL)
                put("request_id", requestId ?: JSONObject.NULL)
            }
            
            val response = post("$baseUrl?action=log-otp", json.toString())
            response?.optBoolean("success", false) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log OTP", e)
            false
        }
    }
    
    suspend fun updateSettings(
        deviceId: String,
        activeSim: Int? = null,
        smsTemplate: String? = null,
        deviceName: String? = null,
        sim1Phone: String? = null,
        sim2Phone: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("device_id", deviceId)
                activeSim?.let { put("active_sim", it) }
                smsTemplate?.let { put("sms_template", it) }
                deviceName?.let { put("device_name", it) }
                sim1Phone?.let { put("sim1_phone", it) }
                sim2Phone?.let { put("sim2_phone", it) }
            }
            
            val response = post("$baseUrl?action=update-settings", json.toString())
            response?.optBoolean("success", false) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update settings", e)
            false
        }
    }
    
    private fun post(urlString: String, body: String): JSONObject? {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        return try {
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val responseText = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: ""
            }
            
            Log.d(TAG, "POST $urlString -> $responseCode: $responseText")
            JSONObject(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "POST failed: $urlString", e)
            null
        } finally {
            connection.disconnect()
        }
    }
}
