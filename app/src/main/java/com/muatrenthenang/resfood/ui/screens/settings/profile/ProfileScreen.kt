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
    
    // Validation function
    fun validateForm(): Boolean {
        return when {
            fullName.isBlank() -> {
                errorMessage = "Vui lòng nhập họ và tên"
                false
            }
            email.isBlank() -> {
                errorMessage = "Vui lòng nhập email"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                errorMessage = "Email không hợp lệ"
                false
            }
            phone.isNotBlank() && phone.length < 10 -> {
                errorMessage = "Số điện thoại không hợp lệ"
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
                        throw Exception("Không thể lưu ảnh đại diện")
                    }
                }
                
                // Update user profile
                val updatedUser = user.copy(
                    fullName = fullName.trim(),
                    phone = phone.trim().ifBlank { null },
                    email = email.trim(),
                    avatarUrl = avatarUrl
                )
                
                val result = userViewModel.updateUser(updatedUser)
                if (result.isFailure) {
                    throw result.exceptionOrNull() ?: Exception("Không thể cập nhật thông tin")
                }
                
                showSuccessDialog = true
                avatarUri = null // Reset avatar URI after successful upload
            } catch (e: Exception) {
                errorMessage = e.message ?: "Đã xảy ra lỗi khi lưu thông tin"
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
                    "Chỉnh sửa hồ sơ",
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
                        Text("Lưu", color = MaterialTheme.colorScheme.primary)
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
                // Show newly selected image, or existing avatar from local path, or placeholder
                val imageModel = when {
                    avatarUri != null -> avatarUri // Ảnh mới chọn (URI)
                    user.avatarUrl != null -> {
                        // Ảnh đã lưu (local path) - convert sang File để Coil load được
                        java.io.File(user.avatarUrl)
                    }
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
                "Nhấn để thay đổi ảnh đại diện",
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
                    "Thành viên ${user.rank}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thông tin cá nhân
            SectionTitle("Thông tin cá nhân")
            
            EditableProfileTextField(
                label = "Họ và tên",
                value = fullName,
                onValueChange = { fullName = it },
                icon = Icons.Default.Person,
                placeholder = "Nhập họ và tên"
            )
            
            EditableProfileTextField(
                label = "Số điện thoại",
                value = phone,
                onValueChange = { phone = it },
                icon = Icons.Default.Phone,
                placeholder = "Nhập số điện thoại",
                keyboardType = KeyboardType.Phone
            )
            
            EditableProfileTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                icon = Icons.Default.Email,
                placeholder = "Nhập email",
                keyboardType = KeyboardType.Email
            )
            
            // Address (read-only, navigate to address management)
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    "Địa chỉ giao hàng",
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
                        .clickable { onNavigateToAddresses() }
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
                        user.getDefaultAddress()?.getFullAddress() ?: "Chưa cập nhật",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cài đặt tài khoản
            SectionTitle("Cài đặt tài khoản")
            Column(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(12.dp)
                )
            ) {
                SettingRow(icon = Icons.Default.Lock, title = "Đổi mật khẩu", onClick = { })
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(icon = Icons.Default.ShoppingCart, title = "Ví Voucher", onClick = { })
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(icon = Icons.Default.LocationOn, title = "Quản lý địa chỉ", onClick = onNavigateToAddresses)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            OutlinedButton(
                onClick = { /* Logout Logic Here */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { /* Delete Account Logic */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xóa tài khoản")
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
            title = { Text("Thành công") },
            text = { Text("Thông tin cá nhân đã được cập nhật") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
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
            title = { Text("Lỗi") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
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