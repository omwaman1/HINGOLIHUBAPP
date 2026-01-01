package com.hellohingoli.smsgateway

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hellohingoli.smsgateway.api.GatewayApiClient
import com.hellohingoli.smsgateway.data.DeviceSettings
import com.hellohingoli.smsgateway.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settings: DeviceSettings
    private lateinit var apiClient: GatewayApiClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
        }
        
        settings = DeviceSettings(this)
        apiClient = GatewayApiClient(settings.apiUrl)
        
        loadSettings()
        setupListeners()
    }
    
    private fun loadSettings() {
        binding.apply {
            tvDeviceId.text = "Device ID: ${settings.deviceId}"
            etDeviceName.setText(settings.deviceName)
            etSim1Phone.setText(settings.sim1Phone ?: "")
            etSim2Phone.setText(settings.sim2Phone ?: "")
            etSmsTemplate.setText(settings.smsTemplate)
            
            if (settings.activeSim == 1) {
                rbSim1.isChecked = true
            } else {
                rbSim2.isChecked = true
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnResetTemplate.setOnClickListener {
            binding.etSmsTemplate.setText(DeviceSettings.DEFAULT_TEMPLATE)
        }
        
        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun saveSettings() {
        val deviceName = binding.etDeviceName.text.toString().trim()
        val sim1Phone = binding.etSim1Phone.text.toString().trim().takeIf { it.isNotEmpty() }
        val sim2Phone = binding.etSim2Phone.text.toString().trim().takeIf { it.isNotEmpty() }
        val smsTemplate = binding.etSmsTemplate.text.toString().trim()
        val activeSim = if (binding.rbSim1.isChecked) 1 else 2
        
        // Validate template
        if (!smsTemplate.contains("{otp}")) {
            Toast.makeText(this, "Template must contain {otp} placeholder", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save locally
        settings.deviceName = deviceName
        settings.sim1Phone = sim1Phone
        settings.sim2Phone = sim2Phone
        settings.smsTemplate = smsTemplate
        settings.activeSim = activeSim
        
        // Sync to server
        lifecycleScope.launch {
            binding.btnSave.isEnabled = false
            binding.btnSave.text = "Saving..."
            
            val success = apiClient.updateSettings(
                deviceId = settings.deviceId,
                deviceName = deviceName,
                sim1Phone = sim1Phone,
                sim2Phone = sim2Phone,
                smsTemplate = smsTemplate,
                activeSim = activeSim
            )
            
            runOnUiThread {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Settings"
                
                if (success) {
                    Toast.makeText(this@SettingsActivity, "Settings saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@SettingsActivity, "Saved locally (server sync failed)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
