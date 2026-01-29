package com.muatrenthenang.resfood.ui.screens.me.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.TableReservation
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.viewmodel.ReservationManagementViewModel
import com.muatrenthenang.resfood.ui.viewmodel.ReservationUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReservationListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    initialTab: String = "ALL",
    viewModel: ReservationManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadReservations()
    }
    
    // Tabs: PENDING, CONFIRMED, COMPLETED, CANCELLED, ALL
    val tabs = listOf("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "ALL")
    val tabTitles = listOf(
        stringResource(R.string.me_status_pending),
        stringResource(R.string.me_status_confirmed),
        stringResource(R.string.me_status_completed),
        stringResource(R.string.order_status_cancelled), // Assuming this exists or using common_cancel if not, but checkout uses it.
        stringResource(R.string.common_all)
    )
    
    // Determine initial index
    var selectedTabIndex by remember { 
        mutableStateOf(
            when(initialTab.uppercase()) {
                "PENDING" -> 0
                "CONFIRMED" -> 1
                "COMPLETED" -> 2
                "CANCELLED" -> 3
                "ALL" -> 4
                else -> 4
            }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                // Custom Top Bar with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        modifier = Modifier
                            .clickable { onNavigateBack() }
                            .padding(8.dp)
                    )
                    Text(
                        text = stringResource(R.string.res_my_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Scrollable Tab Row with Badge
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryColor,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = PrimaryColor
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, status ->
                        // Get count from state
                        val counts = (uiState as? ReservationUiState.Success)?.counts ?: emptyMap()
                        val count = when(status) {
                            "ALL" -> (uiState as? ReservationUiState.Success)?.reservations?.size ?: 0
                            else -> counts[status] ?: 0
                        }
                        
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tabTitles[index],
                                        color = if (selectedTabIndex == index) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                    // Badge with count
                                    if (count > 0) {
                                        val badgeColor = when(status) {
                                            "PENDING" -> Color(0xFFF59E0B) // Orange
                                            "CONFIRMED" -> Color(0xFF3B82F6) // Blue
                                            "COMPLETED" -> SuccessGreen
                                            "CANCELLED" -> Color.Gray
                                            else -> PrimaryColor
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(badgeColor.copy(alpha = if (selectedTabIndex == index) 1f else 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (count > 99) "99+" else count.toString(),
                                                color = if (selectedTabIndex == index) Color.White else badgeColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ReservationUiState.Loading -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator(color = PrimaryColor)
                     }
                }
                is ReservationUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.common_error_with_msg, state.message), color = Color.Red)
                    }
                }
                is ReservationUiState.Success -> {
                    val filteredList = viewModel.getFilteredReservations(tabs[selectedTabIndex])
                    
                    if (filteredList.isEmpty()) {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.res_empty), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredList) { reservation ->
                                ReservationItem(
                                    reservation = reservation,
                                    onClick = { onNavigateToDetail(reservation.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationItem(
    reservation: TableReservation,
    onClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val statusColor = when (reservation.status) {
        "PENDING" -> Color(0xFFF59E0B) // Orange
        "CONFIRMED" -> Color(0xFF3B82F6) // Blue
        "COMPLETED" -> SuccessGreen
        "CANCELLED" -> Color.Gray
        else -> Color.Gray
    }
    
    val statusText = when (reservation.status) {
        "PENDING" -> stringResource(R.string.me_status_pending)
        "CONFIRMED" -> stringResource(R.string.me_status_confirmed)
        "COMPLETED" -> stringResource(R.string.me_status_completed)
        "CANCELLED" -> stringResource(R.string.order_status_cancelled)
        else -> reservation.status
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp, // Added more shadow
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(2.dp) // Padding to show shadow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = reservation.branchName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatter.format(reservation.timeSlot.toDate()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.res_guests_label, reservation.guestCountAdult, reservation.guestCountChild),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.res_detail_btn),
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryColor
                )
            }
        }
    }
}
