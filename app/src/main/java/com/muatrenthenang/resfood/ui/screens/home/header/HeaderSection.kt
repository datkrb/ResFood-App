package com.muatrenthenang.resfood.ui.screens.home.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.ui.screens.home.header.components.Avatar
import com.muatrenthenang.resfood.ui.screens.home.header.components.LocationText
import com.muatrenthenang.resfood.ui.screens.home.header.components.NotificationIcon
import com.muatrenthenang.resfood.ui.screens.home.header.components.TagRanking


@Composable
fun HeaderSection(
    user: User?,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // avatar cua user
        Avatar(
            imageUrl = user?.avatarUrl
        )

        // cum thong tin user
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            TagRanking(rank = user?.rank, point = user?.points) // ranking

            Spacer(modifier = Modifier.height(6.dp))

            // name user
            Text(
                text = stringResource(R.string.home_greeting, user?.fullName ?: "Bạn"),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // dia chi
            LocationText(address = user?.getDefaultAddress()?.getFullAddress())
        }

        // icon thong bao
        NotificationIcon(
            unreadCount = unreadCount,
            onClick = onNotificationClick
        )
    }
}