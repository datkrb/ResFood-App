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
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(modifier = Modifier.padding(16.dp)) {
        // Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterTab("Hôm nay", selectedType == AnalyticsFilterType.TODAY) { onTypeSelected(AnalyticsFilterType.TODAY) }
            FilterTab("Tuần này", selectedType == AnalyticsFilterType.WEEK) { onTypeSelected(AnalyticsFilterType.WEEK) }
            FilterTab("Tháng này", selectedType == AnalyticsFilterType.MONTH) { onTypeSelected(AnalyticsFilterType.MONTH) }
            FilterTab("Tùy chọn", selectedType == AnalyticsFilterType.CUSTOM) { 
                onTypeSelected(AnalyticsFilterType.CUSTOM)
                showDatePicker = true
            }
        }
        
        // Date Display for Custom
        if (selectedType == AnalyticsFilterType.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Text(
                    text = "${formatter.format(Date(startDate))} - ${formatter.format(Date(endDate))}",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    // Simplified: picking single date for demo, ideal is DateRangePicker
                    if (selectedDate != null) {
                         // Mocking range selection with single date for simplicity in this demo wrapper
                         // In real app, use rememberDateRangePickerState
                         onDateRangeSelected(selectedDate, selectedDate + 86400000)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FilterTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(85.dp) // Fixed width or weight
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
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
            Text("Không có dữ liệu", color = Color.Gray)
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
             Text("Không có dữ liệu", color = Color.Gray)
        }
        return
    }

    val total = data.values.sum().toFloat()
    val colors = listOf(
        Color(0xFF4CAF50), // Green - Completed
        Color(0xFFF44336), // Red - Cancelled
        Color(0xFFFFC107), // Yellow - Pending/Processing
        Color(0xFF2196F3)  // Blue - Other
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
