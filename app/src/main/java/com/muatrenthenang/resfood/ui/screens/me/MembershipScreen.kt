package com.muatrenthenang.resfood.ui.screens.me

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.data.model.Rank
import com.muatrenthenang.resfood.data.model.RankReward
import com.muatrenthenang.resfood.data.model.RankRewardType
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.viewmodel.MembershipUiState
import com.muatrenthenang.resfood.ui.viewmodel.MembershipViewModel
import com.muatrenthenang.resfood.ui.viewmodel.RankRewardItem
import com.muatrenthenang.resfood.ui.viewmodel.RewardStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MembershipScreen(
    onNavigateBack: () -> Unit,
    viewModel: MembershipViewModel = viewModel()
) {
    val currentRank by viewModel.currentRank.collectAsState()
    val totalSpending by viewModel.totalSpending.collectAsState()
    val rewardsList by viewModel.rewardsList.collectAsState()
    val progress = viewModel.getProgress()
    val nextRankTarget = viewModel.getNextRankTarget()
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    val uiState by viewModel.uiState.collectAsState()

    // Background colors based on rank for a subtle effect
    val bgModifier = when (currentRank) {
        Rank.DIAMOND -> Modifier.background(Brush.verticalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
        ))
        Rank.GOLD -> Modifier.background(Brush.verticalGradient(
            listOf(Color(0xFF18181B), Color(0xFF422006))
        ))
        else -> Modifier.background(MaterialTheme.colorScheme.background)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent, // Handle in box
            modifier = bgModifier
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Hạng thành viên & Ưu đãi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(40.dp))
                }
                
                // Premium Card
                PremiumRankCard(rank = currentRank, name = "Bạn") // You could pass actual name
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Section
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    "TIẾN TRÌNH THĂNG HẠNG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        currencyFormatter.format(totalSpending),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        " / ${currencyFormatter.format(nextRankTarget)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                    )
                                }
                            }
                            
                            // % Circle
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .border(4.dp, PrimaryColor.copy(alpha = 0.2f), CircleShape)
                            ) {
                                 CircularProgressIndicator(
                                     progress = { progress },
                                     modifier = Modifier.fillMaxSize(),
                                     color = PrimaryColor,
                                     strokeWidth = 4.dp,
                                     trackColor = Color.Transparent,
                                 )
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = PrimaryColor,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        
                        if (currentRank != Rank.DIAMOND) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info, 
                                    contentDescription = null, 
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val remaining = nextRankTarget - totalSpending
                                Text(
                                    "Chi tiêu thêm ${currencyFormatter.format(remaining)} để lên hạng ${Rank.getNextRank(currentRank)?.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryColor.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Milestones & Rewards
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Các mốc hạng thành viên",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Chi tiết",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Render Ranks Logic
                Rank.entries.forEach { rank ->
                    RankTimelineItem(
                        rank = rank, 
                        currentRank = currentRank, 
                        rewards = rewardsList.filter { it.reward.rankRequired == rank },
                        onClaim = { viewModel.claimReward(it) }
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Loading Indicator Overlay
        if (uiState is MembershipUiState.Loading) {
            Box(
                 modifier = Modifier
                     .fillMaxSize()
                     .background(Color.Black.copy(alpha = 0.5f)),
                 contentAlignment = Alignment.Center
            ) {
                 CircularProgressIndicator(color = PrimaryColor)
            }
        }
    }
}

@Composable
fun PremiumRankCard(rank: Rank, name: String) {
    // Dynamic Gradient based on rank
    val gradient = when(rank) {
        Rank.DIAMOND -> Brush.linearGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF4338CA), Color(0xFF6366F1)) // Dark Blue/Indigo
        )
        Rank.GOLD -> Brush.linearGradient(
            colors = listOf(Color(0xFF422006), Color(0xFFCA8A04), Color(0xFFFACC15)) // Gold/Yellow
        )
        Rank.SILVER -> Brush.linearGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF94A3B8), Color(0xFFCBD5E1)) // Slate
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF334155)) // Generic Dark
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
    ) {
        // Abstract overlay pattern (simple circles for now)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 300f,
                center = center.copy(x = size.width, y = 0f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 150f,
                center = center.copy(x = 0f, y = size.height)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                "ResFood Elite", 
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                rank.displayName.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun RankTimelineItem(
    rank: Rank,
    currentRank: Rank,
    rewards: List<RankRewardItem>,
    onClaim: (RankReward) -> Unit
) {
    val isPassed = rank.threshold <= currentRank.threshold
    val isCurrent = rank == currentRank
    
    val icon = when(rank) {
        Rank.MEMBER -> Icons.Default.Person
        Rank.SILVER -> Icons.Default.Shield
        Rank.GOLD -> Icons.Default.WorkspacePremium // Crown replacement
        Rank.DIAMOND -> Icons.Default.Diamond
    }
    
    val iconColor = when(rank) {
        Rank.MEMBER -> Color.White.copy(alpha = 0.4f)
        Rank.SILVER -> Color(0xFF94A3B8)
        Rank.GOLD -> Color(0xFFFACC15)
        Rank.DIAMOND -> Color(0xFF818CF8)
    }
    
    val iconBg = if (isCurrent) iconColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
    val iconBorder = if (isCurrent) iconColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)

    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min) // Intrinsic height for connecting line
    ) {
        // Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, CircleShape)
                    .border(1.dp, iconBorder, CircleShape)
                    .graphicsLayer { 
                        if (isCurrent) {
                            shadowElevation = 10.dp.toPx()
                            this.shape = CircleShape
                            this.clip = true
                        }
                    }
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            
            // Connecting line
            if (rank != Rank.DIAMOND) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        rank.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        if (rank.threshold == 0.0) "Mốc khởi đầu" else "Chi tiêu > ${NumberFormat.getIntegerInstance().format(rank.threshold)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                val statusText = when {
                    isCurrent -> "HIỆN TẠI"
                    isPassed -> "ĐÃ QUA"
                    else -> "LOCKED"
                }
                val statusColor = if (isCurrent) PrimaryColor else Color.White.copy(alpha = 0.3f)
                val statusBg = statusColor.copy(alpha = 0.1f)
                
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            
            // Rewards
            rewards.forEach { rewardItem ->
                RankRewardCard(item = rewardItem, onClaim = { onClaim(rewardItem.reward) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RankRewardCard(item: RankRewardItem, onClaim: () -> Unit) {
    val alpha = if (item.status == RewardStatus.LOCKED) 0.5f else 1f
    val bgColor = when(item.reward.rankRequired) {
        Rank.GOLD -> Color(0xFFFACC15).copy(alpha = 0.1f)
        Rank.DIAMOND -> Color(0xFF6366F1).copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.05f)
    }
    val borderColor = when(item.reward.rankRequired) {
        Rank.GOLD -> Color(0xFFFACC15).copy(alpha = 0.3f)
        Rank.DIAMOND -> Color(0xFF6366F1).copy(alpha = 0.3f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .graphicsLayer { this.alpha = alpha },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(borderColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (item.reward.type == RankRewardType.FREE_SHIP) Icons.Default.LocalShipping else Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.reward.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                item.status.name, // Or description? Let's use status for now for UX feedback
                style = MaterialTheme.typography.labelSmall,
                color = when(item.status) {
                    RewardStatus.AVAILABLE -> PrimaryColor
                    RewardStatus.CLAIMED -> Color.White.copy(alpha = 0.5f)
                    else -> Color.White.copy(alpha = 0.3f)
                },
                fontWeight = FontWeight.Bold
            )
        }
        
        if (item.status == RewardStatus.AVAILABLE) {
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Nhận", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else if (item.status == RewardStatus.CLAIMED) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Claimed",
                tint = Color.Green.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
