package com.muatrenthenang.resfood.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.muatrenthenang.resfood.ui.components.DraggableChatBubble
import com.muatrenthenang.resfood.ui.components.NavigationBottom

@Composable
fun AppLayout(
    navController: NavController,
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val context = LocalContext.current
    
    // Quick user check to drive bubble logic
    val viewModel: com.muatrenthenang.resfood.ui.viewmodel.UserViewModel = viewModel()
    val userState by viewModel.userState.collectAsState()

    // Routes waiting for hiding Bottom Bar
    // Note: Better to organize this list in a constant file eventually
    val hideBottomBarRoutes = listOf(
        "login", "register", "forgot_password", "splash",
        "admin_dashboard", "admin_food_management", "admin_promotion_add",
        "admin_orders", "admin_customers", "admin_tables", "admin_analytics", "admin_settings",
        "detail/{foodId}", "cart", "checkout", "booking_table", "chat_list", "chat_detail/{chatId}"
    )

    // Also hide if route starts with admin
    val showBottom = currentRoute !in hideBottomBarRoutes && !currentRoute.startsWith("admin_")

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBottom(
                    currentRoute = currentRoute,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCart = {
                        navController.navigate("cart") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFavorites = {
                        navController.navigate("favorites") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMe = {
                        navController.navigate("me") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(padding)
            
            // Floating Chat Bubble
            // Only show on Customer Home or Admin Dashboard
            if (userState != null && (currentRoute == "home" || currentRoute == "admin_dashboard")) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp, end = 24.dp), 
                    contentAlignment = androidx.compose.ui.Alignment.BottomEnd
                ) {
                   DraggableChatBubble(
                        onClick = {
                            if (userState?.role == "admin") {
                                navController.navigate("chat_list")
                            } else {
                                navController.navigate("chat_detail/${userState?.id}")
                            }
                        }
                   )
                }
            }
        }
    }
}