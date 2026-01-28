package com.muatrenthenang.resfood.ui.screens.admin.marketing

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    viewModel: AdminViewModel = viewModel(),
    promotionToEdit: Promotion? = null
) {
    // If editing, use existing values, otherwise defaults
    var promoName by remember { mutableStateOf(promotionToEdit?.name ?: "") }
    var promoCode by remember { mutableStateOf(promotionToEdit?.code ?: "") }
    var discountValue by remember { mutableStateOf(promotionToEdit?.discountValue?.toString() ?: "") }
    var discountType by remember { mutableStateOf(promotionToEdit?.discountType ?: 0) } // 0: %, 1: VND
    var minOrderValue by remember { mutableStateOf(promotionToEdit?.minOrderValue?.toString() ?: "") }
    var maxDiscountValue by remember { mutableStateOf(promotionToEdit?.maxDiscountValue?.toString() ?: "") }
    var isActive by remember { mutableStateOf(promotionToEdit?.isActive ?: true) }
    
    // New Fields
    var applyFor by remember { mutableStateOf(promotionToEdit?.applyFor ?: "ALL") } // ALL, SHIP
    var isPublic by remember { mutableStateOf(promotionToEdit?.isPublic() ?: true) } // Public vs Private
    var totalQuantity by remember { mutableStateOf(if (promotionToEdit != null && (promotionToEdit.totalQuantity > 0 || promotionToEdit.userQuantities.isNotEmpty())) {
        if(promotionToEdit.isPublic()) promotionToEdit.totalQuantity.toString()
        else promotionToEdit.userQuantities.values.firstOrNull()?.toString() ?: ""
    } else "") } 
    
    var assignedUserIds by remember { mutableStateOf(promotionToEdit?.assignedUserIds ?: emptyList()) } // For Private
    var userQuantities by remember { mutableStateOf(promotionToEdit?.userQuantities ?: emptyMap()) } // For Private
    
    var startDate by remember { mutableStateOf(promotionToEdit?.startDate?.toDate()?.time ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(promotionToEdit?.endDate?.toDate()?.time ?: (System.currentTimeMillis() + 86400000L * 7)) }

    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    
    var showUserSelectionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
        
        // User Quantities map logic
        val privateQuantity = if (quantity > 0) quantity else 1
        val userQtyMap = if (!isPublic) {
             assignedUserIds.associateWith { privateQuantity }
        } else {
            emptyMap()
        }

        val newPromotion = Promotion(
            id = promotionToEdit?.id ?: "", // Preserve ID if editing
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
            userQuantities = userQtyMap,
            usedByUserIds = promotionToEdit?.usedByUserIds ?: emptyList() // Preserve usage history
        )

        if (promotionToEdit != null && promotionToEdit.id.isNotEmpty()) {
            viewModel.updatePromotion(newPromotion)
            Toast.makeText(context, "Đã cập nhật khuyến mãi", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addPromotion(newPromotion)
            Toast.makeText(context, "Đã tạo khuyến mãi mới", Toast.LENGTH_SHORT).show()
        }
        onNavigateBack()
    }
    
    fun deletePromotion() {
        if (promotionToEdit != null) {
             viewModel.deletePromotion(promotionToEdit.id)
             Toast.makeText(context, "Đã xóa khuyến mãi", Toast.LENGTH_SHORT).show()
             onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (promotionToEdit != null) "Chi tiết khuyến mãi" else "Thêm khuyến mãi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (promotionToEdit != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { savePromotion() }) {
                        Text(if (promotionToEdit != null) "Cập nhật" else "Lưu", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { savePromotion() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(if (promotionToEdit != null) "Cập nhật chương trình" else "Lưu chương trình", color = MaterialTheme.colorScheme.onPrimary)
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
                           Icon(Icons.Default.Refresh, contentDescription = "Gen", tint = MaterialTheme.colorScheme.primary) 
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
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    TabButton(text = "Công khai (Public)", isSelected = isPublic, modifier = Modifier.weight(1f)) { isPublic = true }
                    TabButton(text = "Riêng tư (Private)", isSelected = !isPublic, modifier = Modifier.weight(1f)) { isPublic = false }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isPublic) {
                    Text("Số lượng voucher (0 = không giới hạn)", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    AdminTextField(value = totalQuantity, onValueChange = { totalQuantity = it }, placeholder = "Nhập số lượng tổng")
                } else {
                    Button(
                        onClick = { showUserSelectionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Chọn khách hàng (${assignedUserIds.size})", color = MaterialTheme.colorScheme.primary)
                    }
                    if (assignedUserIds.isNotEmpty()) {
                        Text("Số lượng mỗi khách hàng được dùng", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, modifier = Modifier.padding(top=12.dp))
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
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    TabButton(text = "Theo phần trăm (%)", isSelected = discountType == 0, modifier = Modifier.weight(1f)) { discountType = 0 }
                    TabButton(text = "Theo số tiền (VND)", isSelected = discountType == 1, modifier = Modifier.weight(1f)) { discountType = 1 }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Giá trị giảm", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                AdminTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it },
                    placeholder = "Nhập giá trị",
                    trailingIcon = { Text(if(discountType == 0) "%" else "đ", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end=16.dp)) }
                )
            }

            // Conditions
            InputSection(title = "Điều kiện áp dụng") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Đơn tối thiểu", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = minOrderValue, onValueChange = { minOrderValue = it }, placeholder = "0đ")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Giảm tối đa", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
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
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f), RoundedCornerShape(12.dp))
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
                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                 border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f)),
                 shape = RoundedCornerShape(12.dp),
                 modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     Column {
                            Text("Kích hoạt ngay", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("Hiển thị khuyến mãi", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f), fontSize = 12.sp)
                     }
                     Switch(
                        checked = isActive, 
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha=0.2f)
                        )
                     )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa mã khuyến mãi này không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                     onClick = { 
                         deletePromotion()
                         showDeleteConfirm = false 
                     },
                     colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Chọn khách hàng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
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
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                colors = CheckboxDefaults.colors(checkmarkColor = MaterialTheme.colorScheme.onPrimary, checkedColor = MaterialTheme.colorScheme.primary)
                             )
                             Text("Chọn tất cả (${filteredCustomers.size})", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                                colors = CheckboxDefaults.colors(checkmarkColor = MaterialTheme.colorScheme.onPrimary, checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(user.fullName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Text(if(!user.phone.isNullOrEmpty()) "${user.phone} - ${user.email}" else user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Hủy", color = MaterialTheme.colorScheme.primary) }
                    Button(
                        onClick = { onConfirm(tempSelected.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Xác nhận", color = MaterialTheme.colorScheme.onPrimary) }
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
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f)) },
        trailingIcon = trailingIcon,
        singleLine = true
    )
}

@Composable
fun InputSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            .background(if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent) // Highlight
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, 
            color = if(isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 13.sp, 
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TimeRow(label: String, value: String, isRed: Boolean = false, onClick: () -> Unit) {
    Card(
         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f)),
         shape = RoundedCornerShape(12.dp),
         modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(
                 modifier = Modifier.size(40.dp).background(
                    if(isRed) MaterialTheme.colorScheme.error.copy(alpha=0.1f) else MaterialTheme.colorScheme.primary.copy(alpha=0.1f), 
                    CircleShape
                 ), 
                 contentAlignment = Alignment.Center
             ) {
                 Icon(
                    Icons.Default.CalendarToday, 
                    contentDescription = null, 
                    tint = if(isRed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(20.dp)
                 )
             }
             Spacer(modifier = Modifier.width(16.dp))
             Column {
                 Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                 Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
             }
        }
    }
}
