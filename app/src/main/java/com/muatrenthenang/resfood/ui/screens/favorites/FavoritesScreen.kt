package com.muatrenthenang.resfood.ui.screens.favorites

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.BgLight
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.ui.viewmodel.FavoritesViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.tooling.preview.Preview
import com.muatrenthenang.resfood.ui.theme.LightRed

@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onAddToCart: (String) -> Unit = {},
    onLogin: () -> Unit = {},
    onOpenFoodDetail: (String) -> Unit = {},
    paddingValuesFromParent: PaddingValues = PaddingValues(),
    vm: FavoritesViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val items by vm.items.collectAsState()
    val result by vm.actionResult.collectAsState()
    val needLogin by vm.needLogin.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    // Remove confirmation state
    val showRemoveDialog = remember { mutableStateOf(false) }
    val removingItemId = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(result) {
        result?.let { r ->
            Toast.makeText(context, r, Toast.LENGTH_SHORT).show()
            vm.clearResult()
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }

                Text(
                    text = "Món ăn yêu thích",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { paddingValues ->
        if (needLogin) {
            // Hiển thị thông báo và nút đăng nhập
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = paddingValuesFromParent.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bạn cần đăng nhập để xem danh sách yêu thích.", fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLogin, shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                        Text("Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = paddingValuesFromParent.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .padding(paddingValues)
                .padding(bottom = paddingValuesFromParent.calculateBottomPadding())) {

                // Search bar (sticky mimic: placed at top of scroll content)
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Tìm kiếm món ăn...", color = Color.Gray) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // Content list
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bạn chưa có món ăn yêu thích nào!", color = Color.Gray, fontSize = 16.sp)
                    }
                } else {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items.forEach { item ->
                            val food = item.food
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp)).clickable { /* open detail */ }
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f/9f)
                                        .clip(RoundedCornerShape(12.dp))) {
                                        if (food.imageUrl != null) {
                                            AsyncImage(
                                                model = food.imageUrl,
                                                contentDescription = food.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable { onOpenFoodDetail(food.id) }
                                            )
                                        }
                                        // gradient & badges
                                        Box(modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Transparent)) {}

                                        Row(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFF59D)) {
                                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = "${food.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.6f)) {
                                                Text(text = "${food.reviews.size} đánh giá", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                            }
                                        }

                                        // Favorite button overlay (confirm before removing)
                                        IconButton(onClick = { removingItemId.value = food.id; showRemoveDialog.value = true }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(36.dp).background(Color.Black.copy(alpha = 0.35f), shape = CircleShape)) {
                                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = LightRed)
                                        }
                                    }

                                    // Details
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = food.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                // Có thể thêm thông tin khác nếu muốn
                                            }

                                            Text(text = vm.formatCurrency(food.price), color = PrimaryColor, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Action button
                                        if (!food.isAvailable) {
                                            Button(onClick = { /* disabled */ }, enabled = false, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDF2F7), disabledContainerColor = Color(0xFFEDF2F7)), shape = RoundedCornerShape(999.dp)) {
                                                Icon(imageVector = Icons.Default.Block, contentDescription = null)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = "Tạm hết", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                        } else {
                                            Button(onClick = { vm.addToCart(food.id); onAddToCart(food.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                                                Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = "Thêm vào giỏ", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                // end if-else
            }

                // Confirmation dialog for removing favorite
                if (showRemoveDialog.value) {
                    val itemToRemove = items.firstOrNull { it.food.id == removingItemId.value }
                    AlertDialog(
                        onDismissRequest = { showRemoveDialog.value = false; removingItemId.value = null },
                        title = { Text(text = "Xóa khỏi yêu thích") },
                        text = { Text(text = "Bạn có chắc chắn muốn xóa \"${itemToRemove?.food?.name ?: ""}\" khỏi danh sách yêu thích?") },
                        confirmButton = {
                            TextButton(onClick = {
                                removingItemId.value?.let { vm.removeFavorite(it) }
                                showRemoveDialog.value = false
                                removingItemId.value = null
                            }, colors = ButtonDefaults.textButtonColors(contentColor = LightRed)) {
                                Text("Xóa")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRemoveDialog.value = false; removingItemId.value = null }) {
                                Text("Hủy")
                            }
                        }
                    )
                }
            }
        }
    }

@Preview(showBackground = true)
@Composable
fun FavoritesPreview() {
    FavoritesScreen(
        onNavigateBack = {},
        onLogin = {},
        onAddToCart = {}
    )
}