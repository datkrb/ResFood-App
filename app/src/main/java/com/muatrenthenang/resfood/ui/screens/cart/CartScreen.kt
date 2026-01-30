package com.muatrenthenang.resfood.ui.screens.cart

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.RectangleShape
import com.muatrenthenang.resfood.ui.theme.BgLight
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.TextDark
import com.muatrenthenang.resfood.ui.viewmodel.CartViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.muatrenthenang.resfood.data.model.CartItem


@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onProceedToCheckout: () -> Unit = {},
    onOpenFoodDetail: (String) -> Unit = {},
    viewModel: CartViewModel = viewModel(),
    paddingValuesFromParent: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.actionResult.collectAsState()
    val needLogin by viewModel.needLogin.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingItemId by remember { mutableStateOf<String?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    // Thông báo kết quả
    LaunchedEffect(result) {
        when (result) {
            null -> {}
            else -> {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                viewModel.clearResult()
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(bottom = paddingValuesFromParent.calculateBottomPadding()),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.cart_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = { showClearAllDialog = true },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear",
                        tint = LightRed
                    )
                }
            }
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Column {
                    // Bill summary attached to bottom
                    Surface(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(text = stringResource(R.string.cart_total), fontWeight = FontWeight.Bold)
                                }
                                Text(text = viewModel.formatCurrency(viewModel.subTotal().toLong()), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryColor)
                            }
                        }
                    }

                    // Checkout button
                    Surface(
                        tonalElevation = 3.dp,
                        shadowElevation = 6.dp,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Button(
                                    onClick = { if(viewModel.canCheckout()) {onProceedToCheckout()} },
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = stringResource(R.string.prepare_payment), color = Color.White, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = viewModel.formatCurrency(viewModel.total()),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (needLogin) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(paddingValuesFromParent),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.cart_require_login), fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* TODO: Chuyển hướng đăng nhập */ }, shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                        Text(stringResource(R.string.cart_login_btn), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Items
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.cart_empty), color = Color.Gray, fontSize = 16.sp)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items.forEach { item ->
                            val food = item.food
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                tonalElevation = 1.dp,
                                shadowElevation = 2.dp,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

                                    // Image
                                    Box(modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp)),) {
                                        if (food.imageUrl != null) {
                                            AsyncImage(
                                                model = food.imageUrl,
                                                contentDescription = food.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clickable { onOpenFoodDetail(food.id) }
                                            )
                                        } else {
                                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.fillMaxSize().clickable { onOpenFoodDetail(food.id) })
                                        }

                                        // Selection checkbox (top-left of image)
                                        Box(modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(2.dp)) {
                                            Surface(shape = RectangleShape, color = Color.Black.copy(alpha = 0.75f), modifier = Modifier.size(20.dp).padding(0.dp)) {
                                                // Use Checkbox for accessibility; update ViewModel on change
                                                Checkbox(
                                                    checked = item.isSelected,
                                                    onCheckedChange = { checked -> viewModel.setItemSelected(item.id, checked) },
                                                    modifier = Modifier.padding(1.dp),
                                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryColor, uncheckedColor = Color.White)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = food.name,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 16.sp
                                                )
                                                // Show Toppings
                                                if (item.toppings.isNotEmpty()) {
                                                    Text(
                                                        text = item.toppings.joinToString(", ") { it.name },
                                                        color = Color.Gray,
                                                        fontSize = 12.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Text(
                                                    text = viewModel.formatCurrency(food.price.toLong() + item.toppings.sumOf { it.price }),
                                                    color = PrimaryColor,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            IconButton(
                                                onClick = { deletingItemId = item.id; showDeleteDialog = true }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove",
                                                    tint = Color.Gray
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { viewModel.changeQuantity(item.id, -1) },
                                                modifier = Modifier.size(36.dp)
                                            ){
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(MaterialTheme.colorScheme.outline, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Remove,
                                                        contentDescription = "-",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Text(text = item.quantity.toString(), modifier = Modifier.width(28.dp), fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                            IconButton(
                                                onClick = { viewModel.changeQuantity(item.id, 1) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(PrimaryColor, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "+",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


            }

            // Xác nhận xóa 1 item
            val deletingItem = items.firstOrNull { it.id == deletingItemId }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        deletingItemId = null
                    },
                    title = { Text(text = stringResource(R.string.cart_delete_title)) },
                    text = { Text(text = stringResource(R.string.cart_delete_confirm, deletingItem?.food?.name ?: "")) },
                    confirmButton = {
                        TextButton(onClick = {
                            deletingItemId?.let { viewModel.removeItem(it) }
                            showDeleteDialog = false
                            deletingItemId = null
                        }) {
                            Text(stringResource(R.string.common_delete), color = LightRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            deletingItemId = null
                        }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            if (showClearAllDialog) {
                AlertDialog(
                    onDismissRequest = { showClearAllDialog = false },
                    title = { Text(text = stringResource(R.string.cart_clear_title)) },
                    text = { Text(text = stringResource(R.string.cart_clear_confirm)) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.clearCart()
                            showClearAllDialog = false
                        }) {
                            Text(stringResource(R.string.common_delete), color = LightRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearAllDialog = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    CartScreen(
        onProceedToCheckout = {},
        onNavigateBack = {

        }
    )
}