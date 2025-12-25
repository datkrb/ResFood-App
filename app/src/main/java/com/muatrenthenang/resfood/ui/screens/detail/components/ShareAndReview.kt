package com.muatrenthenang.resfood.ui.screens.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShareAndReview(
    onReviewClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoChip(
            Icons.Default.RateReview,
            Color(0xff339cff),
            "Đánh giá",
            paddingValues = PaddingValues(
                horizontal = 36.dp,
                vertical = 10.dp
            ),
            sizeIcon = 24.dp,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            onClick = onReviewClick
        )

        InfoChip(
            Icons.Default.Share,
            Color(0xff3c82f6),
            "Chia sẻ",
            paddingValues = PaddingValues(
                horizontal = 36.dp,
                vertical = 10.dp
            ),
            sizeIcon = 24.dp,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            onClick = onShareClick
        )
    }
}
