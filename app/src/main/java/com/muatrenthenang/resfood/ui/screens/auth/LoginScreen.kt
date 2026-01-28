    package com.muatrenthenang.resfood.ui.screens.auth

    import android.widget.Toast
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.RestaurantMenu
    import androidx.compose.material.icons.outlined.Mail
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.google.android.gms.auth.api.signin.GoogleSignIn
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
    import com.google.android.gms.common.api.ApiException
    import com.muatrenthenang.resfood.R
    import com.muatrenthenang.resfood.ui.components.ResFoodButton
    import com.muatrenthenang.resfood.ui.components.ResFoodPasswordField
    import com.muatrenthenang.resfood.ui.components.ResFoodTextField
    import com.muatrenthenang.resfood.ui.components.SocialButton // <-- Import Component chung
    import com.muatrenthenang.resfood.ui.viewmodel.auth.LoginViewModel

    @Composable
    fun LoginScreen(
        onLoginSuccess: (Boolean) -> Unit,
        onNavigateToRegister: () -> Unit,
        onNavigateToForgotPassword: () -> Unit
    ) {
        val viewModel: LoginViewModel = viewModel()
        val context = LocalContext.current

        // State dữ liệu
        val isLoading by viewModel.isLoading.collectAsState()
        val loginResult by viewModel.loginResult.collectAsState()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }

        // Xử lý kết quả đăng nhập
        LaunchedEffect(loginResult) {
            when (val result = loginResult) {
                is LoginViewModel.LoginState.Success -> {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    viewModel.resetLoginState()
                    onLoginSuccess(result.isAdmin)
                }
                is LoginViewModel.LoginState.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                null -> {}
            }
        }

        // --- CẤU HÌNH GOOGLE SIGN-IN ---
        // Lấy token mặc định (Android tự sinh ra string default_web_client_id từ file google-services.json)
        val token = stringResource(R.string.default_web_client_id)

        val gso = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(token) // Quan trọng: Yêu cầu trả về ID Token
                .requestEmail()
                .build()
        }

        val googleSignInClient = remember {
            GoogleSignIn.getClient(context, gso)
        }

        // Launcher để mở cửa sổ chọn tài khoản Google
        val googleAuthLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.loginWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Sign-In thất bại: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Giao diện chính ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Dùng màu nền từ Theme
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Background Decor (Hiệu ứng gradient mờ ở trên cùng)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                // Dùng màu Primary từ Theme với độ mờ
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 2. Nội dung chính (Cuộn được nếu màn hình nhỏ)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // --- Logo Area ---
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestaurantMenu,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Header Text ---
                Text(
                    text = "Chào mừng trở lại!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground, // Màu chữ chuẩn
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Đăng nhập để tiếp tục khám phá món ngon",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // --- Form Inputs ---
                ResFoodTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Outlined.Mail,
                    placeholder = "example@email.com",
                    keyboardType = KeyboardType.Email
                )

                ResFoodPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Mật khẩu",
                    isVisible = isPasswordVisible,
                    onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
                    placeholder = "Nhập mật khẩu của bạn"
                )

                // Forgot Password Link
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Quên mật khẩu?", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Login Button ---
                ResFoodButton(
                    text = "Đăng nhập",
                    onClick = { viewModel.login(email, password) },
                    isLoading = isLoading
                )

                // --- Divider (Hoặc đăng nhập bằng) ---
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        text = "HOẶC ĐĂNG NHẬP BẰNG",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                // --- Social Buttons  ---
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialButton(iconRes = R.drawable.ic_google) {
                    /* Login Google */
                        googleSignInClient.signOut().addOnCompleteListener {
                            val signInIntent = googleSignInClient.signInIntent
                            googleAuthLauncher.launch(signInIntent)
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    SocialButton(iconRes = R.drawable.ic_facebook) { /* Login Facebook */ }
                }

                // --- Footer ---
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text("Bạn chưa có tài khoản? ", color = Color.Gray)
                    Text(
                        text = "Đăng ký ngay",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }