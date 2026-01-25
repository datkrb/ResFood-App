package com.muatrenthenang.resfood.ui.screens.home.category_food

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import androidx.compose.material3.MaterialTheme

@Composable
fun CategoryFood(
    imgVector: ImageVector,
    categoryFood: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
){
    val backgroundColor = if (isSelected) PrimaryColor else Color(0xFFF0F0F0)
    val iconColor = if (isSelected) Color.White else PrimaryColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = imgVector,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = categoryFood,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp
        )
    }
}