package com.muatrenthenang.resfood.ui.screens.admin.marketing

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionAddScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    var promoName by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var discountValue by remember { mutableStateOf("") } // Int
    var discountType by remember { mutableStateOf(0) } // 0: %, 1: VND
    var minOrderValue by remember { mutableStateOf("") } // Int
    var maxDiscountValue by remember { mutableStateOf("") } // Int
    var isActive by remember { mutableStateOf(true) }
    
    // New Fields
    var applyFor by remember { mutableStateOf("ALL") } // ALL, SHIP
    var isPublic by remember { mutableStateOf(true) } // Public vs Private
    var totalQuantity by remember { mutableStateOf("") } // For Public
    var assignedUserIds by remember { mutableStateOf<List<String>>(emptyList()) } // For Private
    var userQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) } // For Private (simplified to 1 per user or global input)
    
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000L * 7) } // +7 days

    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    
    var showUserSelectionDialog by remember { mutableStateOf(false) }

    fun generateCode() {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        promoCode = (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
    
    fun showDatePicker(initialDate: Long, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = initialDate
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                onDateSelected(cal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun savePromotion() {
        if(promoName.isBlank() || promoCode.isBlank() || discountValue.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tên, mã và giá trị giảm", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dValue = discountValue.toIntOrNull() ?: 0
        val minOrder = minOrderValue.toIntOrNull() ?: 0
        val maxDiscount = maxDiscountValue.toIntOrNull() ?: 0
        val quantity = totalQuantity.toIntOrNull() ?: 0
        
        // Private check
        if (!isPublic && assignedUserIds.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn khách hàng áp dụng (Private)", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Construct User Quantities map (default 1 per user for now, or use totalQuantity as quota)
        // If Private, we can just say each user gets 'quantity' vouchers, or 1. Let's assume 1 for simplicity or use the input.
        val privateQuantity = if (quantity > 0) quantity else 1
        val userQtyMap = if (!isPublic) {
            assignedUserIds.associateWith { privateQuantity }
        } else {
            emptyMap()
        }

        val promotion = Promotion(
            name = promoName,
            code = promoCode,
            discountType = discountType,
            discountValue = dValue,
            minOrderValue = minOrder,
            maxDiscountValue = maxDiscount,
            startDate = Timestamp(Date(startDate)),
            endDate = Timestamp(Date(endDate)),
            isActive = isActive,
            applyFor = applyFor,
            assignedUserIds = if (isPublic) emptyList() else assignedUserIds,
            totalQuantity = if (isPublic) quantity else 0,
            userQuantities = userQtyMap
        )

        viewModel.addPromotion(promotion)
        Toast.makeText(context, "Đã lưu khuyến mãi", Toast.LENGTH_SHORT).show()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm khuyến mãi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = { savePromotion() }) {
                        Text("Lưu", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2126),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1E2126),
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { savePromotion() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Lưu chương trình")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Name
            InputSection(title = "Tên chương trình khuyến mãi") {
                AdminTextField(
                    value = promoName,
                    onValueChange = { promoName = it },
                    placeholder = "Ví dụ: Chào hè sôi động"
                )
            }

            // Code
            InputSection(title = "Mã giảm giá") {
                AdminTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it.uppercase() },
                    placeholder = "Ví dụ: HE2024",
                    trailingIcon = { 
                        IconButton(onClick = { generateCode() }) {
                           Icon(Icons.Default.Refresh, contentDescription = "Gen", tint = Color(0xFF2196F3)) 
                        }
                    }
                )
            }
            
            // Public / Private Toggle
            InputSection(title = "Đối tượng áp dụng") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF2C3038), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    TabButton(text = "Công khai (Public)", isSelected = isPublic, modifier = Modifier.weight(1f)) { isPublic = true }
                    TabButton(text = "Riêng tư (Private)", isSelected = !isPublic, modifier = Modifier.weight(1f)) { isPublic = false }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isPublic) {
                    Text("Số lượng voucher (0 = không giới hạn)", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    AdminTextField(value = totalQuantity, onValueChange = { totalQuantity = it }, placeholder = "Nhập số lượng tổng")
                } else {
                    Button(
                        onClick = { showUserSelectionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3038)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Chọn khách hàng (${assignedUserIds.size})", color = Color.White)
                    }
                    if (assignedUserIds.isNotEmpty()) {
                        Text("Số lượng mỗi khách hàng được dùng", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top=12.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = totalQuantity, onValueChange = { totalQuantity = it }, placeholder = "Ví dụ: 1")
                    }
                }
            }

            // Discount Value & Type
            InputSection(title = "Loại giảm giá") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF2C3038), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    TabButton(text = "Theo phần trăm (%)", isSelected = discountType == 0, modifier = Modifier.weight(1f)) { discountType = 0 }
                    TabButton(text = "Theo số tiền (VND)", isSelected = discountType == 1, modifier = Modifier.weight(1f)) { discountType = 1 }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Giá trị giảm", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                AdminTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it },
                    placeholder = "Nhập giá trị",
                    trailingIcon = { Text(if(discountType == 0) "%" else "đ", color = Color.Gray, modifier = Modifier.padding(end=16.dp)) }
                )
            }

            // Conditions
            InputSection(title = "Điều kiện áp dụng") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Đơn tối thiểu", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = minOrderValue, onValueChange = { minOrderValue = it }, placeholder = "0đ")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Giảm tối đa", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = maxDiscountValue, onValueChange = { maxDiscountValue = it }, placeholder = "Không giới hạn")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Apply For Scope (Toggle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF2C3038), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabButton(text = "Đơn hàng", isSelected = applyFor == "ALL", modifier = Modifier.weight(1f)) { applyFor = "ALL" }
                    TabButton(text = "Phí vận chuyển", isSelected = applyFor == "SHIP", modifier = Modifier.weight(1f)) { applyFor = "SHIP" }
                }
            }

            // Time
            InputSection(title = "Thời gian áp dụng") {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                TimeRow("BẮT ĐẦU", dateFormat.format(Date(startDate))) {
                    showDatePicker(startDate) { startDate = it }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TimeRow("KẾT THÚC", dateFormat.format(Date(endDate)), isRed = false) {
                    showDatePicker(endDate) { endDate = it }
                }
            }
            
            // Activate Toggle
            Card(
                 colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
                 shape = RoundedCornerShape(12.dp),
                 modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     Column {
                            Text("Kích hoạt ngay", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Hiển thị khuyến mãi", color = Color.Gray, fontSize = 12.sp)
                     }
                     Switch(checked = isActive, onCheckedChange = { isActive = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2196F3)))
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    // User Selection Dialog (Checklist)
    if (showUserSelectionDialog) {
        UserSelectionDialog(
            customers = customers,
            selectedIds = assignedUserIds,
            onDismiss = { showUserSelectionDialog = false },
            onConfirm = { ids ->
                assignedUserIds = ids
                showUserSelectionDialog = false
            }
        )
    }
}

@Composable
fun UserSelectionDialog(
    customers: List<User>,
    selectedIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedIds.toSet()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            (it.phone?.contains(searchQuery) == true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier
                .fillMaxWidth()
                .height(600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2126))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Chọn khách hàng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))
                
                // Search Bar
                AdminTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Tìm tên, SĐT, email...",
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        }
                    }
                )
                
                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.weight(1f)) {
                    item {
                        val allVisibleSelected = filteredCustomers.isNotEmpty() && filteredCustomers.all { tempSelected.contains(it.id) }
                        
                        Row(
                            Modifier.fillMaxWidth().clickable { 
                                if (allVisibleSelected) {
                                    tempSelected = tempSelected - filteredCustomers.map { it.id }.toSet()
                                } else {
                                    tempSelected = tempSelected + filteredCustomers.map { it.id }
                                }
                            }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             Checkbox(
                                checked = allVisibleSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkmarkColor = Color.White, checkedColor = Color(0xFF2196F3))
                             )
                             Text("Chọn tất cả (${filteredCustomers.size})", color = Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                        HorizontalDivider(color = Color.Gray.copy(alpha=0.3f))
                    }
                    items(filteredCustomers) { user ->
                        val isSelected = tempSelected.contains(user.id)
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                tempSelected = if (isSelected) tempSelected - user.id else tempSelected + user.id
                            }.padding(8.dp),
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkmarkColor = Color.White, checkedColor = Color(0xFF2196F3))
                            )
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(user.fullName, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(if(!user.phone.isNullOrEmpty()) "${user.phone} - ${user.email}" else user.email, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Button(
                        onClick = { onConfirm(tempSelected.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) { Text("Xác nhận") }
                }
            }
        }
    }
}

@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF2C3038),
            unfocusedContainerColor = Color(0xFF2C3038),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF2196F3),
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color(0xFF2196F3)
        ),
        placeholder = { Text(placeholder, color = Color.Gray) },
        trailingIcon = trailingIcon,
        singleLine = true
    )
}

@Composable
fun InputSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF1E2126) else Color.Transparent) // Darker for selected
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if(isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TimeRow(label: String, value: String, isRed: Boolean = false, onClick: () -> Unit) {
    Card(
         colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
         shape = RoundedCornerShape(12.dp),
         modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(modifier = Modifier.size(40.dp).background(if(isRed) Color(0xFFFF5252).copy(alpha=0.1f) else Color(0xFF2196F3).copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                 Icon(Icons.Default.CalendarToday, contentDescription = null, tint = if(isRed) Color(0xFFFF5252) else Color(0xFF2196F3), modifier = Modifier.size(20.dp))
             }
             Spacer(modifier = Modifier.width(16.dp))
             Column {
                 Text(label, color = Color.Gray, fontSize = 10.sp)
                 Text(value, color = Color.White, fontWeight = FontWeight.Bold)
             }
        }
    }
}
