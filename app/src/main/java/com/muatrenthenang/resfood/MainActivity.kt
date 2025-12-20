package com.muatrenthenang.resfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muatrenthenang.resfood.ui.screens.auth.LoginScreen
import com.muatrenthenang.resfood.ui.screens.auth.RegisterScreen
import com.muatrenthenang.resfood.ui.theme.ResFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Áp dụng Theme (Màu sắc mặc định của Android Studio)
            ResFoodTheme {
                // Tạo bộ điều hướng
                val navController = rememberNavController()

                // Khai báo các màn hình và đường dẫn
                NavHost(navController = navController, startDestination = "login") {

                    // 1. Màn hình Đăng Nhập
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                // Khi login thành công -> Chuyển sang Home và xóa Login khỏi lịch sử back
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    // 2. Màn hình Trang Chủ (Tạm thời làm giả để test Login)
                    composable("home") {
                        HomeScreenPlaceholder()
                    }

                    // 3. Màn hình Đăng Ký
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                // Đăng ký thành công -> Quay lại màn hình Login
                                navController.popBackStack()
                            },
                            onNavigateBack = {
                                // Bấm nút Back -> Quay lại màn hình Login
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- CÁC MÀN HÌNH GIẢ LẬP (PLACEHOLDER) ---
// Sau này code xong màn hình thật thì xóa mấy cái này đi

@Composable
fun HomeScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "ĐÂY LÀ TRANG CHỦ\n(Đăng nhập thành công!)")
    }
}

@Composable
fun RegisterScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Màn hình Đăng Ký\n(Sẽ làm sau)")
    }
}