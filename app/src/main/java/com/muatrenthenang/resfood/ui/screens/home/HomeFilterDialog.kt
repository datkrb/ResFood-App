package com.muatrenthenang.resfood.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.data.model.CategoryItem
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFilterDialog(
    currentMinPrice: Int?,
    currentMaxPrice: Int?,
    currentMinRating: Float?,
    currentCategory: String?, // Keep to preserve state, but remove selection UI
    onApply: (minPrice: Int?, maxPrice: Int?, minRating: Float?, category: String?) -> Unit,
    onDismiss: () -> Unit
) {
    // We keep selectedCategory = currentCategory so we don't accidentally clear it when applying other filters
    val selectedCategory by remember { mutableStateOf(currentCategory) }
    var minRating by remember { mutableStateOf(currentMinRating) }
    
    // Price range state (0 to 2M)
    val maxPriceLimit = 2000000f
    var priceRange by remember {
        mutableStateOf(
            (currentMinPrice?.toFloat() ?: 0f) .. (currentMaxPrice?.toFloat() ?: maxPriceLimit)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 30.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ lọc tìm kiếm",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 1. Price Range
            Text(
                text = "Khoảng giá",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "${formatCurrency(priceRange.start.toLong())} - ${formatCurrency(priceRange.endInclusive.toLong())}",
                color = PrimaryColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            RangeSlider(
                value = priceRange,
                onValueChange = { priceRange = it },
                valueRange = 0f..maxPriceLimit,
                steps = 39, // 50k steps
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryColor,
                    activeTrackColor = PrimaryColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Rating
            Text(
                text = "Đánh giá",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(5f, 4f, 3f, 2f, 1f).forEach { rating ->
                    FilterChip(
                        selected = minRating == rating,
                        onClick = { 
                            minRating = if (minRating == rating) null else rating 
                        },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (rating == 5f) "5" else ">=${rating.toInt()}")
                                Spacer(modifier = Modifier.width(1.dp))
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFC107))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryColor.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Reset
                        minRating = null
                        priceRange = 0f..maxPriceLimit
                        
                        // Auto apply and dismiss
                        onApply(
                            0,
                            maxPriceLimit.toInt(),
                            null,
                            selectedCategory
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đặt lại")
                }
                
                Button(
                    onClick = {
                        onApply(
                            priceRange.start.toInt(),
                            priceRange.endInclusive.toInt(),
                            minRating,
                            selectedCategory
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Áp dụng")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}
