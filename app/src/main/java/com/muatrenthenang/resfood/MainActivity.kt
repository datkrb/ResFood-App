package com.muatrenthenang.resfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muatrenthenang.resfood.ui.screens.auth.ForgotPasswordScreen
import com.muatrenthenang.resfood.ui.screens.auth.LoginScreen
import com.muatrenthenang.resfood.ui.screens.auth.RegisterScreen
import com.muatrenthenang.resfood.ui.theme.ResFoodTheme
import com.muatrenthenang.resfood.ui.screens.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Áp dụng Theme
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
                            },
                            onNavigateToForgotPassword = {
                                navController.navigate("forgot_password")
                            }
                        )
                    }

                    // 2. Màn hình Trang Chủ (Đã thay bằng màn hình thật)
                    composable("home") {
                        // Gọi màn hình Home thật của nhóm bạn
                        HomeScreen()

                        // Nếu HomeScreen cần điều hướng (ví dụ bấm vào món ăn),
                        // bạn sẽ truyền lambda vào đây sau này. Ví dụ:
                        // HomeScreen(onFoodClick = { foodId -> navController.navigate("detail/$foodId") })
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

                    // 4. Quên mật khẩu
                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}