package com.muatrenthenang.resfood.ui.screens.me

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.ResFoodTheme
import com.muatrenthenang.resfood.ui.viewmodel.SpendingStatisticsViewModel
import com.muatrenthenang.resfood.ui.viewmodel.SpendingPeriod
import com.muatrenthenang.resfood.ui.viewmodel.CategorySpending
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingStatisticsScreen(
    onNavigateBack: () -> Unit = {},
    vm: SpendingStatisticsViewModel = viewModel()
) {
    val totalSpending by vm.totalSpending.collectAsState()
    val categorySpending by vm.categorySpending.collectAsState()
    val selectedPeriod by vm.selectedPeriod.collectAsState()
    val weeklyData by vm.weeklyData.collectAsState()
    val monthlyData by vm.monthlyData.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.stats_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Period Selector (Week/Month)
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { vm.setSelectedPeriod(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Total Spending Card
                TotalSpendingCard(totalSpending = totalSpending, period = selectedPeriod)

                Spacer(modifier = Modifier.height(24.dp))

                // Category Breakdown Section
                Text(
                    text = stringResource(R.string.stats_by_category),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pie Chart for Category
                CategoryPieChart(
                    categorySpending = categorySpending,
                    totalSpending = totalSpending
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Legend & Details
                CategoryLegend(categorySpending = categorySpending)

                Spacer(modifier = Modifier.height(24.dp))

                // Time-based Bar Chart
                Text(
                    text = if (selectedPeriod == SpendingPeriod.WEEK) stringResource(R.string.stats_last_7_days) else stringResource(R.string.stats_last_30_days),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bar Chart
                SpendingBarChart(
                    data = if (selectedPeriod == SpendingPeriod.WEEK) weeklyData else monthlyData,
                    period = selectedPeriod
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Loading Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: SpendingPeriod,
    onPeriodSelected: (SpendingPeriod) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PeriodTab(
                text = stringResource(R.string.stats_week),
                isSelected = selectedPeriod == SpendingPeriod.WEEK,
                onClick = { onPeriodSelected(SpendingPeriod.WEEK) },
                modifier = Modifier.weight(1f)
            )
            PeriodTab(
                text = stringResource(R.string.stats_month),
                isSelected = selectedPeriod == SpendingPeriod.MONTH,
                onClick = { onPeriodSelected(SpendingPeriod.MONTH) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PeriodTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryColor else Color.Transparent,
        animationSpec = tween(300),
        label = "tabBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "tabText"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp)
        )
    }
}

@Composable
private fun TotalSpendingCard(
    totalSpending: Long,
    period: SpendingPeriod
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryColor,
                            PrimaryColor.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (period == SpendingPeriod.WEEK) stringResource(R.string.stats_total_week) else stringResource(R.string.stats_total_month),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = com.muatrenthenang.resfood.util.CurrencyHelper.format(totalSpending),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CategoryPieChart(
    categorySpending: List<CategorySpending>,
    totalSpending: Long
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySpending) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (totalSpending > 0 && categorySpending.isNotEmpty()) {
                Canvas(
                    modifier = Modifier.size(200.dp)
                ) {
                    val strokeWidth = 40.dp.toPx()
                    var startAngle = -90f
                    
                    // Calculate total for normalization to ensure 360 degrees
                    val total = categorySpending.sumOf { it.amount }

                    categorySpending.forEachIndexed { index, category ->
                        // For the last segment, use remaining angle to avoid rounding errors
                        val sweepAngle = if (index == categorySpending.size - 1) {
                            // Last segment fills the remaining space
                            (360f - (startAngle + 90f)) * animatedProgress.value
                        } else {
                            (category.amount.toFloat() / total.toFloat()) * 360f * animatedProgress.value
                        }

                        drawArc(
                            color = category.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth)
                        )

                        startAngle += sweepAngle
                    }
                }

                // Center Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${categorySpending.size}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.stats_category_label),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.stats_no_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryLegend(categorySpending: List<CategorySpending>) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (categorySpending.isEmpty()) {
                Text(
                    text = stringResource(R.string.stats_no_category_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                categorySpending.forEachIndexed { index, category ->
                    CategoryLegendItem(
                        category = category,
                        formattedAmount = com.muatrenthenang.resfood.util.CurrencyHelper.format(category.amount)
                    )
                    if (index < categorySpending.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLegendItem(
    category: CategorySpending,
    formattedAmount: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(category.color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Icon
        Icon(
            imageVector = getCategoryIcon(category.name),
            contentDescription = null,
            tint = category.color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Category Name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.displayName,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = stringResource(R.string.stats_item_count_suffix, category.orderCount),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formattedAmount,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PrimaryColor
            )
            Text(
                text = "${String.format("%.1f", category.percentage)}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpendingBarChart(
    data: List<Pair<String, Long>>,
    period: SpendingPeriod
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val maxValue = data.maxOfOrNull { it.second } ?: 1L

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (data.isEmpty() || data.all { it.second == 0L }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.stats_no_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                // Display using horizontal bars for better mobile UX
                data.forEach { (label, value) ->
                    val barProgress = if (maxValue > 0) (value.toFloat() / maxValue.toFloat()) else 0f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Label
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(if (period == SpendingPeriod.WEEK) 40.dp else 48.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = barProgress * animatedProgress.value)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                PrimaryColor.copy(alpha = 0.7f),
                                                PrimaryColor
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Value
                        Text(
                            text = if (value > 0) "${formatter.format(value / 1000)}k" else "0",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(48.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "món chính", "main", "main_course" -> Icons.Default.Restaurant
        "nước uống", "drink", "drinks", "beverage" -> Icons.Default.LocalCafe
        "tráng miệng", "dessert", "desserts" -> Icons.Default.Icecream
        "khai vị", "appetizer", "appetizers" -> Icons.Default.BrunchDining
        else -> Icons.Default.Fastfood
    }
}

@Preview(showBackground = true)
@Composable
private fun SpendingStatisticsScreenPreview() {
    ResFoodTheme {
        SpendingStatisticsScreen()
    }
}
