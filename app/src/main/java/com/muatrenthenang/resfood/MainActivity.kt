package com.muatrenthenang.resfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import com.muatrenthenang.resfood.ui.layout.AppLayout
import androidx.navigation.navDeepLink
import com.muatrenthenang.resfood.ui.screens.admin.AdminDashboardScreen
import com.muatrenthenang.resfood.ui.screens.admin.FoodEditScreen
import com.muatrenthenang.resfood.ui.screens.admin.FoodManagementScreen
import com.muatrenthenang.resfood.ui.screens.admin.analytics.AnalyticsScreen
import com.muatrenthenang.resfood.ui.screens.admin.customers.CustomerManagementScreen
import com.muatrenthenang.resfood.ui.screens.admin.marketing.PromotionAddScreen
import com.muatrenthenang.resfood.ui.screens.admin.marketing.PromotionManagementScreen
import com.muatrenthenang.resfood.ui.screens.admin.orders.OrderDetailScreen
import com.muatrenthenang.resfood.ui.screens.admin.orders.OrderManagementScreen
import com.muatrenthenang.resfood.ui.screens.admin.settings.AdminSettingsScreen
import com.muatrenthenang.resfood.ui.screens.admin.tables.TableManagementScreen
import com.muatrenthenang.resfood.ui.screens.auth.ForgotPasswordScreen
import com.muatrenthenang.resfood.ui.screens.auth.LoginScreen
import com.muatrenthenang.resfood.ui.screens.auth.RegisterScreen
import com.muatrenthenang.resfood.ui.screens.auth.SplashScreen
import com.muatrenthenang.resfood.ui.screens.cart.CartScreen
import com.muatrenthenang.resfood.ui.screens.checkout.CheckoutScreen
import com.muatrenthenang.resfood.ui.screens.detail.FoodDetailScreen
import com.muatrenthenang.resfood.ui.screens.favorites.FavoritesScreen
import com.muatrenthenang.resfood.ui.screens.home.HomeScreen
import com.muatrenthenang.resfood.ui.screens.settings.SettingScreen
import com.muatrenthenang.resfood.ui.screens.booking.BookingTableScreen

import com.muatrenthenang.resfood.ui.screens.settings.profile.ProfileScreen
import com.muatrenthenang.resfood.ui.screens.me.MeScreen
import com.muatrenthenang.resfood.ui.screens.me.ReferralScreen
import com.muatrenthenang.resfood.ui.screens.me.ReferralHistoryScreen
import com.muatrenthenang.resfood.ui.screens.me.VoucherScreen
import com.muatrenthenang.resfood.ui.screens.me.SpendingStatisticsScreen
import com.muatrenthenang.resfood.ui.screens.order.OrderListScreen
import com.muatrenthenang.resfood.ui.screens.order.UserOrderDetailScreen
import androidx.navigation.navArgument
import com.muatrenthenang.resfood.ui.screens.address.AddressListScreen
import com.muatrenthenang.resfood.ui.screens.address.AddressEditScreen
import com.muatrenthenang.resfood.ui.screens.address.MapPickerScreen
import androidx.compose.ui.platform.LocalContext
import com.muatrenthenang.resfood.ui.theme.ResFoodTheme
import com.muatrenthenang.resfood.ui.viewmodel.UserViewModel
import com.muatrenthenang.resfood.ui.viewmodel.AddressViewModel
import com.muatrenthenang.resfood.ui.viewmodel.CheckoutViewModel
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.ui.viewmodel.auth.LoginViewModel
import com.muatrenthenang.resfood.ui.screens.review.ReviewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Khởi tạo ViewModel
            val userViewModel: UserViewModel = viewModel()
            // Lấy trạng thái theme từ ViewModel
            val isDarkTheme by userViewModel.isDarkTheme.collectAsState()
            // Áp dụng Theme
            ResFoodTheme(darkTheme = isDarkTheme) {
                // Tạo bộ điều hướng
                val navController = rememberNavController()

                // AppLayout bao bọc toàn bộ NavHost
                AppLayout(navController = navController) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier
                    ) {

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
                                    // Refresh user profile after login
                                    userViewModel.fetchUserProfile()
                                    
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

                        // 2. Màn hình Trang Chủ
                        composable("home") {
                            HomeScreen(
                                onFoodClick = { food ->
                                    navController.navigate("detail/${food.id}")
                                },
                                onNavigateToBooking = {
                                    navController.navigate("booking_table")
                                },
                                paddingValues = innerPadding
                            )
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

                        composable("cart") {
                            CartScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onProceedToCheckout = { navController.navigate("checkout") },
                                onOpenFoodDetail = { id -> navController.navigate("detail/$id") },

                                paddingValuesFromParent = innerPadding
                            )
                        }

                        composable("checkout") { backStackEntry ->
                            val checkoutViewModel: CheckoutViewModel = viewModel()

                            // Listen for selected address from AddressListScreen
                            LaunchedEffect(backStackEntry) {
                                backStackEntry.savedStateHandle.getStateFlow<String?>(
                                    "selected_address_id",
                                    null
                                )
                                    .collect { addressId ->
                                        addressId?.let { id ->
                                            // Load the selected address by ID
                                            val addresses = checkoutViewModel.getAddressesFromRepo()
                                            addresses.find { it.id == id }?.let { address ->
                                                checkoutViewModel.setAddress(address)
                                            }
                                            // Clear the saved state
                                            backStackEntry.savedStateHandle.remove<String>("selected_address_id")
                                        }
                                    }
                            }

                            CheckoutScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddresses = { navController.navigate("addresses") },
                                onPaymentConfirmed = {
                                    navController.navigate("orders/pending") {
                                        popUpTo("home")
                                    }
                                },
                                vm = checkoutViewModel
                            )
                        }

                        // Address management routes
                        composable("addresses") {
                            val addressViewModel: AddressViewModel = viewModel()

                            // Reload addresses when this screen becomes visible
                            LaunchedEffect(Unit) {
                                addressViewModel.loadAddresses()
                            }

                            AddressListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEdit = { addressId ->
                                    if (addressId != null) {
                                        navController.navigate("address_edit/$addressId")
                                    } else {
                                        navController.navigate("address_add")
                                    }
                                },
                                onAddressSelected = { address ->
                                    // Pass the selected address ID back to checkout via savedStateHandle
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_address_id",
                                        address.id
                                    )
                                    navController.popBackStack()
                                },
                                vm = addressViewModel
                            )
                        }

                        composable("address_add") { backStackEntry ->
                            val addressViewModel: AddressViewModel = viewModel()
                            AddressEditScreen(
                                addressId = null,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToMap = { lat, lng ->
                                    val route =
                                        if (lat != null && lng != null) "map_picker?lat=$lat&lng=$lng" else "map_picker"
                                    navController.navigate(route)
                                },
                                onSaveSuccess = {
                                    navController.popBackStack()
                                },
                                vm = addressViewModel,
                                savedStateHandle = backStackEntry.savedStateHandle
                            )
                        }

                        composable(
                            route = "address_edit/{addressId}",
                            arguments = listOf(navArgument("addressId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val addressId = backStackEntry.arguments?.getString("addressId")
                            val addressViewModel: AddressViewModel = viewModel()
                            AddressEditScreen(
                                addressId = addressId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToMap = { lat, lng ->
                                    val route =
                                        if (lat != null && lng != null) "map_picker?lat=$lat&lng=$lng" else "map_picker"
                                    navController.navigate(route)
                                },
                                onSaveSuccess = {
                                    navController.popBackStack()
                                },
                                vm = addressViewModel,
                                savedStateHandle = backStackEntry.savedStateHandle
                            )
                        }

                        composable(
                            route = "map_picker?lat={lat}&lng={lng}",
                            arguments = listOf(
                                navArgument("lat") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("lng") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val latStr = backStackEntry.arguments?.getString("lat")
                            val lngStr = backStackEntry.arguments?.getString("lng")
                            val lat = latStr?.toDoubleOrNull()
                            val lng = lngStr?.toDoubleOrNull()

                            MapPickerScreen(
                                initialLat = lat,
                                initialLng = lng,
                                onNavigateBack = { navController.popBackStack() },
                                onLocationPicked = { pickedLat, pickedLng ->
                                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                                        set("picked_lat", pickedLat)
                                        set("picked_lng", pickedLng)
                                    }
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Favorites route
                        composable("favorites") {
                            FavoritesScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddToCart = {},
                                onLogin = { navController.navigate("login") },
                                onOpenFoodDetail = { id -> navController.navigate("detail/$id") },
                                paddingValuesFromParent = innerPadding
                            )
                        }

                        // Màn hình Tôi (Me)
                        composable("me") {
                            MeScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToNotifications = { /* TODO: Notification Screen */ },
                                onNavigateToEditProfile = { navController.navigate("profile_details") },
                                onNavigateToOrders = { status ->
                                    navController.navigate("orders/$status")
                                },

                                onNavigateToReferral = { navController.navigate("referral") },
                                onNavigateToVouchers = { navController.navigate("vouchers") },
                                onNavigateToAddresses = { navController.navigate("profile_addresses") },
                                onNavigateToHelpCenter = { /* TODO: Help Center */ },
                                onNavigateToPaymentMethods = { /* TODO: Payment Methods */ },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                paddingValuesFromParent = innerPadding,
                                onNavigateToMembership = { navController.navigate("membership") },
                                onNavigateToSpendingStatistics = { navController.navigate("spending_statistics") },
                                vm = userViewModel
                            )
                        }

                        // Màn hình Hạng thành viên
                        composable("membership") {
                            com.muatrenthenang.resfood.ui.screens.me.MembershipScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Màn hình Đặt bàn
                        composable("booking_table") {
                            BookingTableScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }  
                        
                        // Màn hình Thống kê chi tiêu
                        composable("spending_statistics") {
                            SpendingStatisticsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Referral Routes
                        composable("referral") {
                            ReferralScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToHistory = { navController.navigate("referral_history") }
                            )
                        }

                        composable("referral_history") {
                            ReferralHistoryScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("vouchers") {
                            VoucherScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "orders/{status}",
                            arguments = listOf(navArgument("status") { defaultValue = "all" })
                        ) { backStackEntry ->
                            val status = backStackEntry.arguments?.getString("status") ?: "all"
                            OrderListScreen(
                                status = status,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToDetail = { orderId -> navController.navigate("order_detail/$orderId") }
                            )
                        }

                        composable(
                            route = "order_detail/{orderId}",
                            arguments = listOf(navArgument("orderId") { defaultValue = "" })
                        ) { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                            UserOrderDetailScreen(
                                orderId = orderId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }


                        // Trang Setting
                        composable("settings") {
                            SettingScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToLogin = {
                                    // Đăng xuất thành công -> Về màn Login và xóa lịch sử
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                paddingValuesFromParent = innerPadding,
                                userViewModel = userViewModel
                            )
                        }


                        // Màn hình Hồ sơ cá nhân chi tiết
                        composable("profile_details") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToAddresses = { navController.navigate("profile_addresses") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                userViewModel = userViewModel
                            )
                        }

                        composable(
                            route = "detail/{foodId}",
                            arguments = listOf(navArgument("foodId") { type = NavType.StringType }),
                            deepLinks = listOf(navDeepLink { uriPattern = "resfood://food/{foodId}" })
                        ) { backStackEntry ->
                            val foodId = backStackEntry.arguments?.getString("foodId").orEmpty()
                            FoodDetailScreen(
                                foodId = foodId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToReview = { id -> navController.navigate("review/$id") }
                            )
                        }

                        composable(
                            route = "review/{foodId}",
                            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val foodId = backStackEntry.arguments?.getString("foodId").orEmpty()
                            ReviewScreen(
                                foodId = foodId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Admin Routes
                        composable("admin_dashboard") {
                            val adminViewModel: AdminViewModel = viewModel()
                            AdminDashboardScreen(
                                viewModel = adminViewModel,
                                onNavigateToFoodManagement = {
                                    navController.navigate("admin_food_management")
                                },
                                onNavigateToMenu = {
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
                            val adminViewModel: AdminViewModel = viewModel()
                            PromotionManagementScreen(
                                viewModel = adminViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAdd = { navController.navigate("admin_promotion_add") }
                            )
                        }


                        composable("admin_food_management") {
                            val adminViewModel: AdminViewModel = viewModel()
                            FoodManagementScreen(
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
                            FoodEditScreen(
                                foodId = foodId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }


                        composable("admin_food_edit_new") {
                            FoodEditScreen(
                                foodId = null,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // New Admin Screens
                        composable("admin_orders") {
                            val adminViewModel: AdminViewModel = viewModel()
                            OrderManagementScreen(
                                viewModel = adminViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToDetail = { orderId -> navController.navigate("admin_order_detail/$orderId") }
                            )
                        }

                        composable("admin_order_detail/{orderId}") { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                            OrderDetailScreen(
                                orderId = orderId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_customers") {
                            val adminViewModel: AdminViewModel = viewModel()
                            CustomerManagementScreen(
                                viewModel = adminViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_promotion_add") {
                            PromotionAddScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_tables") {
                            val adminViewModel: AdminViewModel = viewModel()
                            TableManagementScreen(
                                viewModel = adminViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_analytics") {
                            val adminViewModel: AdminViewModel = viewModel()
                            AnalyticsScreen(
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
                            val adminViewModel: AdminViewModel = viewModel()
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

                        // Quản lý địa chỉ từ Profile
                        composable("profile_addresses") {
                            val addressViewModel: AddressViewModel = viewModel()

                            LaunchedEffect(Unit) {
                                addressViewModel.loadAddresses()
                            }

                            AddressListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEdit = { addressId ->
                                    if (addressId != null) {
                                        navController.navigate("address_edit/$addressId")
                                    } else {
                                        navController.navigate("address_add")
                                    }
                                },
                                onAddressSelected = {
                                    // Không làm gì khi chọn địa chỉ từ profile, chỉ để xem
                                    navController.popBackStack()
                                },
                                vm = addressViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}