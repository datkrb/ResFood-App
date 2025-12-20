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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.ui.components.ResFoodButton
import com.muatrenthenang.resfood.ui.components.ResFoodPasswordField
import com.muatrenthenang.resfood.ui.components.ResFoodTextField
import com.muatrenthenang.resfood.ui.viewmodel.RegisterViewModel

// Màu sắc (Sau này nên đưa vào ui/theme/Color.kt)
val PrimaryColor = Color(0xFF339CFF)
val BgLight = Color(0xFFF5F7F8)
val TextDark = Color(0xFF0F1923)

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: RegisterViewModel = viewModel()
    val context = LocalContext.current

    // State dữ liệu
    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.registerResult.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }

    // State ẩn/hiện mật khẩu
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRePasswordVisible by remember { mutableStateOf(false) }

    // Xử lý kết quả đăng ký
    LaunchedEffect(result) {
        when (result) {
            "Success" -> {
                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onRegisterSuccess()
            }
            null -> {}
            else -> Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Đăng ký",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.align(Alignment.Center)
                )
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

            // --- Header Text ---
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tạo tài khoản mới",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Text(
                text = "Chào mừng bạn đến với ResFood",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- Form Inputs (Dùng Component chung) ---

            ResFoodTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Họ và tên",
                icon = Icons.Outlined.Person,
                placeholder = "Nguyễn Văn A"
            )

            ResFoodTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = Icons.Outlined.Mail,
                placeholder = "email@example.com",
                keyboardType = KeyboardType.Email
            )

            ResFoodPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                isVisible = isPasswordVisible,
                onToggleVisibility = { isPasswordVisible = !isPasswordVisible }
            )

            ResFoodPasswordField(
                value = rePassword,
                onValueChange = { rePassword = it },
                label = "Nhập lại mật khẩu",
                isVisible = isRePasswordVisible,
                onToggleVisibility = { isRePasswordVisible = !isRePasswordVisible }
            )

            // Forgot Password Link
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { /* TODO: Navigate to ForgotPass */ }) {
                    Text("Quên mật khẩu?", color = PrimaryColor, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Submit Button (Dùng Component chung) ---
            ResFoodButton(
                text = "Đăng ký ngay",
                onClick = { viewModel.register(email, password, rePassword, fullName) },
                isLoading = isLoading
            )

            // --- Divider ---
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                Text(
                    text = "Hoặc tiếp tục với",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            }

            // --- Social Login Buttons ---
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialButton(iconRes = R.drawable.ic_launcher_foreground){ /* Login Google */ }
                Spacer(modifier = Modifier.width(20.dp))
                SocialButton(iconRes = R.drawable.ic_launcher_foreground) { /* Login FB */ }
                Spacer(modifier = Modifier.width(20.dp))
                SocialButton(iconRes = null, isApple = true) { /* Login Apple */ }
            }

            // --- Footer ---
            Spacer(modifier = Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bạn đã có tài khoản? ", color = Color.Gray)
                Text(
                    text = "Đăng nhập",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Component SocialButton (Giữ lại đây hoặc chuyển sang file riêng cũng được)
@Composable
fun SocialButton(iconRes: Int?, isApple: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isApple) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Tạm thời dùng icon này
                    contentDescription = "Apple",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            } else if (iconRes != null) {
                // Nếu chưa có ảnh thì dùng icon tạm
                Icon(imageVector = Icons.Filled.Public, contentDescription = null, tint = Color.Gray)

                // Khi có ảnh thật thì mở dòng này ra:
                // Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}