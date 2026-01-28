package com.muatrenthenang.resfood.ui.screens.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tapas
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.data.api.ImgBBUploader
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Review
import com.muatrenthenang.resfood.data.repository.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class CategoryItem(val icon: ImageVector, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodEditScreen(
    foodId: String?,
    onNavigateBack: () -> Unit,
    foodRepository: FoodRepository = FoodRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("0") }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var rating by remember { mutableStateOf(0f) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    
    // Image Upload State
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Status State
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Dropdown State
    var categoriesExpanded by remember { mutableStateOf(false) }
    
    // Fixed Categories
    val knownCategories = listOf(
        CategoryItem(icon = Icons.Default.Restaurant, name = "Món chính"),
        CategoryItem(icon = Icons.Default.Tapas, name = "Món phụ"),
        CategoryItem(icon = Icons.Default.EmojiFoodBeverage, name = "Nước uống"),
        CategoryItem(icon = Icons.Default.Fastfood, name = "Tráng miệng")
    )

    // Image Picker
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        
        // Load Food Data if Editing
        if (foodId != null) {
            foodRepository.getFood(foodId).onSuccess { food ->
                name = food.name
                description = food.description
                price = food.price.toString()
                calories = food.calories.toString()
                discountPercent = food.discountPercent.toString()
                imageUrl = food.imageUrl ?: ""
                category = food.category
                isAvailable = food.isAvailable
                rating = food.rating
                reviews = food.reviews
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (foodId != null) "Cập nhật món ăn" else "Thêm món ăn mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(200.dp).clickable { 
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Current Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp))
                                Text("Chọn ảnh từ thư viện")
                            }
                        }
                    }
                }

                // Basic Info
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món ăn") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Giá (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountPercent,
                        onValueChange = { discountPercent = it },
                        label = { Text("Giảm giá (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoriesExpanded,
                    onExpandedChange = { categoriesExpanded = !categoriesExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Danh mục") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriesExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoriesExpanded,
                        onDismissRequest = { categoriesExpanded = false }
                    ) {
                        knownCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(cat.icon, contentDescription = null, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cat.name)
                                    }
                                },
                                onClick = {
                                    category = cat.name
                                    categoriesExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Năng lượng (Kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Đang mở bán", modifier = Modifier.weight(1f))
                    Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            error = null

                            // Validate
                            if (name.isBlank() || price.isBlank()) {
                                error = "Vui lòng nhập tên và giá"
                                isSaving = false
                                return@launch
                            }
                            
                            // 1. Upload Image if changed
                            var finalImageUrl = imageUrl
                            if (selectedImageUri != null) {
                                try {
                                    val url = uploadImageToImgBB(context, selectedImageUri!!)
                                    if (url != null) {
                                        finalImageUrl = url
                                    } else {
                                        error = "Lỗi upload ảnh"
                                        isSaving = false
                                        return@launch
                                    }
                                } catch (e: Exception) {
                                    error = "Lỗi upload: ${e.message}"
                                    isSaving = false
                                    return@launch
                                }
                            }

                            // 2. Create/Update Food Object
                            val food = Food(
                                id = foodId ?: "",
                                name = name,
                                imageUrl = finalImageUrl.ifEmpty { null },
                                price = price.toIntOrNull() ?: 0,
                                discountPercent = discountPercent.toIntOrNull() ?: 0,
                                calories = calories.toIntOrNull() ?: 0,
                                description = description,
                                isAvailable = isAvailable,
                                category = category,
                                rating = rating,
                                reviews = reviews
                            )
                            
                            // 3. Save Food
                            val saveResult = if (foodId != null) {
                                foodRepository.updateFood(food)
                            } else {
                                foodRepository.addFood(food)
                            }
                            
                            saveResult.onSuccess {
                                Toast.makeText(context, "Lưu thành công", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }.onFailure {
                                error = "Lỗi khi lưu data: ${it.message}"
                            }

                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Lưu món ăn")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

suspend fun uploadImageToImgBB(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)
            
            val response = ImgBBUploader.api.uploadImage(ImgBBUploader.getApiKey(), imagePart)
            if (response.success && response.data != null) {
                return@withContext response.data.url
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
