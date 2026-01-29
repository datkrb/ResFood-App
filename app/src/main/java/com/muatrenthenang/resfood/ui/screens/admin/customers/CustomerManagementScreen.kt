package com.muatrenthenang.resfood.ui.screens.admin.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToOrders: (String) -> Unit // New navigation parameter
) {
    val customers by viewModel.customers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Filters
    val allFilterLabel = stringResource(R.string.filter_all)
    val vipGoldLabel = stringResource(R.string.rank_gold)
    val vipSilverLabel = stringResource(R.string.rank_silver)
    val vipDiamondLabel = stringResource(R.string.filter_rank_diamond)
    val loyalLabel = stringResource(R.string.filter_rank_loyal)
    val memberLabel = stringResource(R.string.rank_member)
    
    val filterOptions = listOf(allFilterLabel, memberLabel, vipSilverLabel, vipGoldLabel, vipDiamondLabel)
    var selectedFilter by remember { mutableStateOf(allFilterLabel) }
    

    // Dialog States
    var showLockDialog by remember { mutableStateOf(false) }
    var userToLock by remember { mutableStateOf<User?>(null) }
    var selectedCustomerForDetail by remember { mutableStateOf<User?>(null) }
    
    // Filtering Logic
    val filteredCustomers = customers.filter { user ->
        val query = searchQuery.trim().lowercase()
        val matchesSearch = user.fullName.lowercase().contains(query) || (user.phone?.contains(query) == true)
        
        val matchesFilter = when(selectedFilter) {
            allFilterLabel -> true
            vipGoldLabel -> user.rank == "VIP GOLD" || user.rank.equals("Vàng", ignoreCase = true)
            vipSilverLabel -> user.rank == "SILVER" || user.rank.equals("Bạc", ignoreCase = true)
            vipDiamondLabel -> user.rank == "DIAMOND" || user.rank.equals("Kim cương", ignoreCase = true)
            loyalLabel -> user.rank == "LOYAL" || user.rank.equals("Thân thiết", ignoreCase = true)
            memberLabel -> user.rank == "MEMBER" || user.rank.equals("Thành viên", ignoreCase = true)
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

    // Lock Confirmation Dialog
    if (showLockDialog && userToLock != null) {
        val isLocked = userToLock!!.isLocked
        AlertDialog(
            onDismissRequest = { showLockDialog = false },
            title = { Text(if (isLocked) stringResource(R.string.admin_customer_unlock_title) else stringResource(R.string.admin_customer_lock_title)) },
            text = { Text(if (isLocked) stringResource(R.string.admin_customer_unlock_confirm) else stringResource(R.string.admin_customer_lock_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUser(userToLock!!.id, mapOf("isLocked" to !isLocked))
                        Toast.makeText(context, if(isLocked) R.string.admin_customer_unlock_success else R.string.admin_customer_lock_success, Toast.LENGTH_SHORT).show()
                        showLockDialog = false
                        userToLock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isLocked) stringResource(R.string.action_unlock_account) else stringResource(R.string.action_lock_account))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Detail Dialog
    if (selectedCustomerForDetail != null) {
        CustomerDetailDialog(
            customer = selectedCustomerForDetail!!,
            onDismiss = { selectedCustomerForDetail = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_customer_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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

                // Search Bar
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                             Spacer(modifier = Modifier.width(8.dp))
                             androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(stringResource(R.string.admin_customer_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Chips
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { filter ->
                        Chip(
                            text = filter, 
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.admin_customer_list)} (${filteredCustomers.size})",
                        color = MaterialTheme.colorScheme.onBackground, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp
                    )
                    Text(
                        stringResource(R.string.admin_customer_sort), 
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // List
                if (filteredCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.admin_customer_empty), // Ensure this string exists or use hardcoded/generic for now
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f) // Ensure it takes remaining space
                    ) {
                        items(filteredCustomers) { customer ->
                            CustomerItem(
                                customer = customer,
                                onChat = { onNavigateToChat(customer.id) },
                                onLockToggle = { 
                                    userToLock = customer
                                    showLockDialog = true
                                },
                                onViewOrders = { onNavigateToOrders(customer.id) },
                                onDetail = { selectedCustomerForDetail = customer }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, badge: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Text(
                    badge, 
                    color = Color(0xFF4CAF50), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Chip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = contentColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CustomerItem(
    customer: User,
    onChat: () -> Unit,
    onLockToggle: () -> Unit,
    onViewOrders: () -> Unit,
    onDetail: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Background color for locked state
    val cardColor = if (customer.isLocked) {
        MaterialTheme.colorScheme.errorContainer 
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetail() }, // Click to show detail
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                if (customer.avatarUrl != null) {
                    AsyncImage(
                        model = customer.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = customer.fullName.firstOrNull()?.toString()?.uppercase() ?: "U",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Locked Indicator Overlay
                if (customer.isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock, 
                            contentDescription = "Locked", 
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        customer.fullName, 
                        color = MaterialTheme.colorScheme.onSurface, 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (customer.isLocked) {
                        Text(
                            text = stringResource(R.string.admin_customer_locked_msg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        // Rank Badge
                        val (rankText, rankColor) = when {
                            customer.rank.equals("VIP GOLD", ignoreCase = true) || customer.rank.equals("Vàng", ignoreCase = true) -> stringResource(R.string.rank_gold) to Color(0xFFFFC107)
                            customer.rank.equals("SILVER", ignoreCase = true) || customer.rank.equals("Bạc", ignoreCase = true) -> stringResource(R.string.rank_silver) to Color(0xFF9E9E9E)
                            customer.rank.equals("DIAMOND", ignoreCase = true) || customer.rank.equals("Kim cương", ignoreCase = true) -> stringResource(R.string.filter_rank_diamond) to Color(0xFF00BCD4)
                            customer.rank.equals("LOYAL", ignoreCase = true) || customer.rank.equals("Thân thiết", ignoreCase = true) -> stringResource(R.string.filter_rank_loyal) to Color(0xFFE91E63)
                            else -> stringResource(R.string.rank_member) to MaterialTheme.colorScheme.primary
                        }
                        
                        Surface(
                            color = rankColor.copy(alpha = 0.1f),
                            contentColor = rankColor,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = rankText.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Details Row - Removed Points, show phone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Phone, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        customer.phone ?: stringResource(R.string.phone_empty), 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = showMenu, 
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_chat)) },
                        onClick = { showMenu = false; onChat() },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_view_orders)) },
                        onClick = { showMenu = false; onViewOrders() },
                        leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { 
                            Text(
                                if (customer.isLocked) stringResource(R.string.action_unlock_account) else stringResource(R.string.action_lock_account), 
                                color = if (customer.isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ) 
                        },
                        onClick = { showMenu = false; onLockToggle() },
                        leadingIcon = { 
                            Icon(
                                if (customer.isLocked) Icons.Default.LockOpen else Icons.Default.Lock, 
                                contentDescription = null, 
                                tint = if (customer.isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerDetailDialog(
    customer: User,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null, // Custom content
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    if (customer.avatarUrl != null) {
                        AsyncImage(
                            model = customer.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = customer.fullName.firstOrNull()?.toString()?.uppercase() ?: "U",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                
                // Name & Rank
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        customer.fullName, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        customer.rank.uppercase(), 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                // Info Rows
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow(icon = Icons.Default.Email, label = stringResource(R.string.username_hint), value = customer.email)
                    InfoRow(icon = Icons.Default.Phone, label = stringResource(R.string.label_phone), value = customer.phone ?: stringResource(R.string.phone_empty))
                    

                    
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val joinedDate = dateFormat.format(Date(customer.createdAt))
                    InfoRow(icon = Icons.Default.DateRange, label = stringResource(R.string.admin_customer_joined), value = joinedDate)
                    
                    // Spending (if available in User model, else remove or calculate). Assuming totalSpending exists.
                    // If not, we can show Points.
                     InfoRow(
                        icon = Icons.Default.AttachMoney, 
                        label = stringResource(R.string.stats_revenue), 
                        value = "${String.format("%,.0f", customer.totalSpending ?: 0.0)} đ"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
