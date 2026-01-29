package com.muatrenthenang.resfood.ui.screens.me

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
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

    // THEME LOGIC
    val isSystemDark = isSystemInDarkTheme()
    val isPremiumRank = currentRank == Rank.GOLD || currentRank == Rank.DIAMOND
    
    // Premium ranks force a dark gradient background, so we always want white content there.
    // Standard ranks follow the system theme (White bg in Light mode -> Dark content).
    val forceDarkBackground = isPremiumRank
    val useDarkContent = !forceDarkBackground && !isSystemDark
    
    val contentColor = if (useDarkContent) MaterialTheme.colorScheme.onBackground else Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.6f)
    
    // Glass/Border colors that adapt to the text contrast
    val glassBorderColor = contentColor.copy(alpha = 0.15f)
    val glassBgColor = contentColor.copy(alpha = 0.05f)

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
                            .background(glassBgColor, CircleShape)
                            .border(1.dp, glassBorderColor, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.common_back),
                            tint = contentColor
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        stringResource(R.string.membership_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(40.dp))
                }
                
                // Premium Card
                PremiumRankCard(rank = currentRank, name = stringResource(R.string.membership_you)) // You could pass actual name
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Section
                GlassCard(
                    backgroundColor = glassBgColor,
                    borderColor = glassBorderColor
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.membership_progress_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryContentColor,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        currencyFormatter.format(totalSpending),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor
                                    )
                                    Text(
                                        " / ${currencyFormatter.format(nextRankTarget)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = secondaryContentColor,
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
                                    color = contentColor
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
                            trackColor = glassBorderColor
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
                                val nextRank = Rank.getNextRank(currentRank)
                                Text(
                                    stringResource(R.string.membership_spend_more, currencyFormatter.format(remaining), if (nextRank != null) stringResource(nextRank.nameResId) else ""),
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
                        stringResource(R.string.membership_milestones),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        "",
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
                        onClaim = { viewModel.claimReward(it) },
                        contentColor = contentColor,
                        secondaryContentColor = secondaryContentColor,
                        lineColor = glassBorderColor
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
                stringResource(rank.nameResId).uppercase(),
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
fun GlassCard(
    backgroundColor: Color = Color.White.copy(alpha = 0.05f),
    borderColor: Color = Color.White.copy(alpha = 0.1f),
    content: @Composable () -> Unit
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
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
    onClaim: (RankReward) -> Unit,
    contentColor: Color = Color.White,
    secondaryContentColor: Color = Color.White.copy(alpha = 0.5f),
    lineColor: Color = Color.White.copy(alpha = 0.1f)
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
    
    val iconBg = if (isCurrent) iconColor.copy(alpha = 0.2f) else lineColor.copy(alpha = 0.5f)
    val iconBorder = if (isCurrent) iconColor.copy(alpha = 0.5f) else lineColor

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
                        .background(lineColor)
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
                        stringResource(rank.nameResId),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        if (rank.threshold == 0.0) stringResource(R.string.membership_start_milestone) else stringResource(R.string.membership_spend_greater, NumberFormat.getIntegerInstance().format(rank.threshold)),
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryContentColor
                    )
                }
                
                val statusText = when {
                    isCurrent -> stringResource(R.string.membership_status_current)
                    isPassed -> stringResource(R.string.membership_status_passed)
                    else -> stringResource(R.string.membership_status_locked)
                }
                val statusColor = if (isCurrent) PrimaryColor else secondaryContentColor.copy(alpha = 0.5f)
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
                RankRewardCard(
                    item = rewardItem, 
                    onClaim = { onClaim(rewardItem.reward) },
                    contentColor = contentColor,
                    secondaryContentColor = secondaryContentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RankRewardCard(
    item: RankRewardItem, 
    onClaim: () -> Unit,
    contentColor: Color,
    secondaryContentColor: Color
) {
    val alpha = if (item.status == RewardStatus.LOCKED) 0.5f else 1f
    val bgColor = when(item.reward.rankRequired) {
        Rank.GOLD -> Color(0xFFFACC15).copy(alpha = 0.1f)
        Rank.DIAMOND -> Color(0xFF6366F1).copy(alpha = 0.1f)
        else -> contentColor.copy(alpha = 0.05f)
    }
    val borderColor = when(item.reward.rankRequired) {
        Rank.GOLD -> Color(0xFFFACC15).copy(alpha = 0.3f)
        Rank.DIAMOND -> Color(0xFF6366F1).copy(alpha = 0.3f)
        else -> contentColor.copy(alpha = 0.15f)
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
                tint = contentColor.copy(alpha = 0.8f)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.reward.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = when(item.status) {
                    RewardStatus.LOCKED -> stringResource(R.string.reward_status_locked)
                    RewardStatus.AVAILABLE -> stringResource(R.string.reward_status_available)
                    RewardStatus.CLAIMED -> stringResource(R.string.reward_status_claimed)
                },
                style = MaterialTheme.typography.labelSmall,
                color = when(item.status) {
                    RewardStatus.AVAILABLE -> PrimaryColor
                    RewardStatus.CLAIMED -> secondaryContentColor
                    else -> secondaryContentColor.copy(alpha = 0.6f)
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
                Text(stringResource(R.string.membership_claim_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else if (item.status == RewardStatus.CLAIMED) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.membership_reward_claimed),
                tint = Color.Green.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
