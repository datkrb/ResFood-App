package com.muatrenthenang.resfood.ui.screens.me.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muatrenthenang.resfood.ui.theme.PrimaryColor

/**
 * Nút icon hình tròn với nền
 *
 * @param icon Icon hiển thị
 * @param onClick Callback khi nhấn
 * @param contentDescription Mô tả cho accessibility
 * @param modifier Modifier tùy chỉnh
 * @param size Kích thước nút (mặc định 40.dp)
 * @param backgroundColor Màu nền
 * @param iconTint Màu icon
 */
@Composable
fun IconCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    iconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint
        )
    }
}

/**
 * Icon hình tròn với nền màu (không có onClick)
 *
 * @param icon Icon hiển thị
 * @param modifier Modifier tùy chỉnh
 * @param size Kích thước (mặc định 48.dp)
 * @param backgroundColor Màu nền
 * @param iconTint Màu icon
 * @param iconSize Kích thước icon bên trong
 */
@Composable
fun CircleIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = PrimaryColor.copy(alpha = 0.15f),
    iconTint: Color = PrimaryColor,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
