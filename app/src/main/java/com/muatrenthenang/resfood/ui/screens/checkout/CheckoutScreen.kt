package com.muatrenthenang.resfood.ui.screens.checkout

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.muatrenthenang.resfood.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.theme.AccentOrange
import com.muatrenthenang.resfood.ui.viewmodel.CheckoutViewModel
import com.muatrenthenang.resfood.ui.viewmodel.PaymentMethod
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddresses: () -> Unit = {},
    onPaymentConfirmed: () -> Unit = {},
    vm: CheckoutViewModel = viewModel()
) {
    val context = LocalContext.current
    val address by vm.address.collectAsState()
    val items by vm.items.collectAsState()
    val paymentMethod by vm.paymentMethod.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val result by vm.actionResult.collectAsState()

    // Compute totals from collected state so UI reacts immediately
    // State to toggle Voucher Selection Screen
    var showVoucherSelection by remember { mutableStateOf(false) }

    val availablePromotions by vm.availablePromotions.collectAsState()
    val productVoucher by vm.selectedProductVoucher.collectAsState()
    val shippingVoucher by vm.selectedShippingVoucher.collectAsState()

    // Calculated values
    val subTotal = vm.subTotal()
    val productDiscount = vm.productDiscount()
    val shippingDiscount = vm.shippingDiscount()
    val total = vm.total()

    if (showVoucherSelection) {
        VoucherSelectionScreen(
            currentProductVoucher = productVoucher,
            currentShippingVoucher = shippingVoucher,
            promotions = availablePromotions,
            onApplyVouchers = { pV, sV ->
                vm.setVouchers(pV, sV)
                showVoucherSelection = false
            },
            onNavigateBack = { showVoucherSelection = false }
        )
        return
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(result) {
        result?.let { r ->
            Toast.makeText(context, r, Toast.LENGTH_SHORT).show()
            vm.clearResult()
            if (r.contains("thành công", true)) onPaymentConfirmed()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                ) {

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Thanh toán",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(text = "Tổng thanh toán", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text(text = vm.formatCurrency(total), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { vm.confirmPayment() },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(28.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text(text = "Xác nhận", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus(); keyboardController?.hide() }) }
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Address
            Column {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Địa chỉ nhận hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Thay đổi", color = PrimaryColor, modifier = Modifier.clickable { onNavigateToAddresses() }, fontWeight = FontWeight.SemiBold)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = address.label, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                if (address.isDefault) {
                                    Text(text = "Mặc định", color = PrimaryColor.copy(alpha = 0.58f), fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Text(text = address.getFullAddress(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(text = "${address.contactName} • ${address.phone}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }

            // Order summary
            Column {
                Text(text = "Tóm tắt đơn hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items.forEach { it ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = it.food.imageUrl, contentDescription = it.food.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    Text(text = "x${it.quantity}", color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black.copy(alpha = 0.5f)).padding(3.dp), fontSize = 10.sp, lineHeight = 10.sp,)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = it.food.name,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = vm.formatCurrency((it.food.price).toLong()),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.wrapContentWidth()
                                        )
                                    }
                                    Text(text = "Không hành, nhiều nước béo", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                            }
                        }

                        // Costs
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Tạm tính", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                                Text(text = vm.formatCurrency(subTotal), fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Phí giao hàng", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                                Text(text = vm.formatCurrency(15000L), fontWeight = FontWeight.Medium)
                            }
                            
                            if (productDiscount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Giảm giá món", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    }
                                    Text(text = "-${vm.formatCurrency(productDiscount)}", color = SuccessGreen, fontWeight = FontWeight.Medium)
                                }
                            }

                            if (shippingDiscount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Giảm phí ship", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0097A7), modifier = Modifier.size(14.dp))
                                    }
                                    Text(text = "-${vm.formatCurrency(shippingDiscount)}", color = Color(0xFF0097A7), fontWeight = FontWeight.Medium)
                                }
                            }

                            Divider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Tổng cộng", fontWeight = FontWeight.Bold)
                                Text(text = vm.formatCurrency(total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryColor)
                            }
                        }
                    }
                }
            }

            // Voucher Selection Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { showVoucherSelection = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.5f)),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, tint = PrimaryColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("ResFood Voucher", fontWeight = FontWeight.Medium)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val count = (if (productVoucher != null) 1 else 0) + (if (shippingVoucher != null) 1 else 0)
                            if (count > 0) {
                                Text("Đã chọn $count", color = PrimaryColor, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Chọn hoặc nhập mã", color = Color.Gray, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Payment methods
            Column {
                Text(text = "Phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    PaymentOption(label = "Ví ZaloPay", subtitle = null, drawableRes = R.drawable.ic_zalopay_logo, selected = paymentMethod == PaymentMethod.ZALOPAY, onSelect = { vm.setPaymentMethod(PaymentMethod.ZALOPAY) })
                    PaymentOption(label = "Ví MoMo", subtitle = null, drawableRes = R.drawable.ic_momo_logo, selected = paymentMethod == PaymentMethod.MOMO, onSelect = { vm.setPaymentMethod(PaymentMethod.MOMO) })
                    PaymentOption(label = "Tiền mặt khi nhận hàng", subtitle = null, drawableRes = R.drawable.ic_cash, selected = paymentMethod == PaymentMethod.COD, onSelect = { vm.setPaymentMethod(PaymentMethod.COD) })
                }
            }

            // Security note
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Thanh toán an toàn & bảo mật 100%", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun PaymentOption(label: String, subtitle: String? = null, @androidx.annotation.DrawableRes drawableRes: Int? = null, selected: Boolean = false, onSelect: () -> Unit = {}) {
    val border = if (selected) BorderStroke(2.dp, PrimaryColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    Surface(shape = RoundedCornerShape(12.dp), border = border, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().clickable { onSelect() }) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (drawableRes != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(1.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Image(painter = androidx.compose.ui.res.painterResource(id = drawableRes), contentDescription = label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(text = label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                    if (subtitle != null) Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
            RadioButton(selected = selected, onClick = { onSelect() }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutPreview() {
    CheckoutScreen(onNavigateBack = {}, onPaymentConfirmed = {})
}