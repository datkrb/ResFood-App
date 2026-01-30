package com.muatrenthenang.resfood.ui.screens.address

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
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
import com.google.android.gms.location.LocationServices
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.model.AddressLabels
import androidx.compose.ui.viewinterop.AndroidView
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.viewmodel.AddressViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import com.muatrenthenang.resfood.R
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

    // GPS location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isGettingLocation by remember { mutableStateOf(false) }
    
    // Resource strings for Toast and Validation
    val gpsSuccessMsg = stringResource(R.string.address_gps_success)
    val gpsFailMsg = stringResource(R.string.address_gps_fail)
    val gpsErrorFormat = stringResource(R.string.address_gps_error)
    val permissionDeniedMsg = stringResource(R.string.address_permission_denied)
    val permissionRequiredMsg = stringResource(R.string.address_permission_required)
    
    val errEmptyLine = stringResource(R.string.address_err_empty_line)
    val errEmptyName = stringResource(R.string.address_err_empty_name)
    val errEmptyPhone = stringResource(R.string.address_err_empty_phone)
    val errInvalidPhone = stringResource(R.string.address_err_invalid_phone)
    val errEmptyCity = stringResource(R.string.address_err_empty_city)
    val errNoLocation = stringResource(R.string.address_err_no_location)

    // Permission launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permission granted, get location
            isGettingLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    isGettingLocation = false
                    location?.let {
                        vm.updateLocation(it.latitude, it.longitude)
                        Toast.makeText(context, gpsSuccessMsg, Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(context, gpsFailMsg, Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    isGettingLocation = false
                    Toast.makeText(context, gpsErrorFormat.format(it.message), Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                isGettingLocation = false
                Toast.makeText(context, permissionDeniedMsg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, permissionRequiredMsg, Toast.LENGTH_SHORT).show()
        }
    }
    
    // Function to get GPS location
    fun getGpsLocation() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Validation errors (UI only)
    var addressLineError by remember { mutableStateOf<String?>(null) }
    var contactNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

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
        locationError = null

        // Validate using formState
        var hasError = false

        if (formState.addressLine.isBlank()) {
            addressLineError = errEmptyLine
            hasError = true
        }
        if (formState.contactName.isBlank()) {
            contactNameError = errEmptyName
            hasError = true
        }
        if (formState.phone.isBlank()) {
            phoneError = errEmptyPhone
            hasError = true
        } else if (!formState.phone.matches(Regex("^[0-9+\\s]{10,15}$"))) {
            phoneError = errInvalidPhone
            hasError = true
        }
        if (formState.city.isBlank()) {
            cityError = errEmptyCity
            hasError = true
        }

        // Check for coordinates
        if (formState.latitude == null || formState.longitude == null || formState.latitude == 0.0 || formState.longitude == 0.0) {
            locationError = errNoLocation
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
                            contentDescription = stringResource(R.string.common_back),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = if (isEditing) stringResource(R.string.address_edit_title) else stringResource(R.string.address_add_title),
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
                    text = stringResource(R.string.address_label_type),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                ExposedDropdownMenuBox(
                    expanded = showLabelDropdown,
                    onExpandedChange = { showLabelDropdown = it }
                ) {
                    OutlinedTextField(
                        value = when(formState.label) {
                            "Nhà riêng" -> stringResource(R.string.address_label_home)
                            "Công ty" -> stringResource(R.string.address_label_work)
                            else -> formState.label
                        },
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = when (formState.label) {
                                    "Nhà riêng", stringResource(R.string.address_label_home) -> Icons.Default.Home
                                    "Công ty", stringResource(R.string.address_label_work) -> Icons.Default.Work
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
                                text = { 
                                    Text(when(option) {
                                        "Nhà riêng" -> stringResource(R.string.address_label_home)
                                        "Công ty" -> stringResource(R.string.address_label_work)
                                        else -> option
                                    })
                                },
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
                    text = stringResource(R.string.address_label_receiver),
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
                    label = stringResource(R.string.auth_full_name),
                    placeholder = stringResource(R.string.auth_full_name_placeholder),
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
                    label = stringResource(R.string.profile_phone),
                    placeholder = stringResource(R.string.profile_phone_placeholder),
                    icon = Icons.Default.Phone,
                    error = phoneError,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Address section
                Text(
                    text = stringResource(R.string.address_label_info),
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
                    label = stringResource(R.string.address_detail),
                    placeholder = stringResource(R.string.address_placeholder_street),
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
                        label = stringResource(R.string.address_ward),
                        placeholder = stringResource(R.string.address_placeholder_ward),
                        icon = Icons.Default.Place,
                        modifier = Modifier.weight(1f),
                        imeAction = ImeAction.Next
                    )

                    FormTextField(
                        value = formState.district,
                        onValueChange = { vm.updateFormField(district = it) },
                        label = stringResource(R.string.address_district),
                        placeholder = stringResource(R.string.address_placeholder_district),
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
                    label = stringResource(R.string.address_city),
                    placeholder = stringResource(R.string.address_placeholder_city),
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

                        // Action buttons overlay (GPS + Fullscreen)
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
                                        Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.common_gps_action), tint = SuccessGreen)
                                    }
                                }
                            }
                            
                            // Fullscreen Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp
                            ) {
                                IconButton(onClick = { onNavigateToMap(formState.latitude, formState.longitude) }) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = stringResource(R.string.common_fullscreen), tint = PrimaryColor)
                                }
                            }
                        }
                    }
                } else {
                    // Placeholder when no location selected - Row with Map Picker and GPS buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToMap(null, null) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.address_map_pick),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        
                        // GPS Button
                        OutlinedButton(
                            onClick = { getGpsLocation() },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SuccessGreen
                            ),
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

                if (locationError != null) {
                    Text(
                        text = locationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
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
                                    text = stringResource(R.string.address_default_set),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.address_default_desc),
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
                            text = if (isEditing) stringResource(R.string.address_update_btn) else stringResource(R.string.address_save_btn),
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
