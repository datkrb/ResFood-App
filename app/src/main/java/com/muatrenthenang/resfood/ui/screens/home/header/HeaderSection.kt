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
import com.muatrenthenang.resfood.ui.screens.home.header.components.Avatar
import com.muatrenthenang.resfood.ui.screens.home.header.components.Location
import com.muatrenthenang.resfood.ui.screens.home.header.components.NotificationIcon
import com.muatrenthenang.resfood.ui.screens.home.header.components.TagRanking

private val HeaderBackground = Color(0xFF0F1923)

@Composable
fun HeaderSection(
    userName: String = "Cừn",
    address: String = "KTX Khu B, ĐHQG",
    rank: String = "vàng",
    point: String = "1,350 điểm"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackground),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // avatar cua user
        Avatar(onClick = {})

        // cum thong tin user
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            TagRanking(rank = rank, point = point) // ranking

            Spacer(modifier = Modifier.height(6.dp))

            // name user
            Text(
                text = "Chào buổi sáng, $userName!",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // dia chi
            Location(address = address)
        }

        // icon thong bao
        NotificationIcon()
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderSectionPreview() {
    HeaderSection()
}