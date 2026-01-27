package com.muatrenthenang.resfood.ui.screens.me

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.theme.ResFoodTheme
import com.muatrenthenang.resfood.ui.theme.rankColor
import com.muatrenthenang.resfood.ui.viewmodel.MeViewModel
import com.muatrenthenang.resfood.ui.viewmodel.MeOrderCounts
import com.muatrenthenang.resfood.ui.viewmodel.MeUserProfile
import com.muatrenthenang.resfood.ui.screens.me.components.BadgeCount
import com.muatrenthenang.resfood.ui.screens.me.components.BadgeDot
import com.muatrenthenang.resfood.ui.screens.me.components.IconCircleButton
import com.muatrenthenang.resfood.ui.screens.me.components.CircleIcon

import com.muatrenthenang.resfood.ui.viewmodel.UtilityIconType
import com.muatrenthenang.resfood.ui.viewmodel.UtilityMenuOption
import com.muatrenthenang.resfood.ui.viewmodel.ReferralPromoData

@Composable
fun MeScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToOrders: (String) -> Unit = {},
    onNavigateToReferral: () -> Unit = {},
    onNavigateToVouchers: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onLogout: () -> Unit = {},
    paddingValuesFromParent: PaddingValues = PaddingValues(),
    vm: MeViewModel = viewModel(),
    userViewModel: com.muatrenthenang.resfood.ui.viewmodel.UserViewModel = viewModel()
) {
    // Lấy dữ liệu user thật từ UserViewModel
    val realUser by userViewModel.userState.collectAsState()
    
    val orderCounts by vm.orderCounts.collectAsState()
    val referralPromo by vm.referralPromo.collectAsState()
    val utilityMenu by vm.utilityMenu.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MeTopBar(
                onSettingsClick = onNavigateToSettings,
                onNotificationsClick = onNavigateToNotifications
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(bottom = paddingValuesFromParent.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Profile Header Section - Dùng dữ liệu thật từ UserViewModel
            ProfileHeaderCard(
                userProfile = MeUserProfile(
                    name = realUser?.fullName ?: "Người dùng",
                    avatarUrl = realUser?.avatarUrl ?: "",
                    rank = realUser?.rank ?: "Bronze",
                    rankDisplayName = "Thành viên ${realUser?.rank ?: "Bronze"}"
                ),
                onEditProfileClick = onNavigateToEditProfile
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Orders Status Section
            OrdersStatusSection(
                orderCounts = orderCounts,
                onViewAll = { onNavigateToOrders("all") },
                onOrderStatusClick = { status -> onNavigateToOrders(status) }
            )

            Spacer(modifier = Modifier.height(20.dp))
            // Referral Promo Card
            ReferralPromoCard(
                promoData = referralPromo,
                onInviteClick = onNavigateToReferral
            )

            Spacer(modifier = Modifier.height(12.dp))
            // Utilities Menu List
            UtilitiesSection(
                utilityMenu = utilityMenu,
                onOptionClick = { id ->
                    when (id) {
                        "vouchers" -> onNavigateToVouchers()
                        "addresses" -> onNavigateToAddresses()
                        "help" -> onNavigateToHelpCenter()
                        "payment" -> onNavigateToPaymentMethods()
                    }
                }
            )


            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun MeTopBar(
    onSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings button
        IconCircleButton(
            icon = Icons.Default.Settings,
            onClick = onSettingsClick,
            contentDescription = "Cài đặt"
        )

        Text(
            text = "Cá nhân",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Notifications button with badge
        Box {
            IconCircleButton(
                icon = Icons.Default.Notifications,
                onClick = onNotificationsClick,
                contentDescription = "Thông báo"
            )
            // Notification badge
            BadgeDot(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp),
                color = PrimaryColor
            )
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    userProfile: MeUserProfile,
    onEditProfileClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 16.dp),

        ) {
            // Avatar with border - clickable to edit profile
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(2.dp, PrimaryColor, CircleShape)
                    .padding(4.dp)
                    .clickable { onEditProfileClick() }
            ) {
                AsyncImage(
                    model = userProfile.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEditProfileClick() }
            ) {
                Text(
                    text = userProfile.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = rankColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userProfile.rankDisplayName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OrdersStatusSection(
    orderCounts: MeOrderCounts,
    onViewAll: () -> Unit,
    onOrderStatusClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đơn hàng của tôi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
                // color = MaterialTheme.colorScheme.onBackground // Removed to match Checkout
            )
            TextButton(onClick = onViewAll) {
                Text(
                    text = "Xem tất cả",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OrderStatusItem(
                    icon = Icons.Default.Receipt,
                    label = "Chờ xác nhận",
                    count = orderCounts.pending,
                    onClick = { onOrderStatusClick("pending") }
                )
                OrderStatusItem(
                    icon = Icons.Default.LocalDining,
                    label = "Đang chế biến",
                    count = orderCounts.processing,
                    onClick = { onOrderStatusClick("processing") }
                )
                OrderStatusItem(
                    icon = Icons.Default.DeliveryDining,
                    label = "Đang giao",
                    count = orderCounts.delivering,
                    onClick = { onOrderStatusClick("delivering") }
                )
                OrderStatusItem(
                    icon = Icons.Default.RateReview,
                    label = "Đánh giá",
                    count = orderCounts.toReview,
                    onClick = { onOrderStatusClick("review") }
                )
            }
        }
    }
}

@Composable
private fun OrderStatusItem(
    icon: ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box {
            CircleIcon(
                icon = icon,
                size = 48.dp,
                iconSize = 24.dp
            )
            // Badge for count
            BadgeCount(
                count = count,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun ReferralPromoCard(
    promoData: ReferralPromoData,
    onInviteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PrimaryColor,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            // Background gift icon
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
            )

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = promoData.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = promoData.subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Button(
                    onClick = onInviteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = promoData.buttonText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun UtilitiesSection(
    utilityMenu: List<UtilityMenuOption>,
    onOptionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Tiện ích",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                utilityMenu.forEachIndexed { index, option ->
                    UtilityMenuItem(
                        icon = getIconForType(option.iconType),
                        iconColor = getColorForType(option.iconType),
                        title = option.title,
                        subtitle = option.subtitle,
                        onClick = { onOptionClick(option.id) },
                        showDivider = index < utilityMenu.size - 1
                    )
                }
            }
        }
    }
}

// Helper to map enum to Vector
private fun getIconForType(type: UtilityIconType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        UtilityIconType.VOUCHER -> Icons.Default.ConfirmationNumber
        UtilityIconType.ADDRESS -> Icons.Default.LocationOn
        UtilityIconType.HELP -> Icons.Default.HelpCenter
        UtilityIconType.PAYMENT -> Icons.Default.Payment
    }
}

// Helper to map enum to Color
private fun getColorForType(type: UtilityIconType): Color {
    return when (type) {
        UtilityIconType.VOUCHER -> Color(0xFF3B82F6) // Blue
        UtilityIconType.ADDRESS -> SuccessGreen
        UtilityIconType.HELP -> Color(0xFF8B5CF6) // Purple
        UtilityIconType.PAYMENT -> Color(0xFFF97316) // Orange
    }
}

@Composable
private fun UtilityMenuItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIcon(
                icon = icon,
                size = 40.dp,
                backgroundColor = iconColor.copy(alpha = 0.1f),
                iconTint = iconColor,
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun LogoutSection(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightRed.copy(alpha = 0.1f),
                contentColor = LightRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Đăng xuất",
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "RESFOOD VERSION 2.4.0",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MeScreenPreviewLight() {
    ResFoodTheme(darkTheme = false) {
        MeScreen()
    }
}
