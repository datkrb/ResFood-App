package com.muatrenthenang.resfood.ui.layout

import com.muatrenthenang.resfood.ui.components.NavigationBottom
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun AppLayout(
    navController: NavController,
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val showBottom = currentRoute in listOf("home", "cart", "favorites", "settings")

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBottom(
                    currentRoute = currentRoute,
                    onNavigateToHome = { navController.navigate("home") { launchSingleTop = true } },
                    onNavigateToCart = { navController.navigate("cart") { launchSingleTop = true } },
                    onNavigateToFavorites = { navController.navigate("favorites") { launchSingleTop = true } },
                    onNavigateToSettings = { navController.navigate("settings") { launchSingleTop = true } }
                )
            }
        }
    ) { padding ->
        content(padding)
    }
}