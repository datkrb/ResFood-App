package com.muatrenthenang.resfood.ui.screens.home.header.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun Avatar(
    imageUrl: String?,
    onClick: () -> Unit
) {
    // Convert local path to File object for Coil to load
    val imageModel = imageUrl?.let { path ->
        // Kiểm tra nếu là local path (bắt đầu bằng /)
        if (path.startsWith("/")) {
            File(path)
        } else {
            path // URL từ internet
        }
    }
    
    AsyncImage(
        model = imageModel,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Yellow, CircleShape)
            .clickable(onClick = onClick)
    )
}