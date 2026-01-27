package com.muatrenthenang.resfood.ui.screens.admin.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.muatrenthenang.resfood.data.model.Reservation
import com.muatrenthenang.resfood.data.model.Table
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val tables by viewModel.tables.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    
    // Reservations
    val reservations by viewModel.reservations.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadReservations() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showReservationDialog by remember { mutableStateOf(false) }
    var tableToEdit by remember { mutableStateOf<Table?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý bàn & Đặt chỗ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (selectedTab == 0) showAddDialog = true 
                    else showReservationDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.   tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sơ đồ bàn") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Danh sách đặt bàn") }
                )
            }

            if (selectedTab == 0) {
                // Table Map View
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.refreshData() },
                    state = pullRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Legend
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatusLegend(Color(0xFF4CAF50), "Trống")
                            StatusLegend(Color(0xFFF44336), "Đang dùng")
                            StatusLegend(Color(0xFFFF9800), "Đặt trước")
                        }
            
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tables) { table ->
                                TableItem(table, onClick = { tableToEdit = table })
                            }
                        }
                    }
                }
            } else {
                // Reservation View
                if (reservations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có đặt bàn nào", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(reservations) { reservation ->
                            ReservationItem(reservation)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddDialog) {
        TableEditDialog(
            table = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, seats, status -> 
                viewModel.addTable(name, seats)
                showAddDialog = false
            },
            onDelete = {}
        )
    }
    
    if (showReservationDialog) {
        ReservationDialog(
            tables = tables,
            onDismiss = { showReservationDialog = false },
            onSave = { res ->
                viewModel.addReservation(res)
                showReservationDialog = false
            }
        )
    }
    
    if (tableToEdit != null) {
        TableEditDialog(
            table = tableToEdit,
            onDismiss = { tableToEdit = null },
            onSave = { name, seats, status ->
                val updated = tableToEdit!!.copy(name = name, seats = seats, status = status)
                viewModel.updateTable(updated)
                tableToEdit = null
            },
            onDelete = {
                viewModel.deleteTable(tableToEdit!!.id)
                tableToEdit = null
            }
        )
    }
}

@Composable
fun StatusLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun TableItem(table: Table, onClick: () -> Unit) {
    val bgColor = when (table.status) {
        "EMPTY" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        "OCCUPIED" -> Color(0xFFF44336).copy(alpha = 0.2f)
        "RESERVED" -> Color(0xFFFF9800).copy(alpha = 0.2f)
        else -> Color.Gray
    }
    val contentColor = when (table.status) {
        "EMPTY" -> Color(0xFF4CAF50)
        "OCCUPIED" -> Color(0xFFF44336)
        "RESERVED" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.aspectRatio(1f).clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = contentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(table.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${table.seats} ghế", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.background(bgColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    when(table.status){
                        "EMPTY" -> "Trống"
                        "OCCUPIED" -> "Có khách"
                        "RESERVED" -> "Đã đặt"
                        else -> "N/A"
                    },
                    color = contentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableEditDialog(
    table: Table?,
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(table?.name ?: "") }
    var seatsStr by remember { mutableStateOf(table?.seats?.toString() ?: "4") }
    var status by remember { mutableStateOf(table?.status ?: "EMPTY") }
    
    val isEdit = table != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Chỉnh sửa bàn" else "Thêm bàn mới") },
        text = {
            Column {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it },
                    label = { Text("Tên bàn") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = seatsStr, 
                    onValueChange = { seatsStr = it },
                    label = { Text("Số ghế") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Trạng thái:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = status == "EMPTY", onClick = { status = "EMPTY" })
                    Text("Trống")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = status == "OCCUPIED", onClick = { status = "OCCUPIED" })
                    Text("Có khách")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = status == "RESERVED", onClick = { status = "RESERVED" })
                    Text("Đặt trước")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val seats = seatsStr.toIntOrNull() ?: 4
                onSave(name, seats, status)
            }) {
                Text(if (isEdit) "Lưu" else "Thêm")
            }
        },
        dismissButton = {
            if (isEdit) {
                 TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("Xóa")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        }
    )
}

@Composable
fun ReservationItem(reservation: Reservation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.customerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(reservation.phoneNumber, color = Color.Gray, fontSize = 14.sp)
                if (reservation.note.isNotEmpty()) {
                    Text("Note: ${reservation.note}", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Bàn: ${if(reservation.tableName.isNotEmpty()) reservation.tableName else "Chưa gán"}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("${reservation.guestCount} khách", color = Color.Gray, fontSize = 12.sp)
                val date = reservation.time?.toDate()
                if (date != null) {
                    val format = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                    Text(format.format(date), color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDialog(
    tables: List<Table>,
    onDismiss: () -> Unit,
    onSave: (Reservation) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("2") }
    var note by remember { mutableStateOf("") }
    var selectedTable by remember { mutableStateOf<Table?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm đặt bàn mới") },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Tên khách hàng") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     OutlinedTextField(
                        value = guests, onValueChange = { guests = it },
                        label = { Text("Số khách") }, modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedTable != null) selectedTable!!.name else "Chọn bàn")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tables.forEach { table ->
                            DropdownMenuItem(
                                text = { Text(table.name) },
                                onClick = { 
                                    selectedTable = table
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val res = Reservation(
                        customerName = name,
                        phoneNumber = phone,
                        guestCount = guests.toIntOrNull() ?: 1,
                        note = note,
                        tableId = selectedTable?.id ?: "",
                        tableName = selectedTable?.name ?: "",
                        time = com.google.firebase.Timestamp.now()
                    )
                    onSave(res)
                },
                enabled = name.isNotEmpty()
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
