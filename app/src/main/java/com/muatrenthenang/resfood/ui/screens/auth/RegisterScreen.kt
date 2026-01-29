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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.muatrenthenang.resfood.ui.viewmodel.auth.RegisterViewModel

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
                Toast.makeText(context, context.getString(R.string.auth_register_success), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onRegisterSuccess()
            }
            null -> {}
            else -> Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }

    //xử lí google
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

    // TODO: Implement Google Sign-In in AuthRepository first
    // Launcher để mở cửa sổ chọn tài khoản Google
    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                viewModel.registerWithGoogle(idToken) // Not implemented yet
            }
        } catch (e: ApiException) {
            Toast.makeText(context, context.getString(R.string.auth_google_fail, e.statusCode), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        // Dùng màu từ Theme chung (Theme.kt đã map BgLight vào background)
        containerColor = MaterialTheme.colorScheme.background,
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
                        // Dùng màu onBackground (TextDark) từ Theme
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.auth_register_now),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    // Dùng màu onBackground (TextDark) từ Theme
                    color = MaterialTheme.colorScheme.onBackground,
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
                text = stringResource(R.string.auth_create_account),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Text(
                text = stringResource(R.string.auth_register_subtitle),
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
                label = stringResource(R.string.auth_full_name),
                icon = Icons.Outlined.Person,
                placeholder = stringResource(R.string.auth_full_name_placeholder)
            )

            ResFoodTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.auth_email),
                icon = Icons.Outlined.Mail,
                placeholder = stringResource(R.string.auth_email_placeholder),
                keyboardType = KeyboardType.Email
            )

            ResFoodPasswordField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.auth_password),
                isVisible = isPasswordVisible,
                onToggleVisibility = { isPasswordVisible = !isPasswordVisible }
            )

            ResFoodPasswordField(
                value = rePassword,
                onValueChange = { rePassword = it },
                label = stringResource(R.string.auth_re_password),
                isVisible = isRePasswordVisible,
                onToggleVisibility = { isRePasswordVisible = !isRePasswordVisible }
            )

            // Forgot Password Link
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { /* TODO: Navigate to ForgotPass */ }) {
                    // Dùng màu Primary từ Theme
                    Text(stringResource(R.string.auth_forgot_password), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Submit Button (Dùng Component chung) ---
            ResFoodButton(
                text = stringResource(R.string.auth_register_now),
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
                    text = stringResource(R.string.auth_or_continue_with),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            }

            // --- Social Login Buttons (Dùng Component chung) ---
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialButton(iconRes = R.drawable.ic_google){
                    /* Login Google */
                    val signInIntent = googleSignInClient.signInIntent
                    googleAuthLauncher.launch(signInIntent)}
                Spacer(modifier = Modifier.width(20.dp))
                SocialButton(iconRes = R.drawable.ic_facebook) { /* Login FB */ }
                Spacer(modifier = Modifier.width(20.dp))
            }

            // --- Footer ---
            Spacer(modifier = Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.auth_have_account), color = Color.Gray)
                Text(
                    text = stringResource(R.string.auth_login_button),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}