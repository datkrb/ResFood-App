package com.muatrenthenang.resfood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Cấu hình bảng màu cho chế độ Tối (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    background = BgDark,
    surface = BgDark,
    onBackground = TextLight,
    onSurface = TextLight,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    outline = DarkBorder,
)

// Cấu hình bảng màu cho chế độ Sáng (Light Mode - Mặc định)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    background = BgLight,
    surface = Color.White,     // Các ô nhập liệu, card thường dùng màu trắng
    onBackground = TextDark,
    onSurface = TextDark,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    outline = LightBorder,
)

@Composable
fun ResFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // QUAN TRỌNG: Đổi thành false để app luôn giữ màu thương hiệu,
    // không bị đổi màu theo hình nền điện thoại (Dynamic Color)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Code xử lý màu thanh trạng thái (Status Bar - Chỗ hiện Pin/Giờ)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Đặt màu status bar trùng màu nền
            window.statusBarColor = colorScheme.background.toArgb()
            // Nếu nền sáng thì icon màu đen, nền tối thì icon màu trắng
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}