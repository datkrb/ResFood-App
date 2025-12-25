package com.muatrenthenang.resfood.ui.screens.detail.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.Food

@Composable
fun FoodDescription(food: Food?){
    Text(
        text = "Mô tả",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
    Text(
        text = food?.description ?: "Đang tải...",
        color = Color.White,
        fontSize = 16.sp
    )
}