package com.muatrenthenang.resfood.ui.screens.home.header.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.muatrenthenang.resfood.ui.screens.me.components.BadgeCount


@Composable
fun NotificationIcon(
    unreadCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        // The Circular Button Background & Icon (Clipped)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Notification badge - Rendered ON TOP of the button and NOT clipped relative to the button background
        // It resides in the outer unclipped Box
        BadgeCount(
            count = unreadCount,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = (-2).dp)
        )
    }
}