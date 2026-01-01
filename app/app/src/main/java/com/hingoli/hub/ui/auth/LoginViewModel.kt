package com.hingoli.hub.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.repository.AuthRepository
import com.hingoli.hub.data.repository.AuthResult
import com.hingoli.hub.data.repository.OtpResult
import com.hingoli.hub.util.SmsRetrieverHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthStep {
    PHONE_INPUT,      // Enter phone number
    PASSWORD_INPUT,   // Enter password (existing users)
    OTP_INPUT,        // Verify OTP to login (both new and existing users)
    FORGOT_PASSWORD,  // Forgot password - enter OTP
    RESET_PASSWORD    // Enter new password
}

data class LoginUiState(
    val phone: String = "",
    val password: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentStep: AuthStep = AuthStep.PHONE_INPUT,
    val username: String? = null,
    val resetToken: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isExistingUser: Boolean = false, // Track if phone number belongs to existing user
    val otpExpiresIn: Int = 300, // 5 minutes in seconds
    // Signup form fields
    val signupName: String = "",
    val signupEmail: String = "",
    val signupPassword: String = "",
    val signupConfirmPassword: String = "",
    val signupGender: String? = null,
    val signupDateOfBirth: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val smsRetrieverHelper: SmsRetrieverHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    override fun onCleared() {
        super.onCleared()
        smsRetrieverHelper.stopListening()
    }
    
    /**
     * Start SMS Retriever for auto OTP detection
     */
    private fun startSmsRetriever() {
        smsRetrieverHelper.startListening { otp ->
            Log.d("LoginViewModel", "Auto OTP received: $otp")
            _uiState.value = _uiState.value.copy(otp = otp)
        }
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)
        }
    }
    
    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, error = null)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }
    
    fun updateOtp(otp: String) {
        // Only allow 6 digits
        if (otp.length <= 6 && otp.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(otp = otp, error = null)
        }
    }
    
    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password, error = null)
    }
    
    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password, error = null)
    }
    
    // Signup form update functions
    fun updateSignupName(name: String) {
        _uiState.value = _uiState.value.copy(signupName = name, error = null)
    }
    
    fun updateSignupEmail(email: String) {
        _uiState.value = _uiState.value.copy(signupEmail = email, error = null)
    }
    
    fun updateSignupPassword(password: String) {
        _uiState.value = _uiState.value.copy(signupPassword = password, error = null)
    }
    
    fun updateSignupConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(signupConfirmPassword = password, error = null)
    }
    
    fun updateSignupGender(gender: String?) {
        _uiState.value = _uiState.value.copy(signupGender = gender, error = null)
    }
    
    fun updateSignupDateOfBirth(dob: String?) {
        _uiState.value = _uiState.value.copy(signupDateOfBirth = dob, error = null)
    }
    
    /**
     * Step 1: Check if phone exists and proceed accordingly
     */
    fun continueWithPhone() {
        val currentState = _uiState.value
        
        if (currentState.phone.isBlank() || currentState.phone.length < 10) {
            _uiState.value = currentState.copy(error = "Please enter a valid phone number")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            val result = authRepository.checkPhone(currentState.phone)
            
            result.fold(
                onSuccess = { data ->
                    if (data.exists) {
                        // Existing user - show password login screen
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentStep = AuthStep.PASSWORD_INPUT,
                            username = data.username,
                            isExistingUser = true
                        )
                    } else {
                        // New user - send OTP and show signup form
                        _uiState.value = _uiState.value.copy(isExistingUser = false)
                        sendOtpForSignup()
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to check phone"
                    )
                }
            )
        }
    }
    
    /**
     * Login with OTP instead of password (for existing users)
     */
    fun loginWithOtp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.sendOtp(_uiState.value.phone, "login")
            
            result.fold(
                onSuccess = { data ->
                    startSmsRetriever()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = AuthStep.OTP_INPUT,
                        otpExpiresIn = data.expiresIn,
                        otp = ""
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send OTP"
                    )
                }
            )
        }
    }
    
    /**
     * Send OTP for existing user login
     */
    private fun sendOtpForLogin() {
        viewModelScope.launch {
            val result = authRepository.sendOtp(_uiState.value.phone, "login")
            
            result.fold(
                onSuccess = { data ->
                    // Start SMS Retriever for auto OTP detection
                    startSmsRetriever()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = AuthStep.OTP_INPUT,
                        otpExpiresIn = data.expiresIn
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send OTP"
                    )
                }
            )
        }
    }
    
    /**
     * Send OTP for new user signup - goes directly to OTP input
     * API will auto-create user account on successful OTP verification
     */
    private fun sendOtpForSignup() {
        viewModelScope.launch {
            val result = authRepository.sendOtp(_uiState.value.phone, "signup")
            
            result.fold(
                onSuccess = { data ->
                    // Start SMS Retriever for auto OTP detection
                    startSmsRetriever()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = AuthStep.OTP_INPUT,
                        otpExpiresIn = data.expiresIn
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send OTP"
                    )
                }
            )
        }
    }
    
    /**
     * Login with password (existing user)
     */
    fun loginWithPassword() {
        val currentState = _uiState.value
        
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "Please enter your password")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            when (val result = authRepository.login(currentState.phone, currentState.password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    /**
     * Verify OTP (for forgot password only)
     */
    fun verifyOtp() {
        val currentState = _uiState.value
        
        if (currentState.otp.length != 6) {
            _uiState.value = currentState.copy(error = "Please enter 6-digit OTP")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            when (val result = authRepository.verifyOtp(currentState.phone, currentState.otp)) {
                is OtpResult.LoginSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is OtpResult.ResetTokenReceived -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = AuthStep.RESET_PASSWORD,
                        resetToken = result.resetToken
                    )
                }
                is OtpResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    /**
     * Complete signup with OTP and user details
     */
    fun signupWithOtp() {
        val currentState = _uiState.value
        
        // Validation
        if (currentState.signupName.isBlank()) {
            _uiState.value = currentState.copy(error = "Please enter your name")
            return
        }
        
        if (currentState.otp.length != 6) {
            _uiState.value = currentState.copy(error = "Please enter 6-digit OTP")
            return
        }
        
        if (currentState.signupPassword.length < 6) {
            _uiState.value = currentState.copy(error = "Password must be at least 6 characters")
            return
        }
        
        if (currentState.signupPassword != currentState.signupConfirmPassword) {
            _uiState.value = currentState.copy(error = "Passwords do not match")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            val result = authRepository.signupWithOtp(
                phone = currentState.phone,
                otp = currentState.otp,
                username = currentState.signupName,
                email = currentState.signupEmail.ifBlank { null },
                password = currentState.signupPassword,
                gender = currentState.signupGender,
                dateOfBirth = currentState.signupDateOfBirth
            )
            
            when (result) {
                is OtpResult.LoginSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is OtpResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Unexpected error occurred"
                    )
                }
            }
        }
    }
    
    /**
     * Initiate forgot password flow
     */
    fun forgotPassword() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.sendOtp(_uiState.value.phone, "reset_password")
            
            result.fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = AuthStep.FORGOT_PASSWORD,
                        otp = "",
                        otpExpiresIn = data.expiresIn
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send OTP"
                    )
                }
            )
        }
    }
    
    /**
     * Reset password after OTP verification
     */
    fun resetPassword() {
        val currentState = _uiState.value
        
        if (currentState.newPassword.length < 6) {
            _uiState.value = currentState.copy(error = "Password must be at least 6 characters")
            return
        }
        
        if (currentState.newPassword != currentState.confirmPassword) {
            _uiState.value = currentState.copy(error = "Passwords do not match")
            return
        }
        
        val resetToken = currentState.resetToken
        if (resetToken == null) {
            _uiState.value = currentState.copy(error = "Invalid reset token")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            val result = authRepository.resetPassword(
                currentState.phone,
                resetToken,
                currentState.newPassword
            )
            
            result.fold(
                onSuccess = {
                    // Password reset successful - now auto-login with the new password
                    val loginResult = authRepository.login(currentState.phone, currentState.newPassword)
                    
                    when (loginResult) {
                        is AuthResult.Success -> {
                            // Auto-login successful
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                newPassword = "",
                                confirmPassword = "",
                                resetToken = null,
                                otp = "",
                                password = "",
                                error = null
                            )
                        }
                        is AuthResult.Error -> {
                            // Login failed - fallback to password input screen
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentStep = AuthStep.PASSWORD_INPUT,
                                newPassword = "",
                                confirmPassword = "",
                                resetToken = null,
                                otp = "",
                                password = "",
                                error = "Password reset successful. Please login with your new password."
                            )
                        }
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Password reset failed"
                    )
                }
            )
        }
    }
    
    /**
     * Resend OTP
     */
    fun resendOtp() {
        val purpose = when (_uiState.value.currentStep) {
            AuthStep.FORGOT_PASSWORD -> "reset_password"
            else -> "signup"
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, otp = "")
            
            val result = authRepository.sendOtp(_uiState.value.phone, purpose)
            
            result.fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otpExpiresIn = data.expiresIn,
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to resend OTP"
                    )
                }
            )
        }
    }
    
    /**
     * Go back to previous step
     */
    fun goBack() {
        val currentState = _uiState.value
        val previousStep = when (currentState.currentStep) {
            AuthStep.PASSWORD_INPUT -> AuthStep.PHONE_INPUT
            AuthStep.OTP_INPUT -> {
                // Go back to password for existing users, phone for new users
                if (currentState.isExistingUser) AuthStep.PASSWORD_INPUT else AuthStep.PHONE_INPUT
            }
            AuthStep.FORGOT_PASSWORD -> AuthStep.PASSWORD_INPUT
            AuthStep.RESET_PASSWORD -> AuthStep.FORGOT_PASSWORD
            else -> AuthStep.PHONE_INPUT
        }
        
        _uiState.value = currentState.copy(
            currentStep = previousStep,
            error = null,
            otp = "",
            password = if (previousStep == AuthStep.PHONE_INPUT) "" else currentState.password
        )
    }
}
