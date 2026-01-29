package com.muatrenthenang.resfood.ui.screens.detail.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.Food
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R

@Composable
fun FoodDescription(food: Food?){
    Text(
        text = stringResource(R.string.food_description_title),
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
    Text(
        text = food?.description ?: stringResource(R.string.food_detail_loading),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp
    )
}