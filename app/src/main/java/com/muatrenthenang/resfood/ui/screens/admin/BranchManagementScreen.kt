package com.muatrenthenang.resfood.ui.screens.admin

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.viewmodel.BranchViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMap: (Double?, Double?) -> Unit = { _, _ -> },
    savedStateHandle: androidx.lifecycle.SavedStateHandle? = null,
    viewModel: BranchViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()

    // GPS State (local UI state)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isGettingLocation by remember { mutableStateOf(false) }

    // Permission launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            isGettingLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    isGettingLocation = false
                    location?.let {
                        viewModel.updateLocation(it.latitude, it.longitude)
                        Toast.makeText(context, "Đã lấy vị trí GPS", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(context, "Không thể lấy vị trí. Vui lòng bật GPS.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    isGettingLocation = false
                    Toast.makeText(context, "Lỗi lấy vị trí: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                isGettingLocation = false
                Toast.makeText(context, "Không có quyền truy cập vị trí", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Cần quyền truy cập vị trí", Toast.LENGTH_SHORT).show()
        }
    }

    fun getGpsLocation() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Handle Map Result from SavedStateHandle (same pattern as AddressEditScreen)
    val pickedLatResult = savedStateHandle?.getLiveData<Double>("picked_lat")?.observeAsState()
    val pickedLngResult = savedStateHandle?.getLiveData<Double>("picked_lng")?.observeAsState()

    LaunchedEffect(pickedLatResult?.value, pickedLngResult?.value) {
        val lat = pickedLatResult?.value
        val lng = pickedLngResult?.value
        if (lat != null && lng != null) {
            // Update location in ViewModel
            viewModel.updateLocation(lat, lng)
            
            // Clear saved state
            savedStateHandle?.remove<Double>("picked_lat")
            savedStateHandle?.remove<Double>("picked_lng")
        }
    }

    // Handle action result (Toast)
    LaunchedEffect(actionResult) {
        actionResult?.let { result ->
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            viewModel.clearResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quản lý chi nhánh", fontWeight = FontWeight.Bold) 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
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
                // Basic Info Section
                Text(
                    "THÔNG TIN CƠ BẢN",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.updateFormField(name = it) },
                    label = { Text("Tên nhà hàng") },
                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = formState.phone,
                    onValueChange = { viewModel.updateFormField(phone = it) },
                    label = { Text("Số điện thoại") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = formState.openingHours,
                    onValueChange = { viewModel.updateFormField(openingHours = it) },
                    label = { Text("Giờ mở cửa (VD: 10:00 - 22:00)") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.maxCapacity,
                        onValueChange = { viewModel.updateFormField(maxCapacity = it) },
                        label = { Text("Sức chứa tối đa") },
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = formState.tableCount,
                        onValueChange = { viewModel.updateFormField(tableCount = it) },
                        label = { Text("Số bàn") },
                        leadingIcon = { Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Shipping Fee
                OutlinedTextField(
                    value = formState.shippingFee,
                    onValueChange = { viewModel.updateFormField(shippingFee = it) },
                    label = { Text("Phí vận chuyển (VNĐ)") },
                    leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    suffix = { Text("đ") }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Address Section
                Text(
                    "ĐỊA CHỈ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = formState.addressLine,
                    onValueChange = { viewModel.updateFormField(addressLine = it) },
                    label = { Text("Địa chỉ (số nhà, đường)") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = formState.ward,
                    onValueChange = { viewModel.updateFormField(ward = it) },
                    label = { Text("Phường/Xã") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.district,
                        onValueChange = { viewModel.updateFormField(district = it) },
                        label = { Text("Quận/Huyện") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = formState.city,
                        onValueChange = { viewModel.updateFormField(city = it) },
                        label = { Text("Thành phố") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = PrimaryColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Location Section with Mini Map
                Text(
                    "VỊ TRÍ TRÊN BẢN ĐỒ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Mini Map or Placeholder
                val lat = formState.latitude
                val lng = formState.longitude
                
                if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    ) {
                        // Initialize osm config
                        Configuration.getInstance().userAgentValue = context.packageName
                        
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(false)
                                    controller.setZoom(15.0)
                                    setOnTouchListener { _, _ -> true }
                                }
                            },
                            update = { mapView ->
                                val geoPoint = GeoPoint(lat, lng)
                                mapView.controller.setCenter(geoPoint)
                                
                                mapView.overlays.clear()
                                val marker = org.osmdroid.views.overlay.Marker(mapView)
                                marker.position = geoPoint
                                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(marker)
                                mapView.invalidate()
                            }
                        )
                        
                        // Clickable overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                                .clickable { onNavigateToMap(lat, lng) }
                        )

                        // Action buttons overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // GPS Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp
                            ) {
                                IconButton(
                                    onClick = { getGpsLocation() },
                                    enabled = !isGettingLocation
                                ) {
                                    if (isGettingLocation) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = SuccessGreen
                                        )
                                    } else {
                                        Icon(Icons.Default.MyLocation, contentDescription = "Lấy vị trí GPS", tint = SuccessGreen)
                                    }
                                }
                            }
                            
                            // Fullscreen Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp
                            ) {
                                IconButton(onClick = { onNavigateToMap(lat, lng) }) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = "Full map", tint = PrimaryColor)
                                }
                            }
                        }
                    }
                } else {
                    // Placeholder when no location
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToMap(null, null) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chọn vị trí trên bản đồ", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        
                        // GPS Button
                        OutlinedButton(
                            onClick = { getGpsLocation() },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                            enabled = !isGettingLocation
                        ) {
                            if (isGettingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = SuccessGreen
                                )
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = "Lấy vị trí GPS", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = { viewModel.saveBranch(onNavigateBack) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu thông tin", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
