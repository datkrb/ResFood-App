package com.muatrenthenang.resfood.ui.layout

import com.muatrenthenang.resfood.ui.components.NavigationBottom
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppLayout(
    navController: NavController,
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    // Bạn có thể giữ dòng này hoặc xóa đi nếu bottom bar luôn hiển thị trên các màn hình này
    // Bạn có thể giữ dòng này hoặc xóa đi nếu bottom bar luôn hiển thị trên các màn hình này
    val showBottom = currentRoute in listOf("home", "cart", "favorites", "me")

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBottom(
                    currentRoute = currentRoute,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCart = {
                        navController.navigate("cart") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFavorites = {
                        navController.navigate("favorites") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMe = {
                        navController.navigate("me") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        content(padding)
    }
}