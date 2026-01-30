package com.muatrenthenang.resfood.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.viewmodel.admin.AnalyticsFilterType
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    selectedType: AnalyticsFilterType,
    startDate: Long,
    endDate: Long,
    onTypeSelected: (AnalyticsFilterType) -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Segmented Control (3 Parts as requested)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha=0.9f), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterTab(stringResource(R.string.admin_order_filter_date_today), selectedType == AnalyticsFilterType.TODAY, Modifier.weight(1f)) { onTypeSelected(AnalyticsFilterType.TODAY) }
            FilterTab(stringResource(R.string.label_this_month), selectedType == AnalyticsFilterType.MONTH, Modifier.weight(1f)) { onTypeSelected(AnalyticsFilterType.MONTH) }
            FilterTab(stringResource(R.string.label_custom), selectedType == AnalyticsFilterType.CUSTOM, Modifier.weight(1f)) { 
                onTypeSelected(AnalyticsFilterType.CUSTOM)
            }
        }
        
        // Date Display for Custom: TWO BUTTONS
        if (selectedType == AnalyticsFilterType.CUSTOM) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // START DATE
                DateInputBox(
                    label = "Từ ngày",
                    date = startDate,
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // END DATE
                DateInputBox(
                    label = "Đến ngày",
                    date = endDate,
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    // START DATE PICKER
    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newStart = datePickerState.selectedDateMillis
                    if (newStart != null) {
                         var newEnd = endDate
                         if (newStart > endDate) {
                              newEnd = newStart + 86399999
                         }
                         onDateRangeSelected(newStart, newEnd)
                    }
                    showStartPicker = false
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState, title = { Text("Ngày bắt đầu", modifier = Modifier.padding(16.dp)) })
        }
    }

    // END DATE PICKER
    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newEndRaw = datePickerState.selectedDateMillis
                    if (newEndRaw != null) {
                         val newEnd = newEndRaw + 86399999 // Add nearly 24h
                         var newStart = startDate
                         if (newEnd < newStart) {
                             newStart = newEndRaw // Set start to beginning of that day
                         }
                         onDateRangeSelected(newStart, newEnd)
                    }
                    showEndPicker = false
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState, title = { Text("Ngày kết thúc", modifier = Modifier.padding(16.dp)) })
        }
    }
}

@Composable
fun DateInputBox(label: String, date: Long, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarMonth, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Text(
                text = formatter.format(Date(date)),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FilterTab(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RevenueLineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.admin_analytics_no_data), color = Color.Gray)
        }
        return
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 40.dp.toPx()
            val chartWidth = size.width - padding * 2
            val chartHeight = size.height - padding * 2
            
            val maxVal = data.maxOfOrNull { it.second } ?: 1.0
            
            // Draw Axis Lines
            drawLine(Color.Gray, Offset(padding, padding), Offset(padding, size.height - padding), strokeWidth = 2f)
            drawLine(Color.Gray, Offset(padding, size.height - padding), Offset(size.width - padding, size.height - padding), strokeWidth = 2f)
            
            // Draw Grid Lines & Y-Axis Labels (5 steps)
            val steps = 4
            for (i in 0..steps) {
                val y = size.height - padding - (i * chartHeight / steps)
                val value = (maxVal / steps * i).toLong()
                
                drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(padding, y), Offset(size.width - padding, y), strokeWidth = 1f)
                
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${value}k",
                        padding - 10f,
                        y + 10f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }
            }
            
            // Draw Line
            val path = Path()
            val points = data.mapIndexed { index, pair ->
                val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * chartWidth
                val y = size.height - padding - ((pair.second / maxVal) * chartHeight).toFloat()
                Offset(x, y)
            }
            
            points.forEachIndexed { index, point ->
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                // Draw Points
                drawCircle(lineColor, 4.dp.toPx(), point)
            }
            drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx()))
            
            // X-Axis Labels (Skip some if too many)
            val skip = if (data.size > 7) data.size / 7 else 1
            data.forEachIndexed { index, pair ->
                if (index % skip == 0) {
                     val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * chartWidth
                     drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            pair.first,
                            x,
                            size.height - padding + 40f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusPieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    if (data.values.sum() == 0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
             Text(stringResource(R.string.admin_analytics_no_data), color = Color.Gray)
        }
        return
    }

    val total = data.values.sum().toFloat()
    val colors = listOf(
        com.muatrenthenang.resfood.ui.theme.SuccessGreen, // Green - Completed
        com.muatrenthenang.resfood.ui.theme.LightRed, // Red - Cancelled
        com.muatrenthenang.resfood.ui.theme.AccentOrange, // Yellow - Pending/Processing
        com.muatrenthenang.resfood.ui.theme.PrimaryColor  // Blue - Other
    )
    
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Pie Chart
        Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp)) {
                var startAngle = -90f
                data.entries.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.value / total) * 360f
                    val color = when(entry.key) {
                        "COMPLETED" -> colors[0]
                        "REJECTED", "CANCELLED" -> colors[1]
                        "PENDING", "PROCESSING" -> colors[2]
                        else -> colors[3]
                    }
                    
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }
            // Cutout for Donut
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background))
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Legend
        Column {
            data.forEach { (status, count) ->
                val color = when(status) {
                    "COMPLETED" -> colors[0]
                    "REJECTED", "CANCELLED" -> colors[1]
                    "PENDING", "PROCESSING" -> colors[2]
                    else -> colors[3]
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "$status: $count", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
