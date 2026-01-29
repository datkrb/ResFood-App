package com.muatrenthenang.resfood.ui.screens.booking

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.theme.*
import com.muatrenthenang.resfood.ui.viewmodel.BookingTableViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTableScreen(
    onNavigateBack: () -> Unit,
    viewModel: BookingTableViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Branch from Firestore
    val branch by viewModel.branch.collectAsState()
    
    val dates by viewModel.dates.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val guestCountAdult by viewModel.guestCountAdult.collectAsState()
    val guestCountChild by viewModel.guestCountChild.collectAsState()
    
    val note by viewModel.note.collectAsState()
    
    val selectedHour by viewModel.selectedHour.collectAsState()
    val selectedMinute by viewModel.selectedMinute.collectAsState()
    
    val availableHours = viewModel.availableHours
    val availableMinutes = viewModel.availableMinutes

    val bookingState by viewModel.bookingState.collectAsState()

    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is com.muatrenthenang.resfood.ui.viewmodel.BookingState.Success -> {
                android.widget.Toast.makeText(context, context.getString(R.string.booking_success_msg), android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetBookingState()
                onNavigateBack()
            }
            is com.muatrenthenang.resfood.ui.viewmodel.BookingState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetBookingState()
            }
            else -> {}
        }
    }

    // Main Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    stringResource(R.string.booking_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(40.dp))
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp) // Space for sticky bottom bar
            ) {
                // 2. Restaurant Info (from Firestore)
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        stringResource(R.string.booking_restaurant_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (branch == null) {
                        // Loading state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Restaurant Info Display
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryColor.copy(alpha = 0.1f))
                                    .border(1.dp, PrimaryColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        branch!!.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        branch!!.address.getFullAddress(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 2
                                    )
                                }
                            }
                            
                            // Map Button
                            IconButton(
                                onClick = {
                                    val lat = branch?.address?.latitude
                                    val lng = branch?.address?.longitude
                                    if (lat != null && lng != null) {
                                        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${branch?.name})")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    }
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryColor.copy(alpha = 0.1f))
                                    .border(1.dp, PrimaryColor, RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    Icons.Default.Place, 
                                    contentDescription = "Map",
                                    tint = PrimaryColor
                                )
                            }
                        }
                    }
                }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            // 3. Date & Time
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        stringResource(R.string.booking_date_time),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        selectedDate.format(DateTimeFormatter.ofPattern("MMMM, yyyy", Locale("vi"))),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Calendar Days (Horizontal)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dates) { date ->
                        val isSelected = date == selectedDate
                        val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE", Locale("vi")))
                        val dayOfMonth = date.dayOfMonth.toString()
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(50.dp)
                                .clip(RoundedCornerShape(8  .dp))
                                .background(if (isSelected) PrimaryColor else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.selectDate(date) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                dayOfWeek,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                dayOfMonth,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.booking_hour),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableHours) { hour ->
                            val isSelected = hour == selectedHour
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryColor else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) PrimaryColor else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectHour(hour) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    String.format("%02d", hour),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.booking_minute),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableMinutes) { minute ->
                            val isSelected = minute == selectedMinute
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryColor else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) PrimaryColor else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectMinute(minute) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    String.format("%02d", minute),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            // 4. Guest Counter
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    stringResource(R.string.booking_guest_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Adult
                GuestCounterInfo(
                    title = stringResource(R.string.booking_guest_adult),
                    subtitle = stringResource(R.string.booking_guest_adult_desc),
                    count = guestCountAdult,
                    onDecrease = { viewModel.updateGuestAdult(-1) },
                    onIncrease = { viewModel.updateGuestAdult(1) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Child
                GuestCounterInfo(
                    title = stringResource(R.string.booking_guest_child),
                    subtitle = stringResource(R.string.booking_guest_child_desc),
                    count = guestCountChild,
                    onDecrease = { viewModel.updateGuestChild(-1) },
                    onIncrease = { viewModel.updateGuestChild(1) },
                    isZeroFaded = true
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            // 5. Note Field
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    stringResource(R.string.booking_note),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { viewModel.updateNote(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    placeholder = { Text(stringResource(R.string.booking_note_hint), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    minLines = 3
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // 7. Bottom Bar
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 10.dp,
            tonalElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.navigationBarsPadding().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.booking_bottom_time), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                        Text(
                            "$formattedTime - ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM"))}", 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    ContainerDivider()
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.booking_bottom_guest), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Text(
                            "$guestCountAdult Lớn${if(guestCountChild > 0) ", $guestCountChild Trẻ" else ""}", 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Button(
                    onClick = { viewModel.confirmBooking() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = branch != null && bookingState !is com.muatrenthenang.resfood.ui.viewmodel.BookingState.Loading
                ) {
                    if (bookingState is com.muatrenthenang.resfood.ui.viewmodel.BookingState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            stringResource(R.string.booking_btn_confirm),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun GuestCounterInfo(
    title: String,
    subtitle: String,
    count: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    isZeroFaded: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(enabled = count > 0) { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (count > 0) 0.7f else 0.3f), modifier = Modifier.padding(4.dp))
            }
            
            Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(PrimaryColor)
                    .clickable { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
fun ContainerDivider() {
    Box(
        modifier = Modifier
            .height(30.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}
