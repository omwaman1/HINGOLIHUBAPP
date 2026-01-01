package com.hellohingoli.smsgateway.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.hellohingoli.smsgateway.GatewayApp
import com.hellohingoli.smsgateway.MainActivity
import com.hellohingoli.smsgateway.R

/**
 * Foreground service to keep the gateway app running persistently.
 * Uses wake lock to prevent the device from sleeping.
 */
class SmsSenderService : Service() {
    
    companion object {
        private const val TAG = "SmsSenderService"
        private const val NOTIFICATION_ID = 1
        
        fun start(context: Context) {
            val intent = Intent(context, SmsSenderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var messageCount = 0
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        acquireWakeLock()
        updateGatewayStatus("online")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Return STICKY to restart service if killed
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed - will restart")
        releaseWakeLock()
        updateGatewayStatus("offline")
        
        // Restart service if destroyed
        val restartIntent = Intent(this, SmsSenderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed - restarting service")
        
        // Restart service when app is swiped from recents
        val restartIntent = Intent(this, SmsSenderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SMSGateway::WakeLock"
        ).apply {
            acquire()
        }
        Log.d(TAG, "Wake lock acquired")
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "Wake lock released")
            }
        }
        wakeLock = null
    }
    
    fun incrementMessageCount() {
        messageCount++
        updateNotification()
    }
    
    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val text = if (messageCount > 0) {
            "Sent $messageCount OTP messages"
        } else {
            "Ready to send OTP messages"
        }
        
        return NotificationCompat.Builder(this, GatewayApp.CHANNEL_ID)
            .setContentTitle("📱 SMS Gateway Active")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun updateGatewayStatus(status: String) {
        try {
            FirebaseDatabase.getInstance()
                .getReference("gateway_devices/primary/status")
                .setValue(status)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update status", e)
        }
    }
}
