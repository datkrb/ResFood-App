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
import com.muatrenthenang.resfood.ui.layout.AppLayout
import com.muatrenthenang.resfood.ui.screens.favorites.FavoritesScreen
import com.muatrenthenang.resfood.ui.screens.admin.marketing.PromotionManagementScreen
import com.muatrenthenang.resfood.ui.screens.admin.settings.AdminSettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Khởi tạo ViewModel (Dùng viewModel())
            val userViewModel: UserViewModel = viewModel()
            // Lấy trạng thái theme từ ViewModel
            val isDarkTheme by userViewModel.isDarkTheme.collectAsState()
            // Áp dụng Theme
            ResFoodTheme (darkTheme = isDarkTheme) {
                // Tạo bộ điều hướng
                val navController = rememberNavController()

                // Khai báo các màn hình và đường dẫn
                NavHost(navController = navController, startDestination = "splash") {

                    // 0. Màn hình Chờ (Splash)
                    composable("splash") {
                        SplashScreen(
                            onAuthSuccess = { isAdmin ->
                                val destination = if (isAdmin) "admin_dashboard" else "home"
                                navController.navigate(destination) {
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
                            onLoginSuccess = { isAdmin ->
                                // Khi login thành công -> Chuyển sang Home hoặc Dashboard và xóa Login khỏi lịch sử back
                                val destination = if (isAdmin) "admin_dashboard" else "home"
                                navController.navigate(destination) {
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
                        AppLayout(navController = navController) { padding ->
                            HomeScreen(
                                onFoodClick = { food ->
                                    navController.navigate("detail/${food.id}")
                                },
                                paddingValues = padding
                            )
                        }
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
                        AppLayout(navController = navController) { padding ->
                            CartScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onProceedToCheckout = { navController.navigate("checkout") },
                                onOpenFoodDetail = { id -> navController.navigate("detail/$id") },
                                paddingValuesFromParent = padding
                            )
                        }
                    }

                    composable("checkout"){
                        CheckoutScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onPaymentConfirmed = {}
                        )
                    }

                    // Favorites route
                    composable("favorites"){
                        AppLayout(navController = navController) { padding ->
                            FavoritesScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddToCart = {},
                                onLogin = { navController.navigate("login") },
                                onOpenFoodDetail = { id -> navController.navigate("detail/$id") }
                            )
                        }
                    }

                    // Trang Setting
                    composable("settings") {
                        AppLayout(navController = navController) { _ ->
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

                    // Admin Routes
                    composable("admin_dashboard") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        com.muatrenthenang.resfood.ui.screens.admin.AdminDashboardScreen(
                            viewModel = adminViewModel,
                            onNavigateToFoodManagement = {
                                navController.navigate("admin_food_management")
                            },
                            onNavigateToMenu = {
                                // Already in admin flow, maybe switch tab or navigate?
                                // For now just keep it simple or navigate back to home if "Menu" means User Menu
                                navController.navigate("home")
                            },
                            onNavigateToAnalytics = {
                                navController.navigate("admin_analytics")
                            },
                            onNavigateToSettings = {
                                navController.navigate("admin_settings")
                            },
                            onNavigateToOrders = {
                                navController.navigate("admin_orders")
                            },
                            onNavigateToCustomers = {
                                navController.navigate("admin_customers")
                            },
                            onNavigateToPromo = {
                                navController.navigate("admin_promotions")
                            },
                            onNavigateToTables = {
                                navController.navigate("admin_tables")
                            }
                        )
                    }

                    composable("admin_promotions") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        PromotionManagementScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAdd = { navController.navigate("admin_promotion_add") }
                        )
                    }




                    composable("admin_food_management") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        com.muatrenthenang.resfood.ui.screens.admin.FoodManagementScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEdit = { foodId ->
                                if (foodId != null) {
                                    navController.navigate("admin_food_edit/$foodId")
                                } else {
                                    navController.navigate("admin_food_edit_new")
                                }
                            },
                            onNavigateToHome = {
                                navController.navigate("admin_dashboard") {
                                    popUpTo("admin_dashboard") { inclusive = true }
                                }
                            },
                            onNavigateToAnalytics = { navController.navigate("admin_analytics") },
                            onNavigateToSettings = { navController.navigate("admin_settings") },
                            onNavigateToOrders = { navController.navigate("admin_orders") }
                        )
                    }

                    composable(
                        route = "admin_food_edit/{foodId}",
                        arguments = listOf(navArgument("foodId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val foodId = backStackEntry.arguments?.getString("foodId")
                        com.muatrenthenang.resfood.ui.screens.admin.FoodEditScreen(
                            foodId = foodId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }


                    composable("admin_food_edit_new") {
                        com.muatrenthenang.resfood.ui.screens.admin.FoodEditScreen(
                            foodId = null,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // New Admin Screens
                    composable("admin_orders") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        com.muatrenthenang.resfood.ui.screens.admin.orders.OrderManagementScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { orderId -> navController.navigate("admin_order_detail/$orderId") }
                        )
                    }

                    composable("admin_order_detail/{orderId}") { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                        com.muatrenthenang.resfood.ui.screens.admin.orders.OrderDetailScreen(
                            orderId = orderId, 
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_customers") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        com.muatrenthenang.resfood.ui.screens.admin.customers.CustomerManagementScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_promotion_add") {
                        com.muatrenthenang.resfood.ui.screens.admin.marketing.PromotionAddScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_tables") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        com.muatrenthenang.resfood.ui.screens.admin.tables.TableManagementScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_analytics") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        com.muatrenthenang.resfood.ui.screens.admin.analytics.AnalyticsScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToHome = {
                                navController.navigate("admin_dashboard") {
                                    popUpTo("admin_dashboard") { inclusive = true }
                                }
                            },
                            onNavigateToMenu = { navController.navigate("admin_food_management") },
                            onNavigateToSettings = { navController.navigate("admin_settings") },
                            onNavigateToOrders = { navController.navigate("admin_orders") }
                        )
                    }

                    composable("admin_settings") {
                        val adminViewModel: com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel = viewModel()
                        AdminSettingsScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onLogout = {
                                // Clear backstack and go to login
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToHome = {
                                navController.navigate("admin_dashboard") {
                                    popUpTo("admin_dashboard") { inclusive = true }
                                }
                            },
                            onNavigateToMenu = { navController.navigate("admin_food_management") },
                            onNavigateToAnalytics = { navController.navigate("admin_analytics") },
                            onNavigateToOrders = { navController.navigate("admin_orders") }
                        )
                    }
                }
            }
        }
    }
}