package com.muatrenthenang.resfood.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muatrenthenang.resfood.R
@Composable
fun SocialButton(iconRes: Int?, isApple: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isApple) {
                // Icon Apple thường là màu đen nên dùng Icon cũng được
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Thay bằng icon Apple nếu có
                    contentDescription = "Apple",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            } else if (iconRes != null) {
                // SỬA Ở ĐÂY: Dùng Image thay vì Icon để giữ nguyên màu gốc của Logo
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}