package com.muatrenthenang.resfood.ui.screens.settings.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToAddresses: () -> Unit = {},
    onLogout: () -> Unit = {},
    userViewModel: UserViewModel
) {
    val userState by userViewModel.userState.collectAsState()
    val user = userState ?: User()
    val context = LocalContext.current
    
    // Editable state variables
    var fullName by remember { mutableStateOf(user.fullName) }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var email by remember { mutableStateOf(user.email) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Change password dialog states
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { avatarUri = it }
    }
    
    // Update local state when user changes
    LaunchedEffect(user) {
        fullName = user.fullName
        phone = user.phone ?: ""
        email = user.email
    }
    
    // Lấy địa chỉ mặc định để hiển thị
    val defaultAddress = user.getDefaultAddress()
    
    // Validation function
    fun validateForm(): Boolean {
        return when {
            fullName.isBlank() -> {
                errorMessage = context.getString(R.string.err_empty_name)
                false
            }
            email.isBlank() -> {
                errorMessage = context.getString(R.string.err_empty_email)
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                errorMessage = context.getString(R.string.err_invalid_email)
                false
            }
            phone.isNotBlank() && phone.length < 10 -> {
                errorMessage = context.getString(R.string.err_invalid_phone)
                false
            }
            else -> true
        }
    }
    
    // Save function
    fun saveProfile() {
        if (!validateForm()) {
            showErrorDialog = true
            return
        }
        
        scope.launch {
            isLoading = true
            try {
                // Upload avatar first if changed
                var avatarUrl = user.avatarUrl
                avatarUri?.let { uri ->
                    val uploadResult = userViewModel.uploadAvatar(uri, context)
                    if (uploadResult.isSuccess) {
                        avatarUrl = uploadResult.getOrNull()
                    } else {
                        throw Exception(context.getString(R.string.err_upload_avatar))
                    }
                }
                
                val result = userViewModel.updateUser(
                    fullName = fullName.trim(),
                    phone = phone.trim().ifBlank { null },
                    email = email.trim()
                )
                if (result.isFailure) {
                    throw result.exceptionOrNull() ?: Exception(context.getString(R.string.err_update_info))
                }
                
                showSuccessDialog = true
                avatarUri = null // Reset avatar URI after successful upload
            } catch (e: Exception) {
                errorMessage = e.message ?: context.getString(R.string.err_save_info)
                showErrorDialog = true
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.profile_edit_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { saveProfile() },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.common_save), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
            ) {
                // Show newly selected image, or existing avatar URL, or placeholder
                val imageModel = when {
                    avatarUri != null -> avatarUri // Ảnh mới chọn (URI)
                    user.avatarUrl != null -> user.avatarUrl // URL từ ImgBB
                    else -> null
                }
                
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Change Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.profile_change_avatar_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    stringResource(R.string.profile_member_rank, user.rank),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thông tin cá nhân
            SectionTitle(stringResource(R.string.profile_info_section))
            
            EditableProfileTextField(
                label = stringResource(R.string.auth_full_name),
                value = fullName,
                onValueChange = { fullName = it },
                icon = Icons.Default.Person,
                placeholder = stringResource(R.string.auth_full_name_placeholder)
            )
            
            EditableProfileTextField(
                label = stringResource(R.string.profile_phone),
                value = phone,
                onValueChange = { phone = it },
                icon = Icons.Default.Phone,
                placeholder = stringResource(R.string.profile_phone_placeholder),
                keyboardType = KeyboardType.Phone
            )
            
            EditableProfileTextField(
                label = stringResource(R.string.auth_email),
                value = email,
                onValueChange = { email = it },
                icon = Icons.Default.Email,
                placeholder = stringResource(R.string.profile_enter_email_placeholder),
                keyboardType = KeyboardType.Email
            )
            
            // Address (read-only, navigate to address management)
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    stringResource(R.string.profile_address_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        defaultAddress?.getFullAddress() ?: stringResource(R.string.profile_address_not_updated),
                        color = if (defaultAddress != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cài đặt tài khoản
            SectionTitle(stringResource(R.string.profile_account_settings))
            Column(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(12.dp)
                )
            ) {
                SettingRow(icon = Icons.Default.Lock, title = stringResource(R.string.profile_change_password), onClick = { showChangePasswordDialog = true })
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(icon = Icons.Default.ShoppingCart, title = stringResource(R.string.profile_voucher_wallet), onClick = { })
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(icon = Icons.Default.LocationOn, title = stringResource(R.string.profile_manage_address), onClick = onNavigateToAddresses)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            OutlinedButton(
                onClick = {
                    // Logout with UserViewModel
                    userViewModel.logout {
                        onLogout()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_logout))
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_delete_account))
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(stringResource(R.string.common_success)) },
            text = { Text(stringResource(R.string.profile_update_success)) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(stringResource(R.string.common_error)) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            },
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(stringResource(R.string.profile_change_password)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text(stringResource(R.string.profile_current_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(stringResource(R.string.profile_new_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(R.string.profile_confirm_new_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Validation
                        when {
                            currentPassword.isBlank() -> {
                                errorMessage = context.getString(R.string.err_empty_current_pass)
                                showErrorDialog = true
                            }
                            newPassword.isBlank() -> {
                                errorMessage = context.getString(R.string.err_empty_new_pass)
                                showErrorDialog = true
                            }
                            newPassword.length < 6 -> {
                                errorMessage = context.getString(R.string.err_short_new_pass)
                                showErrorDialog = true
                            }
                            newPassword != confirmPassword -> {
                                errorMessage = context.getString(R.string.err_password_mismatch)
                                showErrorDialog = true
                            }
                            else -> {
                                // Change password
                                scope.launch {
                                    isLoading = true
                                    val result = userViewModel.changePassword(currentPassword, newPassword)
                                    isLoading = false
                                    
                                    if (result.isSuccess) {
                                        showChangePasswordDialog = false
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        errorMessage = context.getString(R.string.err_pass_change_success)
                                        showSuccessDialog = true
                                    } else {
                                        errorMessage = when {
                                            result.exceptionOrNull()?.message?.contains("password") == true -> 
                                                context.getString(R.string.err_incorrect_current_pass)
                                            else -> result.exceptionOrNull()?.message ?: context.getString(R.string.err_generic)
                                        }
                                        showErrorDialog = true
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.common_ok)) // Or "Xác nhận"
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showChangePasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
    // Delete Account Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(stringResource(R.string.profile_delete_confirm_title)) },
            text = { 
                Text(
                    stringResource(R.string.profile_delete_confirm_msg),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        userViewModel.deleteAccount(
                            onSuccess = {
                                onLogout() // Reuse logout navigation logic
                            },
                            onError = { msg ->
                                errorMessage = msg
                                showErrorDialog = true
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.profile_delete_confirm_btn), color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun EditableProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
fun SettingRow(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}