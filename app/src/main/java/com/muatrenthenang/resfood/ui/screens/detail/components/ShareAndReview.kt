package com.muatrenthenang.resfood.ui.screens.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
@Composable
fun ShareAndReview(
    onShareClick: () -> Unit,
    onReviewClick: () -> Unit,
    reviewCount: Int
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReviewActionChip(
                icon = Icons.Default.RateReview,
                color = Color(0xff339cff),
                text = stringResource(R.string.food_review),
                paddingValues = PaddingValues(
                    horizontal = 36.dp,
                    vertical = 10.dp
                ),
                sizeIcon = 24.dp,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                onClick = onReviewClick
            )

            ReviewActionChip(
                icon = Icons.Default.Share,
                color = Color(0xff3c82f6),
                text = stringResource(R.string.food_share),
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
        HorizontalDivider()
    }
}

@Composable
fun ReviewActionChip(
    icon: ImageVector,
    color: Color,
    text: String,
    paddingValues: PaddingValues,
    sizeIcon: Dp,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = if (enabled) color.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(paddingValues),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) color else Color.Gray,
                modifier = Modifier.size(sizeIcon)
            )
            Text(
                text = text,
                color = if (enabled) color else Color.Gray,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }
    }
}
