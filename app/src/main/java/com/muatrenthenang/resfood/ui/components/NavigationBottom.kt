import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.muatrenthenang.resfood.ui.theme.backgroundBottomBarColor
import com.muatrenthenang.resfood.ui.theme.selectedNavBarColor
import com.muatrenthenang.resfood.ui.theme.unselectedNavBarColor


@Composable
fun NavigationBottom(
    onClick: () -> Unit,
    currentRoute: String = "home"
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = selectedNavBarColor,
        unselectedIconColor = unselectedNavBarColor,
        selectedTextColor = selectedNavBarColor,
        unselectedTextColor = unselectedNavBarColor
    )

    NavigationBar(
        containerColor = backgroundBottomBarColor,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Trang chủ") },
            selected = currentRoute == "home",
            onClick = { /* Xử lý chuyển trang chủ */ },
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng") },
            label = { Text("Giỏ hàng") },
            selected = currentRoute == "orders",
            onClick = { /* Xử lý chuyển trang giỏ hàng */ },
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Yêu thích") },
            label = { Text("Yêu thích") },
            selected = currentRoute == "favorites",
            onClick = { /* Xử lý chuyển trang yêu thích */ },
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Cài đặt") },
            label = { Text("Cài đặt") },
            selected = currentRoute == "settings",
            onClick = {},
            colors = itemColors
        )
    }
}
