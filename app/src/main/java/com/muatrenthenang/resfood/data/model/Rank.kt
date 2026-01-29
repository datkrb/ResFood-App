package com.muatrenthenang.resfood.data.model

import com.muatrenthenang.resfood.R

enum class Rank(val displayName: String, val threshold: Double, val colorHex: Long, val nameResId: Int) {
    MEMBER("Thân thiết", 0.0, 0xFF64748B, R.string.rank_member),
    SILVER("Bạc", 1_000_000.0, 0xFF94A3B8, R.string.rank_silver),
    GOLD("Vàng", 5_000_000.0, 0xFFF2B90D, R.string.rank_gold),
    DIAMOND("Kim cương", 10_000_000.0, 0xFF6366F1, R.string.rank_diamond);

    companion object {
        fun getRankFromSpending(spending: Double): Rank {
            return when {
                spending >= DIAMOND.threshold -> DIAMOND
                spending >= GOLD.threshold -> GOLD
                spending >= SILVER.threshold -> SILVER
                else -> MEMBER
            }
        }
        
        fun getNextRank(currentRank: Rank): Rank? {
            return when (currentRank) {
                MEMBER -> SILVER
                SILVER -> GOLD
                GOLD -> DIAMOND
                DIAMOND -> null
            }
        }
    }
}

data class RankReward(
    val id: String,
    val title: String,
    val description: String,
    val rankRequired: Rank,
    val voucherValue: Int, // e.g. 50000 -> 50k
    val type: RankRewardType = RankRewardType.VOUCHER
)

enum class RankRewardType {
    VOUCHER, FREE_SHIP, GIFT
}
