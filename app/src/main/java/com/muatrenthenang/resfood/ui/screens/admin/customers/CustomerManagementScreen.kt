package com.muatrenthenang.resfood.ui.screens.admin.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.data.model.User
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }
    val allFilterLabel = stringResource(R.string.filter_all)
    val vipGoldLabel = stringResource(R.string.rank_gold)
    val vipSilverLabel = stringResource(R.string.rank_silver)
    
    val filterOptions = listOf(allFilterLabel, vipGoldLabel, vipSilverLabel)
    var selectedFilter by remember { mutableStateOf(allFilterLabel) }
    
    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    
    // Filtering Logic
    val filteredCustomers = customers.filter { user ->
        val query = searchQuery.trim().lowercase()
        val matchesSearch = user.fullName.lowercase().contains(query) || (user.phone?.contains(query) == true)
        
        val matchesFilter = when(selectedFilter) {
            allFilterLabel -> true
            vipGoldLabel -> user.rank == "VIP GOLD"
            vipSilverLabel -> user.rank == "SILVER"
            else -> true
        }
        
        matchesSearch && matchesFilter
    }

    // Stats Logic
    val totalCustomers = customers.size
    val newCustomers = customers.filter { 
        val diff = System.currentTimeMillis() - it.createdAt
        diff < 30L * 24 * 60 * 60 * 1000
    }.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_customer_title), fontWeight = FontWeight.Bold) },
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
        containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column {
            // Stats Header
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.admin_customer_total),
                    value = "$totalCustomers",
                    badge = stringResource(R.string.admin_customer_member),
                    icon = Icons.Default.Person,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.admin_customer_this_month),
                    value = "+$newCustomers",
                    badge = stringResource(R.string.admin_customer_new),
                    icon = Icons.Default.Add,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search & Filter
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(com.muatrenthenang.resfood.ui.theme.SurfaceCard, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                         Spacer(modifier = Modifier.width(8.dp))
                         androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(stringResource(R.string.admin_customer_search_hint), color = Color.Gray)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filterOptions.forEach { filter ->
                    Chip(
                        text = filter, 
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${stringResource(R.string.admin_customer_list)} ($totalCustomers)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(stringResource(R.string.admin_customer_sort), color = Color(0xFF2196F3))
            }

            // List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCustomers) { customer ->
                    CustomerItem(
                        customer = customer,
                        onChat = { onNavigateToChat(customer.id) },
                        onEdit = { 
                            userToEdit = customer
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteUser(customer.id) }
                    )
                }
            }
            }
        }
    }
    
    if (showEditDialog && userToEdit != null) {
        CustomerEditDialog(
            user = userToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updates ->
                viewModel.updateUser(userToEdit!!.id, updates)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, badge: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(40.dp).background(color.copy(alpha=0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Text(badge, color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xFF4CAF50).copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(horizontal=6.dp, vertical=2.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.Gray, fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Chip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if(isSelected) Color.White else com.muatrenthenang.resfood.ui.theme.SurfaceCard, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = if(isSelected) Color.Black else Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CustomerItem(
    customer: User,
    onChat: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                AsyncImage(model = customer.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                     Text(customer.fullName, color = Color.White, fontWeight = FontWeight.Bold)
                     Text("${customer.points} ${stringResource(R.string.label_points)}", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold) 
                }
                Text(customer.phone ?: stringResource(R.string.phone_empty), color = Color.Gray, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    val badgeColor = when(customer.rank) {
                        "VIP GOLD" -> Color(0xFFFFC107)
                        "SILVER" -> Color(0xFF9E9E9E)
                        "THÀNH VIÊN" -> Color(0xFF607D8B)
                        else -> Color(0xFF4CAF50)
                    }
                    val rankLabel = when(customer.rank) {
                        "VIP GOLD" -> stringResource(R.string.rank_gold)
                        "SILVER" -> stringResource(R.string.rank_silver)
                        "THÀNH VIÊN" -> stringResource(R.string.rank_member)
                        else -> customer.rank
                    }
                    Text(rankLabel, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(badgeColor.copy(alpha=0.2f), RoundedCornerShape(4.dp)).padding(horizontal=4.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• ${stringResource(R.string.admin_customer_joined)}: ${java.text.SimpleDateFormat("MM/yyyy").format(java.util.Date(customer.createdAt))}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_chat)) },
                        onClick = { showMenu = false; onChat() },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_edit)) },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete), color = Color.Red) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerEditDialog(user: User, onDismiss: () -> Unit, onSave: (Map<String, Any>) -> Unit) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var points by remember { mutableStateOf(user.points.toString()) }
    var rank by remember { mutableStateOf(user.rank) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.admin_customer_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text(stringResource(R.string.label_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = points, onValueChange = { points = it }, label = { Text(stringResource(R.string.label_points)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rank, onValueChange = { rank = it }, label = { Text(stringResource(R.string.label_rank_hint)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(mapOf(
                    "fullName" to fullName,
                    "points" to (points.toIntOrNull() ?: 0),
                    "rank" to rank
                ))
            }) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
