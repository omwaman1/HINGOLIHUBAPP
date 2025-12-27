package com.hingoli.hub.ui.profile

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hingoli.hub.ui.components.ErrorView
import com.hingoli.hub.ui.components.LoadingView
import com.hingoli.hub.ui.theme.PrimaryBlue
import com.hingoli.hub.ui.theme.Strings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    pendingAction: String? = null,
    listingId: Long? = null,
    onProfileCompleted: ((String, Long) -> Unit)? = null,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Helper: detect auto-generated email
    fun isSystemGeneratedEmail(email: String?) = email?.endsWith("@temp.hellohingoli.com") == true
    
    // Helper: detect auto-generated username (e.g., "User1234_ab3f")
    fun isSystemGeneratedUsername(username: String?) = 
        username?.matches(Regex("^User\\d{4}_[a-f0-9]+$")) == true
    
    // Form state - filter out system-generated values
    var username by remember(uiState.profile) { 
        mutableStateOf(uiState.profile?.username?.takeUnless { isSystemGeneratedUsername(it) } ?: "")
    }
    var email by remember(uiState.profile) { 
        mutableStateOf(uiState.profile?.email?.takeUnless { isSystemGeneratedEmail(it) } ?: "")
    }
    var gender by remember(uiState.profile) { mutableStateOf(uiState.profile?.gender ?: "") }
    var dateOfBirth by remember(uiState.profile) { mutableStateOf(uiState.profile?.dateOfBirth ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var genderDropdownExpanded by remember { mutableStateOf(false) }
    
    // Profile completion required for Call/Chat actions
    val isPendingAction = !pendingAction.isNullOrBlank() && listingId != null && listingId > 0L
    
    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, Strings.profileUpdatedSuccess(uiState.isMarathi), Toast.LENGTH_SHORT).show()
            viewModel.clearSaveSuccess()
            if (isPendingAction && onProfileCompleted != null) {
                onProfileCompleted(pendingAction!!, listingId!!)
            } else {
                onBackClick()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.editProfile(uiState.isMarathi), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.error != null && uiState.profile == null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadProfile() }
                )
                uiState.profile != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Show mandatory fields message when coming from Call/Chat
                        if (isPendingAction) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3CD) // Warning yellow
                                )
                            ) {
                                Text(
                                    text = "⚠️ कृपया कॉल किंवा चॅट करण्यापूर्वी सर्व माहिती भरा\n(Please fill all details to proceed)",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF856404)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Avatar
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.profile?.avatarUrl != null) {
                                AsyncImage(
                                    model = uiState.profile?.avatarUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = coil.compose.rememberAsyncImagePainter(
                                        model = null
                                    ),
                                    error = coil.compose.rememberAsyncImagePainter(
                                        model = null
                                    )
                                )
                            }
                            // Always show Person icon as fallback/placeholder
                            if (uiState.profile?.avatarUrl == null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E7FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User placeholder",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Name field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("${Strings.name(uiState.isMarathi)} *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mobile Number (read-only)
                        OutlinedTextField(
                            value = uiState.profile?.phone ?: "",
                            onValueChange = { },
                            label = { Text(Strings.mobileNumber(uiState.isMarathi)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (isPendingAction) "${Strings.email(uiState.isMarathi)} *" else Strings.email(uiState.isMarathi)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Gender and Date of Birth in one row
                        val calendar = Calendar.getInstance()
                        if (dateOfBirth.isNotEmpty()) {
                            try {
                                val parts = dateOfBirth.split("-")
                                if (parts.size == 3) {
                                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                                }
                            } catch (e: Exception) { }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Gender dropdown
                            ExposedDropdownMenuBox(
                                expanded = genderDropdownExpanded,
                                onExpandedChange = { genderDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = when (gender) {
                                        "male" -> Strings.male(uiState.isMarathi)
                                        "female" -> Strings.female(uiState.isMarathi)
                                        "other" -> Strings.other(uiState.isMarathi)
                                        else -> ""
                                    },
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text(if (isPendingAction) "${Strings.gender(uiState.isMarathi)} *" else Strings.gender(uiState.isMarathi)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = genderDropdownExpanded,
                                    onDismissRequest = { genderDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(Strings.male(uiState.isMarathi)) },
                                        onClick = {
                                            gender = "male"
                                            genderDropdownExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(Strings.female(uiState.isMarathi)) },
                                        onClick = {
                                            gender = "female"
                                            genderDropdownExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(Strings.other(uiState.isMarathi)) },
                                        onClick = {
                                            gender = "other"
                                            genderDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                            
                            // Date of Birth picker
                            OutlinedTextField(
                                value = if (dateOfBirth.isEmpty()) "" else dateOfBirth,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text(if (isPendingAction) "${Strings.dob(uiState.isMarathi)} *" else Strings.dob(uiState.isMarathi)) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = "Select date",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, day ->
                                                dateOfBirth = String.format("%04d-%02d-%02d", year, month + 1, day)
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Password field (always visible)
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("${Strings.password(uiState.isMarathi)} *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(if (isPendingAction) "${Strings.confirmPassword(uiState.isMarathi)} *" else Strings.confirmPassword(uiState.isMarathi)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                            supportingText = if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                { Text(Strings.passwordsDontMatch(uiState.isMarathi), color = MaterialTheme.colorScheme.error) }
                            } else null
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Error message
                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        // Validation for pending action
                        val isFormValid = if (isPendingAction) {
                            username.isNotBlank() && 
                            email.isNotBlank() && 
                            gender.isNotBlank() && 
                            dateOfBirth.isNotBlank() && 
                            password.isNotBlank() && 
                            password == confirmPassword &&
                            password.length >= 6
                        } else {
                            username.isNotBlank() &&
                            password.isNotBlank() && 
                            password == confirmPassword &&
                            password.length >= 6
                        }
                        
                        // Save button
                        Button(
                            onClick = {
                                viewModel.updateProfile(
                                    username = username,
                                    email = email.takeIf { it.isNotBlank() },
                                    gender = gender.takeIf { it.isNotBlank() },
                                    dateOfBirth = dateOfBirth.takeIf { it.isNotBlank() },
                                    password = password.takeIf { it.isNotBlank() }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            enabled = !uiState.isSaving && isFormValid
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (isPendingAction) Strings.saveAndContinue(uiState.isMarathi) else Strings.saveChanges(uiState.isMarathi), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
