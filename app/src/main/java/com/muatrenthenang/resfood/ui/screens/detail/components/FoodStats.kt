package com.muatrenthenang.resfood.ui.screens.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.muatrenthenang.resfood.data.model.Food
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R

@Composable
fun FoodStats(food: Food?){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoChip(
            Icons.Default.LocalFireDepartment,
            Color(0xfff97315),
            "${food?.calories ?: 0} ${stringResource(R.string.food_stats_kcal)}",
            modifier = Modifier
        )
        InfoChip(
            Icons.Default.WatchLater,
            Color(0xff3c82f6),
            stringResource(R.string.food_stats_time),
            modifier = Modifier
        )
        InfoChip(
            Icons.Default.Star,
            Color(0xffe9b40a),
            String.format("%.1f ★", food?.rating ?: 0f),
            modifier = Modifier
        )
    }
}