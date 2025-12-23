package com.muatrenthenang.resfood.ui.screens.home.card_food

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.bgCardFood

@Composable
fun CardFood(
    food: Food,
    onClickFood: (Food) -> Unit,
    onClickAdd: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .wrapContentHeight()
            .clickable{
                onClickFood(food)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Column(
            modifier = Modifier
                .background(bgCardFood)
        ) {

            // IMAGE
            AsyncImage(
                model = food.imageUrl,
                contentDescription = "Food image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {

                Text(
                    text = food.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "%,dđ".format(food.price),
                        color = PrimaryColor.copy(alpha = 0.9f), // xanh dương
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = PrimaryColor,
                                shape = CircleShape
                            )
                            .clickable(onClick = onClickAdd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}