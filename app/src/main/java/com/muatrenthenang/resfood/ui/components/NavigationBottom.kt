package com.muatrenthenang.resfood.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.ui.theme.backgroundBottomBarColor
import com.muatrenthenang.resfood.ui.theme.selectedNavBarColor
import com.muatrenthenang.resfood.ui.theme.unselectedNavBarColor


@Composable
fun NavigationBottom(
    currentRoute: String = "",
    onNavigateToHome: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToMe: () -> Unit = {}
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.footer_home)) },
            label = { Text(stringResource(R.string.footer_home)) },
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.footer_cart)) },
            label = { Text(stringResource(R.string.footer_cart)) },
            selected = currentRoute == "cart",
            onClick = onNavigateToCart,
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.footer_favorites)) },
            label = { Text(stringResource(R.string.footer_favorites)) },
            selected = currentRoute == "favorites",
            onClick = onNavigateToFavorites,
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.footer_me)) },
            label = { Text(stringResource(R.string.footer_me)) },
            selected = currentRoute == "me",
            onClick = onNavigateToMe,
            colors = itemColors
        )
    }
}
