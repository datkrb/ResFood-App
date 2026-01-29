package com.muatrenthenang.resfood.ui.screens.admin.tables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.data.model.TableReservation
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {}
) {
    val reservations by viewModel.reservations.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) { 
        viewModel.loadReservations()
        viewModel.loadCustomers()
    }

    // Tabs matching Order Management style
    val tabs = listOf("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "REJECTED", "ALL")
    val tabTitles = listOf(
        stringResource(R.string.table_status_pending),
        stringResource(R.string.table_status_confirmed),
        stringResource(R.string.table_status_completed),
        stringResource(R.string.table_status_cancelled),
        stringResource(R.string.table_status_rejected),
        stringResource(R.string.filter_all)
    )
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedDateFilter by remember { mutableStateOf<String>("All") }
    var searchQuery by remember { mutableStateOf("") }

    // Filter Logic
    val filteredReservations = reservations.filter { reservation ->
        val selectedStatus = tabs[selectedTabIndex]
        val matchesStatus = when(selectedStatus) {
             "ALL" -> true
             else -> reservation.status == selectedStatus
        }
        
        val matchesDate = when(selectedDateFilter) {
            "Today" -> {
                val diff = System.currentTimeMillis() - (reservation.createdAt?.toDate()?.time ?: 0)
                diff < 24 * 60 * 60 * 1000
            }
            "Week" -> {
                val diff = System.currentTimeMillis() - (reservation.createdAt?.toDate()?.time ?: 0)
                diff < 7 * 24 * 60 * 60 * 1000
            }
            else -> true
        }

        val matchesSearch = if(searchQuery.isBlank()) true else {
            reservation.id.contains(searchQuery, ignoreCase = true) ||
            reservation.branchName.contains(searchQuery, ignoreCase = true) ||
            reservation.note.contains(searchQuery, ignoreCase = true)
        }
        matchesStatus && matchesDate && matchesSearch
    }.sortedByDescending { it.createdAt }

    // State for Reject Dialog
    var showRejectDialog by remember { mutableStateOf(false) }
    var reservationToReject by remember { mutableStateOf<TableReservation?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    if (showRejectDialog && reservationToReject != null) {
        AlertDialog(
            onDismissRequest = { 
                showRejectDialog = false 
                rejectReason = ""
            },
            title = { Text(stringResource(R.string.table_reject_title), fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text(stringResource(R.string.table_reject_confirm_msg, reservationToReject?.id?.takeLast(5)?.uppercase() ?: ""))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Lý do từ chối") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reservationToReject?.let { reservation ->
                            viewModel.rejectReservation(reservation.id, rejectReason) {
                                Toast.makeText(context, context.getString(R.string.table_msg_rejected), Toast.LENGTH_SHORT).show()
                            }
                        }
                        showRejectDialog = false
                        reservationToReject = null
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightRed)
                ) {
                    Text(stringResource(R.string.admin_order_reject_btn), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRejectDialog = false 
                    rejectReason = ""
                }) {
                    Text(stringResource(R.string.common_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.table_mgmt_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadReservations() },
            state = pullRefreshState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                CustomSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                
                // Date Filter Chips
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "All" to stringResource(R.string.filter_all),
                        "Today" to stringResource(R.string.admin_order_filter_date_today), 
                        "Week" to stringResource(R.string.admin_order_filter_date_week)
                    )
                    filters.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedDateFilter == key,
                            onClick = { selectedDateFilter = key },
                            label = { Text(label) }
                        )
                    }
                }

                // Tabs with Badge
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PrimaryColor
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, status ->
                        // Calculate count for this status
                        val count = when(status) {
                            "ALL" -> reservations.size
                            else -> reservations.count { it.status == status }
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
                                        color = if (selectedTabIndex == index) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                    // Badge with count
                                    if (count > 0) {
                                        val badgeColor = when(status) {
                                            "PENDING" -> Color(0xFFF59E0B)
                                            "CONFIRMED" -> Color(0xFF3B82F6)
                                            "COMPLETED" -> SuccessGreen
                                            "CANCELLED" -> Color.Gray
                                            "REJECTED" -> LightRed
                                            else -> MaterialTheme.colorScheme.primary
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

                Spacer(modifier = Modifier.height(16.dp))

                // Reservation List
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header for Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val headerTitle = tabTitles[selectedTabIndex]
                            Text(headerTitle, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${filteredReservations.size} ${stringResource(R.string.unit_orders)}", color = LightRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (filteredReservations.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.table_empty_list), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    items(filteredReservations) { reservation ->
                        val customer = customers.find { it.id == reservation.userId }
                        ReservationItem(
                            reservation = reservation, 
                            userAvatar = customer?.avatarUrl,
                            userName = customer?.fullName ?: "Khách",
                            onClick = { onNavigateToDetail(reservation.id) },
                            onAccept = { 
                                viewModel.approveReservation(reservation.id) {
                                    Toast.makeText(context, context.getString(R.string.table_msg_approved), Toast.LENGTH_SHORT).show()
                                } 
                            },
                            onReject = { 
                                reservationToReject = reservation
                                showRejectDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomSearchBar(query: String, onQueryChange: (String) -> Unit) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(stringResource(R.string.table_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun ReservationItem(
    reservation: TableReservation, 
    userAvatar: String? = null,
    userName: String,
    onClick: () -> Unit, 
    onAccept: () -> Unit, 
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    // Avatar
                    if (userAvatar != null) {
                        AsyncImage(
                            model = userAvatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.toString()?.uppercase() ?: "K",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(userName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        val date = if(reservation.createdAt != null) {
                            SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(reservation.createdAt.toDate())
                        } else stringResource(R.string.admin_order_just_now)
                        Text(date + " • #" + reservation.id.takeLast(6).uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                
                // Status Badge
                val (badgeColor, badgeText) = when(reservation.status) {
                    "PENDING" -> Color(0xFFF59E0B) to stringResource(R.string.table_status_pending)
                    "CONFIRMED" -> Color(0xFF3B82F6) to stringResource(R.string.table_status_confirmed)
                    "COMPLETED" -> SuccessGreen to stringResource(R.string.table_status_completed)
                    "CANCELLED" -> Color.Gray to stringResource(R.string.table_status_cancelled)
                    "REJECTED" -> LightRed to stringResource(R.string.table_status_rejected)
                    else -> Color.Gray to reservation.status
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    val statusLabel = when(reservation.status) {
                        "PENDING" -> stringResource(R.string.admin_order_status_short_pending)
                        "CONFIRMED" -> stringResource(R.string.admin_order_status_short_processing) // Adjusted mapping if needed
                        "COMPLETED" -> stringResource(R.string.admin_order_status_short_completed)
                        "CANCELLED" -> stringResource(R.string.admin_order_status_short_cancelled)
                        "REJECTED" -> stringResource(R.string.admin_order_status_short_rejected)
                        else -> reservation.status
                    }
                    Text(statusLabel, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Reservation details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.table_label_branch), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(reservation.branchName.ifEmpty { stringResource(R.string.admin_branch_unknown) }, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.table_label_guests), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(stringResource(R.string.table_guests_format, reservation.guestCountAdult, reservation.guestCountChild), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.table_label_time), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    val timeFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                    Text(timeFormat.format(reservation.timeSlot.toDate()), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (reservation.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.table_label_note, reservation.note), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer: Button Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.table_btn_view_detail), color = PrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onClick() })

                if (reservation.status == "PENDING") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reject Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { onReject() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.LightGray)
                        }
                        
                        // Approve Button
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(stringResource(R.string.admin_order_approve_btn))
                        }
                    }
                }
            }
        }
    }
}
