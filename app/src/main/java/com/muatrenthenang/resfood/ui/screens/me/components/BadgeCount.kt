package com.muatrenthenang.resfood.ui.screens.me.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.theme.LightRed

/**
 * Badge hiển thị số lượng (dùng cho thông báo, đơn hàng, v.v.)
 *
 * @param count Số lượng cần hiển thị
 * @param modifier Modifier tùy chỉnh vị trí
 * @param size Kích thước badge (mặc định 18.dp)
 * @param backgroundColor Màu nền badge (mặc định LightRed)
 */
@Composable
fun BadgeCount(
    count: Int,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    backgroundColor: Color = LightRed
) {
    if (count > 0) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            modifier = modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

/**
 * Badge nhỏ dạng chấm tròn (không hiển thị số)
 *
 * @param modifier Modifier tùy chỉnh vị trí
 * @param size Kích thước badge (mặc định 8.dp)
 * @param color Màu badge
 */
@Composable
fun BadgeDot(
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    color: Color = LightRed
) {
    Surface(
        shape = CircleShape,
        color = color,
        modifier = modifier.size(size)
    ) {}
}
