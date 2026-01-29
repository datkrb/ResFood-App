package com.muatrenthenang.resfood.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.ui.viewmodel.UserViewModel


private val TextColorSecondary = Color.Gray

@Composable
fun SettingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    paddingValuesFromParent: PaddingValues = PaddingValues(),
    userViewModel: UserViewModel
) {
    val userState by userViewModel.userState.collectAsState()
    val user = userState ?: User(fullName = "Đang tải...", rank = "...") // Placeholder khi loading
    val isDarkTheme by userViewModel.isDarkTheme.collectAsState()
    val isPushNotificationEnabled by userViewModel.isPushNotificationEnabled.collectAsState()
    val currentLanguage by userViewModel.currentLanguage.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsTopBar(onBack = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(bottom = paddingValuesFromParent.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HIỂN THỊ
            SectionHeader(title = stringResource(id = R.string.settings_display))
            SettingToggleRow(
                title = stringResource(id = R.string.settings_dark_mode),
                subtitle = stringResource(id = R.string.settings_dark_mode_subtitle),
                checked = isDarkTheme, // Hiển thị theo state
                onCheckedChange = { isChecked: Boolean ->
                    userViewModel.toggleTheme(isChecked)
                }
            )

            // 3. Thông báo
            SectionHeader(title = stringResource(id = R.string.settings_notifications))
            SettingToggleRow(
                title = stringResource(id = R.string.settings_push_notifications),
                subtitle = stringResource(id = R.string.settings_push_notifications_subtitle),
                checked = isPushNotificationEnabled,
                onCheckedChange = { isEnabled: Boolean ->
                    userViewModel.togglePushNotification(isEnabled)
                }
            )

            // Language
            SectionHeader(title = stringResource(id = R.string.settings_language)) // Or just re-use Display/General
            SettingItemRow(
                title = stringResource(id = R.string.settings_language),
                subtitle = if (currentLanguage == "vi") "Tiếng Việt" else "English",
                showArrow = true,
                modifier = Modifier.clickable { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Version
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ResFood cho Android", color = TextColorSecondary, fontSize = 12.sp)
                Text("Phiên bản 0.0.0 (Build 2025)", color = TextColorSecondary, fontSize = 12.sp)
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                userViewModel.setLanguage("vi")
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == "vi",
                            onClick = {
                                userViewModel.setLanguage("vi")
                                showLanguageDialog = false
                            }
                        )
                        Text("Tiếng Việt", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                userViewModel.setLanguage("en")
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == "en",
                            onClick = {
                                userViewModel.setLanguage("en")
                                showLanguageDialog = false
                            }
                        )
                        Text("English", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(title: String, badge: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFF1B5E20),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badge,
                    color = Color(0xFF4CAF50),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SettingItemRow(title: String, subtitle: String? = null, showArrow: Boolean = false, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().padding(bottom = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                if (subtitle != null) {
                    Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            if (showArrow) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingToggleRow(title: String, subtitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit = {}) {
    //var isChecked by remember { mutableStateOf(checked) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                if (subtitle != null) {
                    Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}