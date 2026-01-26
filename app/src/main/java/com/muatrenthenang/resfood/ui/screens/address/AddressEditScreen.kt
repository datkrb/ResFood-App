package com.muatrenthenang.resfood.ui.screens.address

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.model.AddressLabels
import androidx.compose.ui.viewinterop.AndroidView
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.viewmodel.AddressViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressEditScreen(
    addressId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToMap: (Double?, Double?) -> Unit = { _, _ -> },
    onSaveSuccess: () -> Unit,
    savedStateHandle: androidx.lifecycle.SavedStateHandle? = null,
    vm: AddressViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditing = addressId != null
    val actionResult by vm.actionResult.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    
    // Form state from ViewModel
    val formState by vm.formState.collectAsState()

    // Dropdown state (UI only)
    var showLabelDropdown by remember { mutableStateOf(false) }

    // Validation errors (UI only)
    var addressLineError by remember { mutableStateOf<String?>(null) }
    var contactNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }

    // Initialize form on first composition
    LaunchedEffect(addressId) {
        if (addressId != null) {
            vm.loadAddressForEdit(addressId)
        } else {
            vm.initNewAddressForm()
        }
    }

    // Handle Map Result from SavedStateHandle
    val pickedLatResult = savedStateHandle?.getLiveData<Double>("picked_lat")?.observeAsState()
    val pickedLngResult = savedStateHandle?.getLiveData<Double>("picked_lng")?.observeAsState()

    LaunchedEffect(pickedLatResult?.value, pickedLngResult?.value) {
        val lat = pickedLatResult?.value
        val lng = pickedLngResult?.value
        if (lat != null && lng != null) {
            // Update location in ViewModel
            vm.updateLocation(lat, lng)
            
            // Clear saved state
            savedStateHandle?.remove<Double>("picked_lat")
            savedStateHandle?.remove<Double>("picked_lng")
        }
    }

    // Handle action result
    LaunchedEffect(actionResult) {
        actionResult?.let { result ->
            if (result.contains("thành công") || result.contains("Đã thêm") || result.contains("Đã cập nhật")) {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                vm.clearResult()
                onSaveSuccess()
            }
        }
    }

    fun validateAndSave() {
        // Reset errors
        addressLineError = null
        contactNameError = null
        phoneError = null
        cityError = null

        // Validate using formState
        var hasError = false

        if (formState.addressLine.isBlank()) {
            addressLineError = "Vui lòng nhập địa chỉ"
            hasError = true
        }
        if (formState.contactName.isBlank()) {
            contactNameError = "Vui lòng nhập tên người nhận"
            hasError = true
        }
        if (formState.phone.isBlank()) {
            phoneError = "Vui lòng nhập số điện thoại"
            hasError = true
        } else if (!formState.phone.matches(Regex("^[0-9+\\s]{10,15}$"))) {
            phoneError = "Số điện thoại không hợp lệ"
            hasError = true
        }
        if (formState.city.isBlank()) {
            cityError = "Vui lòng nhập thành phố"
            hasError = true
        }

        if (hasError) return

        // Save using ViewModel
        vm.saveAddressFromForm()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 2.dp
            ) {
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
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = if (isEditing) "Sửa địa chỉ" else "Thêm địa chỉ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryColor.copy(alpha = 0.25f),
                                    PrimaryColor.copy(alpha = 0.08f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = PrimaryColor
                    )
                }
            }

            // Form fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Label dropdown
                Text(
                    text = "Loại địa chỉ",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                ExposedDropdownMenuBox(
                    expanded = showLabelDropdown,
                    onExpandedChange = { showLabelDropdown = it }
                ) {
                    OutlinedTextField(
                        value = formState.label,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = when (formState.label) {
                                    "Nhà riêng" -> Icons.Default.Home
                                    "Công ty" -> Icons.Default.Work
                                    else -> Icons.Default.LocationOn
                                },
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLabelDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = showLabelDropdown,
                        onDismissRequest = { showLabelDropdown = false }
                    ) {
                        AddressLabels.labels.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (option) {
                                            "Nhà riêng" -> Icons.Default.Home
                                            "Công ty" -> Icons.Default.Work
                                            else -> Icons.Default.LocationOn
                                        },
                                        contentDescription = null,
                                        tint = if (formState.label == option) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                },
                                onClick = {
                                    vm.updateFormField(label = option)
                                    showLabelDropdown = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Contact section (Moved up)
                Text(
                    text = "Thông tin người nhận",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FormTextField(
                    value = formState.contactName,
                    onValueChange = {
                        vm.updateFormField(contactName = it)
                        contactNameError = null
                    },
                    label = "Họ và tên *",
                    placeholder = "Nhập họ và tên",
                    icon = Icons.Default.Person,
                    error = contactNameError,
                    imeAction = ImeAction.Next
                )

                FormTextField(
                    value = formState.phone,
                    onValueChange = {
                        vm.updateFormField(phone = it)
                        phoneError = null
                    },
                    label = "Số điện thoại *",
                    placeholder = "Nhập số điện thoại",
                    icon = Icons.Default.Phone,
                    error = phoneError,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Address section
                Text(
                    text = "Thông tin địa chỉ",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FormTextField(
                    value = formState.addressLine,
                    onValueChange = {
                        vm.updateFormField(addressLine = it)
                        addressLineError = null
                    },
                    label = "Địa chỉ chi tiết *",
                    placeholder = "Số nhà, tên đường...",
                    icon = Icons.Default.Home,
                    error = addressLineError,
                    imeAction = ImeAction.Next
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormTextField(
                        value = formState.ward,
                        onValueChange = { vm.updateFormField(ward = it) },
                        label = "Phường/Xã",
                        placeholder = "Nhập phường/xã",
                        icon = Icons.Default.Place,
                        modifier = Modifier.weight(1f),
                        imeAction = ImeAction.Next
                    )

                    FormTextField(
                        value = formState.district,
                        onValueChange = { vm.updateFormField(district = it) },
                        label = "Quận/Huyện",
                        placeholder = "Nhập quận/huyện",
                        icon = Icons.Default.Place,
                        modifier = Modifier.weight(1f),
                        imeAction = ImeAction.Next
                    )
                }

                FormTextField(
                    value = formState.city,
                    onValueChange = {
                        vm.updateFormField(city = it)
                        cityError = null
                    },
                    label = "Thành phố/Tỉnh *",
                    placeholder = "Nhập thành phố/tỉnh",
                    icon = Icons.Default.LocationCity,
                    error = cityError,
                    imeAction = ImeAction.Next
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Mini Map Preview
                if (formState.latitude != null && formState.longitude != null && formState.latitude != 0.0 && formState.longitude != 0.0) {
                    Log.d("AddressEditScreen", "Latitude: ${formState.latitude}, Longitude: ${formState.longitude}")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    ) {
                        // Initialize osm config if needed (safe to call multiple times)
                        Configuration.getInstance().userAgentValue = context.packageName
                        
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(false) // Disable interaction on mini map
                                    controller.setZoom(15.0)
                                    
                                    // Disable touch to allow clicking the parent Box
                                    setOnTouchListener { _, _ -> true } 
                                }
                            },
                            update = { mapView ->
                                val geoPoint = GeoPoint(formState.latitude!!, formState.longitude!!)
                                mapView.controller.setCenter(geoPoint)
                                
                                // Clean existing markers
                                mapView.overlays.clear()
                                // Add simple marker centered
                                val marker = org.osmdroid.views.overlay.Marker(mapView)
                                marker.position = geoPoint
                                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(marker)
                                mapView.invalidate()
                            }
                        )
                        
                        // Clickable overlay to navigate to map
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                                .clickable { onNavigateToMap(formState.latitude, formState.longitude) }
                        )

                        // Expand/Edit Button Overlay
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            IconButton(onClick = { onNavigateToMap(formState.latitude, formState.longitude) }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Full map", tint = PrimaryColor)
                            }
                        }
                    }
                } else {
                    // Placeholder when no location selected
                    OutlinedButton(
                        onClick = { onNavigateToMap(null, null) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chọn vị trí trên bản đồ",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Default switch
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.updateFormField(isDefault = !formState.isDefault) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (formState.isDefault) SuccessGreen.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (formState.isDefault) SuccessGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Đặt làm địa chỉ mặc định",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Tự động chọn khi đặt hàng",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = formState.isDefault,
                            onCheckedChange = { vm.updateFormField(isDefault = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = { validateAndSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "Cập nhật địa chỉ" else "Lưu địa chỉ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (error != null) MaterialTheme.colorScheme.error else PrimaryColor.copy(alpha = 0.7f)
                )
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else PrimaryColor,
                unfocusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )
    }
}
