package com.muatrenthenang.resfood

import SplashScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.muatrenthenang.resfood.ui.screens.auth.ForgotPasswordScreen
import com.muatrenthenang.resfood.ui.screens.auth.LoginScreen
import com.muatrenthenang.resfood.ui.screens.auth.RegisterScreen
import com.muatrenthenang.resfood.ui.theme.ResFoodTheme
import com.muatrenthenang.resfood.ui.screens.home.HomeScreen
import com.muatrenthenang.resfood.ui.screens.cart.CartScreen
import com.muatrenthenang.resfood.ui.screens.checkout.CheckoutScreen
import com.muatrenthenang.resfood.ui.screens.detail.FoodDetailScreen
import com.muatrenthenang.resfood.ui.viewmodel.UserViewModel
import com.muatrenthenang.resfood.ui.viewmodel.auth.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Áp dụng Theme
            ResFoodTheme {
                // Tạo bộ điều hướng
                val navController = rememberNavController()
                // Khởi tạo ViewModel (Dùng viewModel())
                val userViewModel: UserViewModel = viewModel()

                // Khai báo các màn hình và đường dẫn
                NavHost(navController = navController, startDestination = "splash") {

                    //Check Login
                    composable("splash") {
                        SplashScreen(
                            onGoHome = {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            },
                            onGoLogin = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
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
                        HomeScreen(
                            onFoodClick = { food ->
                                navController.navigate("detail/${food.id}")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )

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

                    composable("cart"){
                        CartScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onProceedToCheckout = {navController.navigate("checkout")}
                        )
                    }

                    composable("checkout"){
                        CheckoutScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onPaymentConfirmed = {}
                        )
                    }

                    // Trang Setting
                    composable("settings") {
                        com.muatrenthenang.resfood.ui.screens.settings.SettingScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToLogin = {
                                // Đăng xuất thành công -> Về màn Login và xóa lịch sử
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate("account_center")
                            },
                            userViewModel = userViewModel
                        )
                    }

                    // Màn hình Trung tâm tài khoản
                    composable("account_center") {
                        com.muatrenthenang.resfood.ui.screens.settings.profile.AccountCenterScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToDetails = {
                                // Bấm "Thông tin chi tiết" -> Chuyển sang Hồ sơ cá nhân (ProfileScreen)
                                navController.navigate("profile_details")
                            },
                            userViewModel = userViewModel
                        )
                    }

                    // Màn hình Hồ sơ cá nhân chi tiết
                    composable("profile_details") {
                        com.muatrenthenang.resfood.ui.screens.settings.profile.ProfileScreen(
                            onBack = { navController.popBackStack() },
                            userViewModel = userViewModel
                        )
                    }

                    composable(
                        route = "detail/{foodId}",
                        arguments = listOf(navArgument("foodId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val foodId = backStackEntry.arguments?.getString("foodId").orEmpty()
                        FoodDetailScreen(
                            foodId = foodId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}