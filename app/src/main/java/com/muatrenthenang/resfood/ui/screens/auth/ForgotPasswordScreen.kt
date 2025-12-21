package com.muatrenthenang.resfood.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.components.ResFoodButton
import com.muatrenthenang.resfood.ui.components.ResFoodTextField
import com.muatrenthenang.resfood.ui.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: ForgotPasswordViewModel = viewModel()
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.resetResult.collectAsState()

    var email by remember { mutableStateOf("") }

    // Xử lý kết quả
    LaunchedEffect(result) {
        when (result) {
            "Success" -> {
                Toast.makeText(context, "Đã gửi email! Vui lòng kiểm tra hòm thư.", Toast.LENGTH_LONG).show()
                viewModel.resetState()
                onNavigateBack() // Quay về đăng nhập sau khi gửi xong
            }
            null -> {}
            else -> Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Nút Back đơn giản như thiết kế
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .background(Color.Transparent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Hero Icon Section (Ổ khóa phát sáng) ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                // Vòng sáng mờ bên ngoài (Glow effect giả lập)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                )
                // Vòng tròn chứa Icon bên trong
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        // Giả lập màu surface-dark/border của bạn bằng màu primary rất nhạt hoặc gray
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // --- Text Content ---
            Text(
                text = "Quên mật khẩu?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Đừng lo lắng! Hãy nhập email đăng ký của bạn, chúng tôi sẽ gửi mã xác minh ngay.",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Form ---
            ResFoodTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = Icons.Outlined.Mail,
                placeholder = "nguyenvan.a@gmail.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(24.dp))

            ResFoodButton(
                text = "Gửi mã",
                onClick = { viewModel.sendResetEmail(email) },
                isLoading = isLoading
            )

            // --- Footer ---
            Spacer(modifier = Modifier.weight(1f)) // Đẩy footer xuống dưới cùng nếu màn hình dài
            Row(
                modifier = Modifier.padding(bottom = 32.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bạn đã nhớ mật khẩu? ", color = Color.Gray)
                Text(
                    text = "Đăng nhập",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }
        }
    }
}