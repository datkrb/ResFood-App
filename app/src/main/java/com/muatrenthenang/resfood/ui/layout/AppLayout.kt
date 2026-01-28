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
    val userViewModel: com.muatrenthenang.resfood.ui.viewmodel.UserViewModel = viewModel()
    val chatViewModel: com.muatrenthenang.resfood.ui.viewmodel.ChatViewModel = viewModel() // Add ChatViewModel
    val userState by userViewModel.userState.collectAsState()
    val unreadChatCount by chatViewModel.totalUnreadCount.collectAsState()

    // Monitor chat unread count
    androidx.compose.runtime.LaunchedEffect(userState) {
        if (userState != null) {
            chatViewModel.startUnreadCountMonitor(
                isAdmin = userState?.role == "admin",
                userId = userState!!.id
            )
        }
    }

    // Only show Bottom Bar on the main 4 tabs
    val showBottomBarRoutes = listOf("home", "cart", "favorites", "me")
    val showBottom = currentRoute in showBottomBarRoutes

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
                        unreadCount = unreadChatCount,
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