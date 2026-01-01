package com.hellohingoli.smsgateway

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.hellohingoli.smsgateway.adapter.OtpHistoryAdapter
import com.hellohingoli.smsgateway.data.AppDatabase
import com.hellohingoli.smsgateway.databinding.ActivityMainBinding
import com.hellohingoli.smsgateway.service.SmsSenderService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var historyAdapter: OtpHistoryAdapter
    private lateinit var database: AppDatabase
    
    private val smsSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val phone = intent?.getStringExtra("phone") ?: "unknown"
            val status = intent?.getStringExtra("status") ?: "unknown"
            Toast.makeText(this@MainActivity, "SMS $status to $phone", Toast.LENGTH_SHORT).show()
            loadHistory()
        }
    }
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = AppDatabase.getInstance(this)
        
        setupRecyclerView()
        setupUI()
        checkPermissions()
        registerGatewayDevice()
        SmsSenderService.start(this)
        checkBatteryOptimization()
        loadHistory()
    }
    
    private fun setupRecyclerView() {
        historyAdapter = OtpHistoryAdapter()
        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }
    
    private fun setupUI() {
        binding.btnRefresh.setOnClickListener {
            registerGatewayDevice()
            loadHistory()
        }
        
        binding.btnClearLog.setOnClickListener {
            loadHistory()
        }
        
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun loadHistory() {
        lifecycleScope.launch {
            val history = database.otpHistoryDao().getRecentHistory()
            val sentCount = database.otpHistoryDao().getSentCount()
            val failedCount = database.otpHistoryDao().getFailedCount()
            
            runOnUiThread {
                historyAdapter.submitList(history)
                binding.tvStats.text = "✅ Sent: $sentCount | ❌ Failed: $failedCount"
                
                // Show/hide empty state
                if (history.isEmpty()) {
                    binding.rvHistory.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvHistory.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                }
            }
        }
    }
    
    private fun checkPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toMutableList()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            updateStatus("Ready")
        }
    }
    
    @SuppressLint("BatteryLife")
    private fun checkBatteryOptimization() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            AlertDialog.Builder(this)
                .setTitle("Battery Optimization")
                .setMessage("For reliable SMS delivery, please disable battery optimization.")
                .setPositiveButton("Disable") { _, _ ->
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                updateStatus("Ready")
            } else {
                updateStatus("Permission Required")
                Toast.makeText(this, "SMS permission is required", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun registerGatewayDevice() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                
                FirebaseDatabase.getInstance()
                    .getReference("gateway_devices/primary")
                    .setValue(mapOf(
                        "token" to token,
                        "updatedAt" to System.currentTimeMillis(),
                        "status" to "online",
                        "deviceModel" to Build.MODEL,
                        "androidVersion" to Build.VERSION.SDK_INT
                    ))
                    .addOnSuccessListener {
                        updateStatus("Online")
                        binding.tvToken.text = "Token: ${token.take(30)}..."
                    }
                    .addOnFailureListener { e ->
                        updateStatus("Error: ${e.message}")
                    }
            } else {
                updateStatus("FCM Error")
            }
        }
    }
    
    private fun updateStatus(status: String) {
        binding.tvStatus.text = "Status: $status"
    }
    
    override fun onResume() {
        super.onResume()
        loadHistory()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                smsSentReceiver, 
                IntentFilter("com.hellohingoli.smsgateway.SMS_SENT"),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(smsSentReceiver, IntentFilter("com.hellohingoli.smsgateway.SMS_SENT"))
        }
    }
    
    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(smsSentReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }
}
