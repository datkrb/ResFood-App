package com.muatrenthenang.resfood.ui.screens.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.Food

@Composable
fun NameAndPriceFood(food: Food?){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = food?.name ?: "Đang tải...",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Text(
            text = food?.let { "%,dđ".format(it.price) } ?: "--",
            color = Color(0xFF3B82F6),
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
    }
}