package com.muatrenthenang.resfood.ui.screens.home.header.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muatrenthenang.resfood.R

@Composable
fun Avatar(
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(R.drawable.domixi),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Yellow, CircleShape)
            .clickable(onClick = onClick)
    )
}