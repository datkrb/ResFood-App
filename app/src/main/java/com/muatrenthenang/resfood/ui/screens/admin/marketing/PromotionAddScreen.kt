package com.muatrenthenang.resfood.ui.screens.admin.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Percent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionAddScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    var promoName by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var discountValue by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf(0) } // 0: %, 1: VND
    var minOrderValue by remember { mutableStateOf("") }
    var maxDiscountValue by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    fun generateCode() {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        promoCode = (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
    
    fun savePromotion() {
        if(promoName.isBlank() || promoCode.isBlank() || discountValue.isBlank()) {
            android.widget.Toast.makeText(context, "Vui lòng nhập đủ thông tin", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val value = discountValue.toIntOrNull() ?: 0
        viewModel.addPromotion(promoName, promoCode, value, discountType)
        android.widget.Toast.makeText(context, "Đã lưu khuyến mãi", android.widget.Toast.LENGTH_SHORT).show()
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
                    placeholder = "Nhập tên chương trình"
                )
            }

            // Code
            InputSection(title = "Mã giảm giá") {
                AdminTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it },
                    placeholder = "Nhập mã...",
                    trailingIcon = { 
                        IconButton(onClick = { generateCode() }) {
                           Icon(Icons.Default.Refresh, contentDescription = "Gen", tint = Color(0xFF2196F3)) 
                        }
                    }
                )
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
                Text("Nhập số phần trăm muốn giảm cho đơn hàng.", color = Color.Gray, fontSize = 12.sp)
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
                
                // Apply For Scope
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
                     shape = RoundedCornerShape(12.dp),
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFF2196F3).copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF2196F3))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Áp dụng cho", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Tất cả món ăn", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Time
            InputSection(title = "Thời gian áp dụng") {
                TimeRow("BẮT ĐẦU", "10/06/2024 - 08:00")
                Spacer(modifier = Modifier.height(12.dp))
                TimeRow("KẾT THÚC", "15/06/2024 - 23:59", isRed = false) // Changed logic if needed
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
                            Text("Hiển thị khuyến mãi trên ứng dụng", color = Color.Gray, fontSize = 12.sp)
                     }
                     Switch(checked = isActive, onCheckedChange = { isActive = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2196F3)))
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
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
fun TimeRow(label: String, value: String, isRed: Boolean = false) {
    Card(
         colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
         shape = RoundedCornerShape(12.dp),
         modifier = Modifier.fillMaxWidth()
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
